/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.backend.file;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.CmsRepository;
import se.simonsoft.cms.item.RepoRevision;
import se.simonsoft.cms.item.commit.CmsCommit;
import se.simonsoft.cms.item.commit.CmsCommitChangeset;
import se.simonsoft.cms.item.commit.FileModification;
import se.simonsoft.cms.item.impl.CmsItemIdUrl;
import se.simonsoft.cms.item.info.CmsItemNotFoundException;

public class CmsCommitFilesystemTest {

	private CmsRepository repo = new CmsRepository("http://host/repo1");
	private File root = null;
	private CmsCommit commit = null;
	
	@Before
	public void setUp() throws IOException {
		root = File.createTempFile("test-" + this.getClass().getName(), "dir");
		root.delete();
		root.mkdir();
		
		commit = new CmsCommitFilesystem(repo, root);
	}
	
	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(root);
	}

	@Test
	public void testRunFileModification() throws IOException {
		// create "existing" file
		File file = new File(root, "file1.txt");
		CmsItemId fileId = new CmsItemIdUrl(repo, "/file1.txt");
		FileUtils.writeStringToFile(file, "A\n");
		
		// revision is last modified date TODO do we require "revision" so we can detect conflicts?
		Date fileLastModified = new Date(file.lastModified());
		RepoRevision rev = new RepoRevision(-1, fileLastModified);
		
		CmsCommitChangeset changeset = new CmsCommitChangeset();
		changeset.add(new FileModification(fileId.getRelPath(), rev, null, new ByteArrayInputStream("A\nB\n".getBytes())));
		commit.run(changeset);
		
		assertEquals("Should have written file", "A\nB\n", FileUtils.readFileToString(file));
	}

	@Test
	public void testRunFileModificationNotExisting() throws IOException {
		CmsItemId fileId = new CmsItemIdUrl(repo, "/file1.txt");
		RepoRevision rev = new RepoRevision(-1, new Date());
		
		CmsCommitChangeset changeset = new CmsCommitChangeset();
		changeset.add(new FileModification(fileId.getRelPath(), rev, null, new ByteArrayInputStream("x".getBytes())));
		try {
			commit.run(changeset);
			fail("Should bail out at attempt to modify non existing file");
		} catch (CmsItemNotFoundException e) {
			assertEquals(fileId.getRelPath(), e.getPath());
		}
	}	
	
	@Test
	public void testRunFileModificationsWrongRev() {
		// TODO when rev is given we should verify that it matches last modified date, or else someone else might have modified it
	}
	
	@Test
	public void testRunFileModificationsWithBase() {
		// TODO when base is given we should verify that it matches existing content, probably using checksum
	}
	
	@Test
	public void testRunFileModificationsWithLock() {
		// test without lock information, should result in CmsItemLockedException
		
		// test keep lock
		
		// test don't keep
	}

}
