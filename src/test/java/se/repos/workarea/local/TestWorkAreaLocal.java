/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.workarea.local;



import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import se.repos.workarea.local.WorkAreaLocal;

public class TestWorkAreaLocal {
	
	private WorkAreaLocal workarea = new WorkAreaLocal();
	private String localRepository = workarea.getLocalRepository();
	private String localFolder     = workarea.getLocalFolder();
	private List<String> filesToCommit;
	private String testUploadFolderName = "TEST-LOCAL-FOLDER";
	
	@Test
	public void testUploadFile(){
		
		File repositoryFolder = new File(localRepository);
		if(!repositoryFolder.exists())
			repositoryFolder.mkdirs();

		if(repositoryFolder.isDirectory() && repositoryFolder.list().length == 0){
			for(int i = 0 ; i <= 2 ; i++){
				File testFile = new File(repositoryFolder,"Test"+i+".txt");
				try {
					testFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		File localFolderFile = new File(localFolder);
		if(!localFolderFile.exists()){
			localFolderFile.mkdirs();
		}
		
		
		
		assertTrue(repositoryFolder.exists() && repositoryFolder.isDirectory());
		
		File[] repositoryFiles = repositoryFolder.listFiles();
		List<String> listOfFiles = new LinkedList<String>();
		if(repositoryFiles.length > 0){
			for(int i = 0 ; i < repositoryFiles.length ; i++){
					listOfFiles.add(localRepository + repositoryFiles[i].getName());
			}
		workarea.uploadFile(testUploadFolderName, listOfFiles);
		File testUploadFolder = new File(localFolder + testUploadFolderName);
		
		assertTrue(testUploadFolder.exists());
		
		assertTrue(testUploadFolder.listFiles().length == listOfFiles.size());
		}
			
	}
	
	@Test
	public void testUpdatedFileCheck(){
		File testUploadFolder = new File(workarea.getLocalFolder() + testUploadFolderName);
		
		assertTrue(testUploadFolder.exists() && testUploadFolder.isDirectory());
		
		File[] listOfFiles = testUploadFolder.listFiles();
		if(listOfFiles.length > 0){
			for(File localFile : Arrays.asList(listOfFiles)){
				localFile.setLastModified(System.currentTimeMillis() + 1000000000);
			}
			filesToCommit = workarea.updatedFileCheck();
			
			assertTrue(filesToCommit.size() == listOfFiles.length);
		}	
	}


	@Test
	public void testCommitFiles(){
		File testUploadFolder = new File(workarea.getLocalFolder() + testUploadFolderName);	
		filesToCommit = workarea.updatedFileCheck();
		
		assertTrue(testUploadFolder.exists());
		
		workarea.commitFiles(filesToCommit);

		
		assertTrue(!testUploadFolder.exists());
	}
	
	@Test
	public void testDestinationFolderMissing() {
		// TODO there is no error message in webapp if the work area target folder does not exist. Send just appears to work.
		//log says
		//07:15:39.421 [qtp1713279871-19] INFO  s.repos.workarea.local.WorkAreaLocal - Something went wrong while copying file
		//07:15:39.421 [qtp1713279871-19] INFO  s.repos.workarea.local.WorkAreaLocal - Can't find local folder: tmp/testLocalfolder/
		
	}
	
	@Test
	public void testSelectedFilesAlreadyLocked() {
		// TODO there is no check that file being sent to work area isn't already locked
		// Ideally this should be reported when listing "repository" files
		
	}
	
	@Test
	public void testCommitFilesSomeUnchanged() {
		// TODO if each folder is a work unit I think all changes should be committed at once and unchanged files should be unlocked in repository
		// then after commit the entire work area should be deleted
		// THis might need some clarifications in the UI
		
	}
	
}
