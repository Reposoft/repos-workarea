/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.workarea.local;

import se.repos.workarea.WorkArea;

import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Date;

import se.repos.backend.file.CmsCommitFilesystem;
import se.repos.backend.file.CmsItemLockFile;
import se.repos.backend.file.CmsItemLookupFilesystem;
import se.repos.backend.file.WorkAreaCmsItemAdditionalOperations;
import se.repos.lgr.Lgr;
import se.repos.lgr.LgrFactory;
import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.CmsItemPath;
import se.simonsoft.cms.item.CmsRepository;
import se.simonsoft.cms.item.RepoRevision;
import se.simonsoft.cms.item.commit.CmsCommitChangeset;
import se.simonsoft.cms.item.commit.CmsItemLockedException;
import se.simonsoft.cms.item.commit.FileModification;
import se.simonsoft.cms.item.impl.CmsRepositoryId;


public class WorkAreaLocal implements WorkArea {

		public static final Lgr logger = LgrFactory.getLogger();
		private String localFolder;
		private String tempRepository;
		private CmsItemLookupFilesystem cmsLookup;
		private CmsCommitFilesystem cmsCommitRepo;
		private CmsCommitFilesystem cmsCommitLocal;
		private CmsRepository repo;
		private WorkAreaCmsItemAdditionalOperations workareaOperations;


		public WorkAreaLocal(){
			this.localFolder = "tmp/testLocalfolder/";
			this.tempRepository = "tmp/repos-test/";
			this.repo = new CmsRepository("http://localhost/svn/testrepo");
			File repoRoot = new File(this.tempRepository);
			File localRoot = new File(this.localFolder);
			cmsLookup = new CmsItemLookupFilesystem(this.repo,repoRoot);
			this.workareaOperations = new WorkAreaCmsItemAdditionalOperations();
			this.cmsCommitRepo = new CmsCommitFilesystem(this.repo,repoRoot);
			this.cmsCommitLocal = new CmsCommitFilesystem(this.repo,localRoot);
		}



		/**
		*Uploads files from local folder to local temporary repository
		*
		*@param folderName Name of the folder which to upload the files to
		*@param pathList paths to the files which to upload
		*/
		public void uploadFile(String folderName, List<CmsItemId> cmsItems){
			CmsCommitChangeset changeSet = new CmsCommitChangeset();
			File folder = new File(localFolder+folderName);
			List<CmsItemId> lockItems = new LinkedList<CmsItemId>();
			if(!folder.exists())
				folder.mkdir();
			
			for(CmsItemId cmsId : cmsItems){
				String path = cmsId.getRelPath().getName();
				if(path.startsWith("/")){
					path = path.substring(1,path.length());
				}
				File source = new File(this.tempRepository + path);
				String sourceName = source.getName();
				String localPath = folderName + "/" + sourceName;
				try{
					cmsCommitRepo.lock(localFolder+localPath, cmsId.getRelPath());
					lockItems.add(cmsId);
				}catch(CmsItemLockedException cms){
					unlock(lockItems);
					String errorMsg = cms.getMessage();
		    		logger.error(errorMsg,cms);
		    		throw new RuntimeException(errorMsg,cms);
				}
			}
			
			for(CmsItemId cmsId : cmsItems){
				try {
					String path = cmsId.getRelPath().getName();
					if(path.startsWith("/")){
						path = path.substring(1,path.length());
					}
					File source = new File(this.tempRepository + path);
					String sourceName = source.getName();
					File target = new File(folder,sourceName);
					target.createNewFile();
					Date fileLastModified = new Date(target.lastModified());
					RepoRevision rev = new RepoRevision(-1, fileLastModified);
					String localPath = folderName + "/" + sourceName;
					changeSet.add(new FileModification(new CmsItemPath("/"+localPath),rev,null,new FileInputStream(source)));

					long lDateTime = new Date().getTime();
					source.setLastModified(lDateTime);							
				} catch (FileNotFoundException e) {
					unlock(cmsItems);
					String errorMsg =  "Could not find file: " +e;
					logger.error(errorMsg,e);
					throw new RuntimeException(errorMsg,e);
				}catch(IOException e){
					String errorMsg =  "Problem while creating file " +e;
					logger.error(errorMsg,e);
					throw new RuntimeException(errorMsg,e);						
				}		
			}
				
			cmsCommitLocal.run(changeSet);
	
    	}
		
		
		

