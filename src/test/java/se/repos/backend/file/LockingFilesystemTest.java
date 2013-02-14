package se.repos.backend.file;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import se.simonsoft.cms.item.CmsItem;
import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.CmsRepository;
import se.simonsoft.cms.item.commit.CmsCommit;
import se.simonsoft.cms.item.commit.CmsItemLockedException;
import se.simonsoft.cms.item.impl.CmsItemIdUrl;
import se.simonsoft.cms.item.impl.CmsRepositoryId;
import se.simonsoft.cms.item.info.CmsItemLookup;

/**
 * Tests CmsCommit and CmsItemLookup lock emulation for plain filesystem.
 */
public class LockingFilesystemTest {

	private CmsRepository repo = new CmsRepository("http://host/repo1");
	private File root = null;
	private CmsItemLookup lookup = null;
	private CmsCommit commit = null;
	
	@Before
	public void setUp() throws IOException {
		root = File.createTempFile("test-" + this.getClass().getName(), "dir");
		root.delete();
		root.mkdir();
		lookup = new CmsItemLookupFilesystem(repo, root);
		commit = new CmsCommitFilesystem(repo, root);
	}
	
	@Test
	public void testLock() throws IOException {
		File file = new File(root, "file1.txt");
		file.createNewFile();
		CmsItemId fileId = new CmsItemIdUrl(repo, "/file.txt");
		
		// lock
		commit.lock("Locking\ntest", fileId.getRelPath());
		File fileLock = new File(root, "file1.txt.lock");
		assertTrue("Lock should create file with suffix .lock", fileLock.exists());
		// if we want to emulate svn's tokens there could be more content
		assertEquals("Lock file should contain message", "Locking\ntest", FileUtils.readFileToString(fileLock));
		
		// lock again
		try {
			commit.lock("again", fileId.getRelPath());
			fail("Locking should fail when file is already locked");
		} catch (CmsItemLockedException e) {
			// expected
		}
		
		// now lookup should not list the lock file
		Set<CmsItem> list = lookup.getImmediates(new CmsRepositoryId(repo));
		assertEquals("should not list the lock file as a regular file", 1, list.size());
		
		CmsItem fileItem = list.iterator().next();
		assertNotNull("lookup should detect locks", fileItem.getLock());
		assertEquals("Lookup should read lock message", "Locking\ntest", fileItem.getLock().getComment());
		
		// unlock
		commit.unlock(fileId.getRelPath(), fileItem.getLock());
		assertFalse("Should have removed lock file", fileLock.exists());
		
		// unlock again, result TBD
	}
	
	@Test
	public void testUnlockWithWrongToken() {
		// lock tokens not needed yet
	}
	
}
