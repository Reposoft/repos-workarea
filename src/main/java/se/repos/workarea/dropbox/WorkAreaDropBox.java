/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.workarea.dropbox;
import se.repos.workarea.WorkArea;

import java.util.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Date;
import java.io.FileOutputStream;

import se.repos.backend.file.CmsCommitFilesystem;
import se.repos.backend.file.CmsItemLockFile;
import se.repos.backend.file.CmsItemLookupFilesystem;
import se.repos.backend.file.WorkAreaCmsItemAdditionalOperations;
import se.repos.lgr.Lgr;
import se.repos.lgr.LgrFactory;
import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.CmsItemPath;
import se.simonsoft.cms.item.CmsRepository;
import se.simonsoft.cms.item.commit.CmsItemLockedException;
import se.simonsoft.cms.item.impl.CmsRepositoryId;

import com.dropbox.client2.exception.DropboxException; 
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.WebAuthSession;	
import com.dropbox.client2.session.RequestTokenPair;


public class WorkAreaDropBox implements WorkArea{

	//Dropbox application key and secret
	private static final String KEY = "lz6b5bi5iochme1"; 	
	private static final String SECRET = "d1bcl1j7ifxw4qj";
	//Dropbox access type
	private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
	private AppKeyPair appKeys;
	private DropboxAPI<WebAuthSession> api;


	public static final Lgr logger = LgrFactory.getLogger();
	private CmsItemLookupFilesystem cmsLookup;
	private CmsCommitFilesystem cmsCommit;
	private CmsRepository repo;
	private WorkAreaCmsItemAdditionalOperations workareaOperations;
	private DropboxTokenStore tokenStore;
	
	private String acceptUrl;
	private String tempRepository;
	private String dropboxURL;
	private String dropboxAccept;
	private String userName;

	public WorkAreaDropBox(String userName){
		this.userName = userName;
		//Repository set to local temp folder
		this.tempRepository = "tmp/repos-test/";
		this.dropboxURL = "http://localhost:8088/repos/work/admin";
		this.dropboxAccept = "If you have not accepted this applikation for use in dropbox";
		this.dropboxAccept += "please visit this URL: " + dropboxURL +" and do so.";
		
		this.repo = new CmsRepository("http://localhost/svn/testrepo");
		File root = new File(tempRepository);
		cmsLookup = new CmsItemLookupFilesystem(this.repo,root);
		this.workareaOperations = new WorkAreaCmsItemAdditionalOperations();
		this.cmsCommit = new CmsCommitFilesystem(this.repo,root);
	}


	/**
	*Re-authentication to dropbox if TOKENS file exists
	*/
	
	private void reAuthenticate(){
		//read token key and secret with username
		List<String> tokensList = tokenStore.read(this.userName);
		String AUTH_KEY = tokensList.get(0);
		String AUTH_SECRET = tokensList.get(1);
		// re-authenticate with dropbox key and secret
		AccessTokenPair reAuthTokens = new AccessTokenPair(AUTH_KEY,AUTH_SECRET);
		api.getSession().setAccessTokenPair(reAuthTokens);
		logger.info("Re-authentication Sucessful");
		// Run test command
		try {
			logger.info("Hello there, " +
					api.accountInfo().displayName + "(" +
					api.accountInfo().quota + ")");
		} catch (DropboxException e) {
			String errorMsg = "Could not retreive account info. " + dropboxAccept;
			logger.error(errorMsg,e);
			throw new RuntimeException(errorMsg,e);
		}
	}

	/**
	*A first authentication to dropbox to get dropbox key and secret and create TOKENS file to store them in
	*/
	
	private void firstAuthenticate(){
		AccessTokenPair tokenPair = api.getSession().getAccessTokenPair();
		RequestTokenPair tokens = new RequestTokenPair(tokenPair.key, tokenPair.secret);
		try {
			// Perform Check
			api.getSession().retrieveWebAccessToken(tokens);
		} catch (DropboxException e) {
			String errorMsg = "Could not retreive WebAccessTokens. "  + dropboxAccept;
			logger.error(errorMsg,e);
			throw new RuntimeException(errorMsg,e);
		}
		List<String> tokensList = new LinkedList<String>();
		tokensList.add(api.getSession().getAccessTokenPair().key);
		tokensList.add(api.getSession().getAccessTokenPair().secret);
		// Save token
		this.tokenStore.write(this.userName, tokensList);
	}


