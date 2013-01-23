/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.workarea.local;

import se.repos.workarea.WorkArea;

import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import se.repos.lgr.Lgr;
import se.repos.lgr.LgrFactory;


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
    		}
		}

		/**
		*List files in local temporary repository
		*
		*@return List Names of files in repository
		*/
		public List getFileList(){
			File localDirectory = new File(tempRepository);
			String[] fileList = localDirectory.list();
			if(fileList == null)
				throw new NullPointerException("Null Value");
		return Arrays.asList(fileList);
		}


		/**
		*Checks if files in local folder has been updated 
		*
		*@return List Names of files that has been updated
		*/
		public List<String> updatedFileCheck(){
			List<String> fileUpdated = new LinkedList<String>();
			File root = new File(localFolder);
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
			return fileUpdated;
		}



		/**
		*Commits files from a local folder to a temporary local repository
		*
		*@param files List of file names to be commited to repository
		*/
		public void commitFiles(List<String> files){
			for(String fileName : files){
				File file;
				File root = new File(localFolder);
				search : for(File subFolder : Arrays.asList(root.listFiles())){
					File[] listFiles = subFolder.listFiles();
					if(listFiles != null){
						for(File localFile : Arrays.asList(listFiles)){
							if(localFile.getName().equals(fileName)){
								file = new File(localFolder+subFolder.getName() +"/" + fileName);
								File reposFile = new File(tempRepository+fileName);
								if(reposFile.exists()){
									writeToFile(file,reposFile);
									file.delete();
									if(subFolder.list().length == 0)
										subFolder.delete();
								}
									break search;
							}
						}
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
				logger.info("Something went wrong while copying file");
			}finally{
				try{
					inStream.close();
					outStream.close();
				}catch(IOException ioE){
					logger.info("Somthing went wrong while closing stream");
				}
			}
		}


}
