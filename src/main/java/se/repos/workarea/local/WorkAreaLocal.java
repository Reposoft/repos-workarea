/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.workarea.local;

import se.repos.workarea.WorkArea;

import java.util.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Date;

import se.repos.backend.file.CmsItemFilesystem;
import se.repos.backend.file.CmsItemLookupFilesystem;
import se.repos.lgr.Lgr;
import se.repos.lgr.LgrFactory;
import se.simonsoft.cms.item.CmsItem;
import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.CmsItemPath;
import se.simonsoft.cms.item.CmsRepository;
import se.simonsoft.cms.item.impl.CmsItemIdUrl;


public class WorkAreaLocal implements WorkArea {

		public static final Lgr logger = LgrFactory.getLogger();
		private String localFolder;
		private String tempRepository;


		public WorkAreaLocal(){
			this.localFolder = "tmp/testLocalfolder/";
			this.tempRepository = "tmp/repos-test/";
		}



		/**
		*Uploads files from local folder to local temporary repository
		*
		*@param folderName Name of the folder which to upload the files to
		*@param pathList paths to the files which to upload
		*/
		public void uploadFile(String folderName, List<String> pathList){
			for(String path : pathList){
				File folder = new File(localFolder+folderName);
				if(!folder.exists())
					folder.mkdir();
				File source = new File(path);
				File target = new File(localFolder+folderName+"/"+source.getName());
				writeToFile(source,target);
				long lDateTime = new Date().getTime();
				source.setLastModified(lDateTime);
				
				File reposFolder = new File(tempRepository);
				if(reposFolder.exists() && reposFolder.isDirectory()){
					File lockFile = new File(reposFolder,source.getName() +".lock");
					try {
						lockFile.createNewFile();
						if(lockFile.exists()){
							String content = localFolder+folderName+"/"+source.getName();
							FileWriter fw = new FileWriter(lockFile.getAbsoluteFile());
							BufferedWriter bw = new BufferedWriter(fw);
							bw.write(content);
							bw.close();
						}
					} catch (IOException e) {
						String errorMsg = "Someting went wrong while writing to lock file";
						logger.error(errorMsg,e);
						throw new RuntimeException(errorMsg,e);
					}
				}
			}
				
    	}
		

		/**
		*List files in local temporary repository
		*
		*@return List Names of files in repository
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
								fileUpdated.add(localFile.getName());
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
		public void commitFiles(List<String> files){
			for(String fileName : files){
				File reposFolder = new File(tempRepository);
				File lockFile = new File(reposFolder,fileName + ".lock");
				String localPath = "";
				if(lockFile.exists());{
					try{
						FileInputStream in = new FileInputStream(lockFile);
						BufferedReader br = new BufferedReader(new InputStreamReader(in));
						localPath = br.readLine();
						br.close();
					}catch(Exception e){
						String errorMsg =  "Someting went wrong while reading lock file";
						logger.error(errorMsg,e);
						throw new RuntimeException(errorMsg,e);
					}
					File source = new File(localPath);
					File target = new File(tempRepository + fileName);
					if(source.exists() && target.exists()){
						writeToFile(source,target);
						source.delete();
						File parentFolder = source.getParentFile();
						if(parentFolder.list().length == 0)
							parentFolder.delete();
						lockFile.delete();
					}			
				}
			}
		}

		/**
		*Overwrites content from a file to another 
		*
		*@param source File which to write content from
		*@param target File which to write content to
		*/
		private void writeToFile(File source, File target){
			FileInputStream inStream = null;
			OutputStream outStream = null;
			try{
				inStream = new FileInputStream(source);
				outStream = new FileOutputStream(target);
				byte[] buffer = new byte[1024];
				int bytesRead = inStream.read(buffer);
				while(bytesRead >= 0){
					outStream.write(buffer, 0, bytesRead);
					bytesRead = inStream.read(buffer);
				}
			}catch(IOException e){
				String errorMsg = "Something went wrong while copying file";
				logger.error(errorMsg,e);
				throw new RuntimeException(errorMsg,e);
			}finally{
				try{
					inStream.close();
					outStream.close();
				}catch(IOException ioE){
					String errorMsg = "Someting went wrong while closing stream";
					logger.error(errorMsg,ioE);
					throw new RuntimeException(errorMsg,ioE);
				}catch(NullPointerException e){
					String errorMsg = "Can't find local folder: " + localFolder;
					logger.error(errorMsg,e);
					throw new RuntimeException(errorMsg,e);
				}
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
