/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.backend.file;

import static org.junit.Assert.*;

import se.repos.backend.file.CmsItemLookupFilesystem;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.simonsoft.cms.item.CmsItem;
import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.CmsItemPath;
import se.simonsoft.cms.item.CmsRepository;
import se.simonsoft.cms.item.impl.CmsItemIdUrl;
import se.simonsoft.cms.item.info.CmsItemLookup;
import se.simonsoft.cms.item.info.CmsItemNotFoundException;

public class CmsItemLookupFilesystemTest {

	private CmsRepository repo = new CmsRepository("http://host/repo1");
	private File root = null;
	private CmsItemLookup lookup = null;
	
	@Before
	public void setUp() throws IOException {
		root = File.createTempFile("test-" + this.getClass().getName(), "dir");
		root.delete();
		root.mkdir();
		lookup = new CmsItemLookupFilesystem(repo, root);
	}
	
	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(root);
	}
	
	@Test
	public void testGetItem() throws IOException {
		new File(root, "file1.txt").createNewFile();
		CmsItemId id1 = new CmsItemIdUrl(repo, new CmsItemPath("/file1.txt"));
		CmsItem file1 = lookup.getItem(id1);
		assertNotNull("Should find a file", file1);
		assertEquals("file1.txt", file1.getId().getRelPath().getName());
	}
	
	@Test(expected=CmsItemNotFoundException.class)
	public void testGetItemMissing() throws IOException {
		lookup.getItem(new CmsItemIdUrl(repo, new CmsItemPath("/file1.txt")));
	}

	@Test
	public void testGetImmediates() throws IOException {
		File file1 = new File(root, "file1.txt");
		file1.createNewFile();
		File folder = new File(root, "folder");
		CmsItemId folderId = new CmsItemIdUrl(repo, "/folder");
		
		try {
			lookup.getImmediates(folderId);
			fail("Should report non existing parent before listing is attempted");
		} catch (CmsItemNotFoundException e) {
			assertEquals("should indicate the attempted item", folderId.getRelPath(), e.getPath());
		}
		
		folder.mkdir();
		assertEquals("Should find 0 items in empty folder",Collections.emptySet(),lookup.getImmediates(folderId));
		
		Set<CmsItem> list = lookup.getImmediates(repo.getItemId());
		assertEquals("should find two items under root", 2, list.size());

		Iterator<CmsItem> it = list.iterator();
		CmsItem item1 = it.next();
		/*CmsItem item2 = it.next();
		assertEquals("iteration order is defined by backend but probably when creation sequence and alphabetical order is the same we'll get it that way", 
				"file1.txt", item1.getId().getRelPath().getName());*/
		assertEquals("revision should contain last modified timestamp of the corresponding file",
				file1.lastModified(), item1.getRevisionChanged().getDate().getTime());
		
		/*assertEquals("folder", item2.getId().getRelPath().getName());
		assertEquals(CmsItemKind.Folder, item2.getKind());*/
	}

}