	/**
	*If TOKENS file exist with dropbox key and secret then reauthenticate 
	*otherwise it is the first time and we do a first authentication
	*/
	private void authenticate(){
		//check if already authenticated
		List<String> tokensFile = tokenStore.read(this.userName); 
		if (tokensFile != null && !tokensFile.isEmpty()) {
			// tokensFile does already exist
			logger.info("TokensFile seems to exist. Attempting reauthentication");
			reAuthenticate();
		} else {
			// tokensFile does not exist, create (First authentication)
			logger.info("TokensFile does not exist. Performing first authentication");
			firstAuthenticate();
		}
	}


	/**
	*Upload files to a specific folder in dropbox
	*@param folderName The name of the folder which to upload the files to
	*@param pathList List of paths to the files in repository
	*/
	public void uploadFile(String folderName,  List<CmsItemId> cmsItems){
		//Authenticate to dropbox a account
		authenticate();
		List<CmsItemId> lockFiles = new LinkedList<CmsItemId>();
		for(CmsItemId cmsId : cmsItems){
			File source = new File(tempRepository + cmsId.getRelPath().getPath());
			String dropboxPath = folderName + "/" + source.getName();
			
			try{
				cmsCommit.lock(dropboxPath, cmsId.getRelPath());
				lockFiles.add(cmsId);
			}catch(CmsItemLockedException cms){
				unlock(lockFiles);
				String errorMsg = cms.getMessage();
	    		logger.error(errorMsg,cms);
	    		throw new RuntimeException(errorMsg,cms);
			}
		}
		for(CmsItemId cmsId : cmsItems){
			try{
				File source = new File(tempRepository + cmsId.getRelPath().getPath());
				FileInputStream fis = new FileInputStream(source);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte [] buf = new byte[1024];
				for(int readNum; (readNum = fis.read(buf)) != -1;) {
			    	bos.write(buf, 0, readNum);
				}
				fis.close();
				ByteArrayInputStream inputStream2 = new ByteArrayInputStream(bos.toByteArray());
				//Upload file to dropbox folder: folderName, if folderName don't exist it is created
				String dropboxPath = folderName + "/" + source.getName();
				
				api.putFile(dropboxPath, inputStream2,bos.size(), null, null);
				long lDateTime = new Date().getTime();
				source.setLastModified(lDateTime);
			}catch(IOException e){
				unlock(cmsItems);
				String errorMsg =  "Problem while closing inputstream " +e;
				logger.error(errorMsg,e);
				throw new RuntimeException(errorMsg,e);
			}catch(DropboxException de){
				unlock(cmsItems);
				String errorMsg = "Something went wrong while trying to upload to dropbox. " + dropboxAccept;
	    		logger.error(errorMsg,de);
	    		throw new RuntimeException(errorMsg,de);
			}
		}
	}


	/**
	*Gets a list of files for a local repository (tmp/repos-test)
	*@return List A list of the files in repository
	*/
	public List<String> getFileList(){
		CmsRepositoryId cmsId = new CmsRepositoryId(repo);
		return this.workareaOperations.getRepositoryFileNames(cmsLookup.getImmediateFiles(cmsId));
	}


