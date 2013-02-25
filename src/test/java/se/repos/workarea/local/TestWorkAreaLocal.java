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

import org.junit.Before;
import org.junit.Test;

import se.repos.workarea.local.WorkAreaLocal;
import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.CmsItemPath;
import se.simonsoft.cms.item.CmsRepository;
import se.simonsoft.cms.item.impl.CmsItemIdUrl;

public class TestWorkAreaLocal {
	
	private WorkAreaLocal workarea = new WorkAreaLocal();
	private String localRepositoryPath = workarea.getLocalRepository();
	private String localFolderPath     = workarea.getLocalFolder();
	private CmsRepository repo = new CmsRepository("http://localhost/svn/testrepo");
	private List<String> filesToCommit;
	private String testUploadFolderName = "TEST-LOCAL-FOLDER";
	
	private File localRepository;
	private File localFolder;
	
	
	@Before
	public void setUp(){
		this.localRepository = new File(this.localRepositoryPath);
		this.localFolder = new File(this.localFolderPath);
		this.localRepository.mkdirs();
		this.localFolder.mkdirs();
		
		buildTestFiles(localRepository);
	}
	
	
	@Test
	public void testUploadFile(){
		
		File[] repositoryFiles = localRepository.listFiles();
		List<CmsItemId> listOfFiles = new LinkedList<CmsItemId>();
		if(repositoryFiles.length > 0){
			for(int i = 0 ; i < repositoryFiles.length ; i++){
				CmsItemPath cmsPath = new CmsItemPath("/"+repositoryFiles[i].getName());
				CmsItemIdUrl cmsUrl = new CmsItemIdUrl(repo,cmsPath);
				listOfFiles.add(cmsUrl);
			}
		workarea.uploadFile(testUploadFolderName, listOfFiles);
		File testUploadFolder = new File(this.localFolder, testUploadFolderName);
		
		assertTrue(testUploadFolder.exists());
		assertEquals(testUploadFolder.listFiles().length , listOfFiles.size());
		}	
	}
	
	@Test
	public void testUpdatedFileCheck(){
		File testUploadFolder = new File(this.localFolder,testUploadFolderName);
		
		if(!testUploadFolder.exists()){
			testUploadFolder.mkdirs();
			buildTestFiles(testUploadFolder);
		}
		
		changeModified(testUploadFolder);
		filesToCommit = workarea.updatedFileCheck();	
		assertTrue(filesToCommit.size() == testUploadFolder.list().length);	
	}


	@Test
	public void testCommitFiles(){
		File testUploadFolder = new File(this.localFolder, testUploadFolderName);
		if(!testUploadFolder.exists()){
			testUploadFolder.mkdirs();
			buildTestFiles(testUploadFolder);
			changeModified(testUploadFolder);
		}
		assertTrue(testUploadFolder.exists());
		changeModified(testUploadFolder);
			
		filesToCommit = workarea.updatedFileCheck();
		assertTrue(filesToCommit.size() > 0);


		List<CmsItemId> items = new LinkedList<CmsItemId>();
		
		for(String s : filesToCommit){
			CmsItemPath cmsPath = new CmsItemPath(s);
			CmsItemIdUrl cmsUrl = new CmsItemIdUrl(repo,cmsPath);
			items.add(cmsUrl);
		}
		
		workarea.commitFiles(items);
		assertTrue(!testUploadFolder.exists());
	}
	
	
	private void buildTestFiles(File folder){
		for(int i = 0 ; i <= 2 ; i++){
			File testFile = new File(folder,"Test"+i+".txt");
			try {
				testFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void changeModified(File folder){
		File[] listOfFiles = folder.listFiles();
		if(listOfFiles.length > 0){
			for(File localFile : Arrays.asList(listOfFiles)){
				localFile.setLastModified(System.currentTimeMillis() + 1000000000 + 10000000);
			}
		}
	}

}
