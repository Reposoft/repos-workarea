package se.repos.backend.file;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
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
		CmsItemId id1 = new CmsItemIdUrl(repo, new CmsItemPath("file1.txt"));
		CmsItem file1 = lookup.getItem(id1);
		assertNotNull("Should find a file", file1);
		assertEquals("file1.txt", file1.getId().getRelPath().getName());
		
	}

	@Test
	public void testGetImmediates() throws IOException {
		new File(root, "file1.txt").createNewFile();
		new File(root, "file2.txt").createNewFile();
		CmsItemId root = new CmsItemIdUrl(repo, (CmsItemPath) null); // null means root
		
		Set<CmsItem> list = lookup.getImmediates(root);
		assertEquals("should find two files in root", 2, list.size());
	}

}
