/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.workarea.local;



import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import se.repos.workarea.local.*;

public class TestWorkAreaLocal {
	
	private WorkAreaLocal workarea = new WorkAreaLocal();
	private String localRepository = workarea.getLocalRepository();
	private String localFolder     = workarea.getLocalFolder();
	private List<String> filesToCommit;
	private String testUploadFolderName = "TEST-LOCAL-FOLDER";
	private String webAppPathFromTest = "../repos-workarea-webapp/";

	@Test
	public void testLocalFolder() {
		File fileFolder = new File(webAppPathFromTest + localFolder);
		assertTrue(fileFolder.exists());
		assertTrue(fileFolder.isDirectory());
	}
	
	@Test
	public void testUploadFile(){
		workarea.setLocalRepository(webAppPathFromTest + localRepository); 
		workarea.setLocalFolder(webAppPathFromTest + localFolder);
		File repositoryFolder = new File(webAppPathFromTest + localRepository);
		
		assertTrue(repositoryFolder.exists() && repositoryFolder.isDirectory());
		
		File[] repositoryFiles = repositoryFolder.listFiles();
		List<String> listOfFiles = new LinkedList<String>();
		if(repositoryFiles.length > 0){
			for(int i = 0 ; i < repositoryFiles.length ; i++){
					listOfFiles.add(workarea.getLocalRepository() + repositoryFiles[i].getName());
			}
		workarea.uploadFile(testUploadFolderName, listOfFiles);
		File testUploadFolder = new File(workarea.getLocalFolder() + testUploadFolderName);
		
		assertTrue(testUploadFolder.exists());
		
		assertTrue(testUploadFolder.listFiles().length == listOfFiles.size());
		}
			
	}
	
	@Test
	public void testUpdatedFileCheck(){
		workarea.setLocalRepository(webAppPathFromTest + localRepository); 
		workarea.setLocalFolder(webAppPathFromTest + localFolder);
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
		workarea.setLocalRepository(webAppPathFromTest + localRepository); 
		workarea.setLocalFolder(webAppPathFromTest + localFolder);
		File testUploadFolder = new File(workarea.getLocalFolder() + testUploadFolderName);	
		filesToCommit = workarea.updatedFileCheck();
		
		assertTrue(testUploadFolder.exists());
		
		workarea.commitFiles(filesToCommit);

		
		assertTrue(!testUploadFolder.exists());
	}
}
