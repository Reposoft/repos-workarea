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
import java.lang.NullPointerException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Date;
import java.io.FileOutputStream;

import javax.inject.Inject;

import se.repos.lgr.Lgr;
import se.repos.lgr.LgrFactory;
import se.simonsoft.cms.item.commit.CmsCommit;
import se.simonsoft.cms.item.info.CmsItemLookup;

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
	private String acceptUrl;
	private String tempRepository;
	private String dropboxURL;
	private String dropboxAccept;

	/**
	 * @deprecated Not using abstractions
	 */
	public WorkAreaDropBox(){
		initializeDropbox();
		//Repository set to local temp folder
		this.tempRepository = "tmp/repos-test/";
		this.dropboxURL = "http://localhost:8088/repos/work/admin";
		this.dropboxAccept = "If you have not accepted this applikation for use in dropbox";
		this.dropboxAccept += "please visit this URL: " + dropboxURL +" and do so.";
	}

	/**
	 * 
	 * @param lookup
	 * @param commit
	 */
	@Inject
	public WorkAreaDropBox(CmsItemLookup lookup, CmsCommit commit) {
		// TODO use these abstractions
	}
	
	@Inject
	public void setDropboxTokenStore(/* TODO */) {
		// TODO use token store abstraction
	}

	/**
	*Re-authentication to dropbox if TOKENS file exists
	*/
	private void reAuthenticate(){
		File tokensFile = new File("TOKENS");
		List<String> list = new LinkedList<String>();
		//Try to read the TOKENS file to get drobpox key and secret
		try{
			FileInputStream in = new FileInputStream(tokensFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while((strLine = br.readLine())!= null){
				list.add(strLine);
			}
			br.close();
		}catch(Exception e){
			String errorMsg = "Something went wrong while trying to read TOKENS file";
			logger.error(errorMsg,e);
			throw new RuntimeException(errorMsg,e);
		}
		String AUTH_KEY = list.get(0);
		String AUTH_SECRET = list.get(1);
		// re-authenticate with dropbox key and secret from TOKENS file
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
		File tokensFile = new File("TOKENS");
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
		// Write tokens to file
		try {
			PrintWriter tokenWriter = new PrintWriter(tokensFile);
			tokenWriter.println(api.getSession().getAccessTokenPair().key);
			tokenWriter.println(api.getSession().getAccessTokenPair().secret);
			tokenWriter.close();
		} catch (Exception e) {
			String errorMsg = "Something went wrong while writing tokens to file.";
			logger.error(errorMsg,e);
			throw new RuntimeException(errorMsg,e);
		}

	}

	/**
	*If TOKENS file exist with dropbox key and secret then reauthenticate 
	*otherwise it is the first time and we do a first authentication
	*/
	private void authenticate(){
		//check if already authenticated
		File tokensFile = new File("TOKENS"); 
		if (tokensFile.exists()) {
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
	public void uploadFile(String folderName, List<String> pathList){
		//Authenticate to dropbox a account
		authenticate();
		for(String path : pathList){
			try{
				File source = new File(path);
				FileInputStream fis = new FileInputStream(source);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte [] buf = new byte[1024];
				for(int readNum; (readNum = fis.read(buf)) != -1;) {
			    	bos.write(buf, 0, readNum);
				}
				fis.close();
				ByteArrayInputStream inputStream2 = new ByteArrayInputStream(bos.toByteArray());
				//Upload file to dropbox folder: folderName, if folderName don't exist it is created
				api.putFile(folderName+ "/" +source.getName(), inputStream2,bos.size(), null, null);
				long lDateTime = new Date().getTime();
				source.setLastModified(lDateTime);
	
				File reposFolder = new File(tempRepository);
				if(reposFolder.exists() && reposFolder.isDirectory()){
					File lockFile = new File(reposFolder,source.getName() +".lock");
					try{
						lockFile.createNewFile();
						if(lockFile.exists()){
							String content = folderName+"/"+source.getName();
							FileWriter fw = new FileWriter(lockFile.getAbsoluteFile());
							BufferedWriter bw = new BufferedWriter(fw);
							bw.write(content);
							bw.close();
						}
					}catch (IOException e) {
						String errorMsg = "Something went wrong while writing to lock file";
						logger.error(errorMsg,e);
						throw new RuntimeException(errorMsg,e);
					}
				}
            }catch (Exception e) {
            	String errorMsg = "Something went wrong while uploading file to dropbox. " + dropboxAccept;
				logger.error(errorMsg,e);
				throw new RuntimeException(errorMsg,e);
			}
		}
	}


	/**
	*Gets a list of files for a local repository (tmp/repos-test)
	*@return List A list of the files in repository
	*/
	public List<String> getFileList(){
		File localDirectory = new File(tempRepository);
		String[] fileList = localDirectory.list();
		List<String> repositoryFiles = new LinkedList<String>();
		for(String file : Arrays.asList(fileList)){
			int index = file.lastIndexOf('.');
			if(index >= 0 && !file.substring(index+1).equals("lock"))
				repositoryFiles.add(file);
		}
		return repositoryFiles;
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
							fileUpdated.add(e.path);
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
	public void commitFiles(List<String> files){
		//Authenticate to dropbox a account
		authenticate();
        FileOutputStream outputStream = null;
        for(String s : files){
        	try{
        		String fileN = s.substring(s.lastIndexOf("/") + 1);
        		File reposFolder = new File(tempRepository);
        		File lockFile = new File(reposFolder,fileN + ".lock");
        		String dropboxPath = "";
        		if(lockFile.exists()){
        			FileInputStream in = new FileInputStream(lockFile);
        			BufferedReader br = new BufferedReader(new InputStreamReader(in));
        			dropboxPath = br.readLine();
        			br.close();
        			lockFile.delete();
        		}
			
        		//File in repository which to store change in, is set to a local temp folder (tmp/repos-test/)
        		File file = new File(tempRepository + s.substring(s.lastIndexOf("/") + 1));
        		outputStream = new FileOutputStream(file);
        		//Get file from dropbox 
        		DropboxFileInfo info = api.getFile(dropboxPath, null, outputStream, null);
        		//Get entry for this dropbox file   
        		Entry fileMetadata = info.getMetadata();
        		//Delete file from dropbox     	
        		api.delete(s);
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
        	}catch (IOException e) {
        		String errorMsg = "Problem reading .lock file";
        		logger.error(errorMsg,e);
        		throw new RuntimeException(errorMsg,e);
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
	
	
	public void initializeDropbox(){
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
		File tokens = new File("TOKENS");
		if(tokens.exists()){
			tokens.delete();
		}
	}
	
	
	public String getAcceptUrl(){
		return acceptUrl;
	}


}