		/**
		*List files in local temporary repository
		*
		*@return List Names of files in repository
		*/
		public List<String> getFileList(){
			CmsRepositoryId cmsId = new CmsRepositoryId(repo);
			return this.workareaOperations.getRepositoryFileNames(cmsLookup.getImmediateFiles(cmsId));
		}

		/**
		*Checks if files in local folder has been updated 
		*
		*@return List Names of files that has been updated
		*/
		public List<String> updatedFileCheck(){
			List<String> fileUpdated = new LinkedList<String>();
			File root = new File(localFolder);
			if(!root.exists()){
				try {
					throw new FileNotFoundException();
				} catch (FileNotFoundException e) {
					String errorMsg = "Can't find local folder: " + localFolder; 
					logger.error(errorMsg,e);
					throw new RuntimeException(errorMsg,e);
				}
			}else{
				for(File subFolder : Arrays.asList(root.listFiles())){
					File[]	listFiles = subFolder.listFiles();
					if(listFiles!=null){
						for(File localFile : Arrays.asList(listFiles)){
							File reposFile = new File(tempRepository + localFile.getName());
							if(localFile.lastModified() > reposFile.lastModified())
								fileUpdated.add("/" + localFile.getName());
						}	
					}
				}
			}
			return fileUpdated;
			
		}

		/**
		*Commits files from a local folder to a temporary local repository
		*
		*@param files List of file names to be commited to repository
		*/
		public void commitFiles(List<CmsItemId> cmsItems){
			CmsCommitChangeset changeSet = new CmsCommitChangeset();
			for(CmsItemId cmsId : cmsItems){
				File reposFolder = new File(tempRepository);
				String path = cmsId.getRelPath().getPath();
				File lockFile = new File(reposFolder,path + ".lock");
				String localPath = "";
				if(lockFile.exists()){
					try{
						localPath = workareaOperations.readLockFile(lockFile);
						File source = new File(localPath);
						File target = new File(tempRepository + path);
						if(source.exists() && target.exists()){
							File parentFolder = source.getParentFile();
							Date fileLastModified = new Date(target.lastModified());
							RepoRevision rev = new RepoRevision(-1, fileLastModified);
							changeSet.add(new FileModification(cmsId.getRelPath(),rev,null,new FileInputStream(source)));
							source.delete();
							if(parentFolder.list().length == 0)
								parentFolder.delete();
							cmsCommitRepo.unlock(new CmsItemPath(path),new CmsItemLockFile(tempRepository,new Date(),localPath));
						}
					}catch(FileNotFoundException e){
						String errorMsg =  "Could not find file: " +e;
						logger.error(errorMsg,e);
						throw new RuntimeException(errorMsg,e);
					}

				}
			}
			cmsCommitRepo.run(changeSet);
		}
		
		private void unlock(List<CmsItemId> cmsItems){
			for(CmsItemId cmsId : cmsItems){
				cmsCommitRepo.unlock(cmsId.getRelPath(), new CmsItemLockFile(tempRepository,new Date(),cmsId.getRelPath().getPath()));
			}
		}
		
		public String getLocalFolder(){
			return this.localFolder;
		}
		
		public void setLocalFolder(String localFolder){
			this.localFolder = localFolder;
		}
		
		public String getLocalRepository(){
			return this.tempRepository;
		}

		public void setLocalRepository(String tempRepository){
			this.tempRepository = tempRepository;
		}
}