	/**
	* Checks to see if any files in dropbox has been changed since uploaded.
	* And checks if you have a tokens file to indicate that you've already accepted this application to use dropbox account
	*
	* @return List File paths to all files which have been changed
	*/
	public List<String> updatedFileCheck(){
		List<String> fileUpdated = new LinkedList<String>();
		//Authenticate to dropbox a account
		authenticate();
		Entry existingEntry = null;
		try {
			//Get Entry for root folder in dropbox
    		existingEntry =  api.metadata("/", 0, null, true, null);
    		//For every subfolder in root directory
    		for (Entry entryFolders : existingEntry.contents){
				Entry entryFolder = api.metadata("/" + entryFolders.fileName(),0,null,true,null);
				//For every file in subfolder
            	for(Entry e : entryFolder.contents){
            		//Check to se if it exists,
            		if (!e.isDeleted){
            			File localFile = new File(this.tempRepository + e.fileName());
            			Date localDate = new Date(localFile.lastModified());
            			Date dropboxDate = new Date(e.modified);
            			//If last modified for drobox file is later than for file in repository, add to return list
            			if(dropboxDate.after(localDate)){
							fileUpdated.add(e.path.substring(e.path.lastIndexOf("/")));
            			}
            		}
        		}
        	}
		}catch (DropboxException e) {
			String errorMsg = "Something went wrong while getting metadata. " + dropboxAccept;
    		logger.error(errorMsg,e);
    		throw new RuntimeException(errorMsg,e);
		}
		return fileUpdated;
	}

	/**
	 * Commits the files in the list from dropbox to the repository and then deletes them from dropbox
	 * 
	 * @param files list of file paths to files which to commit
	 */
	public void commitFiles(List<CmsItemId> cmsItems){
		//Authenticate to dropbox a account
		authenticate();
        FileOutputStream outputStream = null;
        for(CmsItemId cmsId : cmsItems){
        	try{
        		String fileN = cmsId.getRelPath().getPath();
        		File reposFolder = new File(tempRepository);
        		File lockFile = new File(reposFolder,fileN + ".lock");
        		String dropboxPath = "";
        		if(lockFile.exists()){
        			dropboxPath = workareaOperations.readLockFile(lockFile);
					cmsCommit.unlock(new CmsItemPath(fileN),new CmsItemLockFile(tempRepository,new Date(),dropboxPath));
				}
			
        		//File in repository which to store change in, is set to a local temp folder (tmp/repos-test/)
        		File file = new File(tempRepository + fileN);
        		outputStream = new FileOutputStream(file);
        		//Get file from dropbox 
        		DropboxFileInfo info = api.getFile(dropboxPath, null, outputStream, null);
        		//Get entry for this dropbox file   
        		Entry fileMetadata = info.getMetadata();
        		//Delete file from dropbox     	
        		api.delete(dropboxPath);
        		//Get entry for files parent folder
        		Entry parentFolder = api.metadata(fileMetadata.parentPath(),0,null,true,null);
        		//Checks if parent folder is empty, and delete folder if it is
        		if(parentFolder.contents.isEmpty())
        			api.delete(fileMetadata.parentPath());
        	}catch (DropboxException de) {
        		String errorMsg = "Something went wrong while downloading from dropbox"  + dropboxAccept;
        		logger.error(errorMsg,de);
        		throw new RuntimeException(errorMsg,de);
        	}catch (FileNotFoundException fe) {
        		String errorMsg = "File not found";
        		logger.error(errorMsg,fe);
        		throw new RuntimeException(errorMsg,fe);
        	}finally {
        		if(outputStream != null) {
        			try {
        				outputStream.close();
        			}catch(IOException IOe) {
        				String errorMsg = "Something went wrong with outputstream";
        				logger.info(errorMsg,IOe);
        				throw new RuntimeException(errorMsg,IOe);
        			}
        		}
        	}
		}
	}
		
	public void initializeDropbox(DropboxTokenStore tokenStore){
		this.tokenStore = tokenStore;
		// Initialize the session
		this.appKeys = new AppKeyPair(KEY, SECRET);
		WebAuthSession session = new WebAuthSession(appKeys, ACCESS_TYPE);
		// Initialize DropboxAPI object
		this.api = new DropboxAPI<WebAuthSession>(session);
		try{
			acceptUrl = api.getSession().getAuthInfo().url;
		}catch (DropboxException e) {
			String errorMsg = "Could not retreive AuthInfo from session";
			logger.info(errorMsg,e);
			throw new RuntimeException(errorMsg,e);
		}		
	}
	
	private void unlock(List<CmsItemId> cmsItems){
		for(CmsItemId cmsId : cmsItems){
			cmsCommit.unlock(cmsId.getRelPath(), new CmsItemLockFile(tempRepository,new Date(),cmsId.getRelPath().getPath()));
		}
	}

	public String getAcceptUrl(){
		return acceptUrl;
	}


}