/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.backend.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.simonsoft.cms.item.CmsItemLock;
import se.simonsoft.cms.item.CmsItemPath;
import se.simonsoft.cms.item.CmsRepository;
import se.simonsoft.cms.item.RepoRevision;
import se.simonsoft.cms.item.commit.CmsCommit;
import se.simonsoft.cms.item.commit.CmsCommitChange;
import se.simonsoft.cms.item.commit.CmsCommitChangeset;
import se.simonsoft.cms.item.commit.CmsItemLockedException;
import se.simonsoft.cms.item.commit.FileModification;
import se.simonsoft.cms.item.info.CmsItemNotFoundException;

/**
 * There's probably little use in implementing this now until it supports (un)locking,
 * because we need the specialized {@link WorkAreaCmsItemAdditionalOperations} anyway.
 */
public class CmsCommitFilesystem implements CmsCommit {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private CmsRepository repository = null;
	private File root;

	/**
	 * @param root corresponding to repository root in subversion
	 */
	@Inject
	public CmsCommitFilesystem(CmsRepository repository, @Named("root") File repositoryRoot) {
		this.repository = repository;
		this.root = repositoryRoot;
	}	
	
	@Override
	public CmsItemLock lock(String arg0, CmsItemPath arg1)
			throws CmsItemLockedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void unlock(CmsItemPath arg0, CmsItemLock arg1) {
		// TODO Auto-generated method stub
		
	}	
	
	@Override
	public RepoRevision run(CmsCommitChangeset changeset) {
		Date now = new Date();
		for (CmsCommitChange change : changeset) {
			run(change);
		}
		return new RepoRevision(-1, now);
	}
	
	private CmsItemFilesystem item(CmsItemPath path) throws CmsItemNotFoundException {
		File f = new File(root, path.getPathTrimmed());
		return new CmsItemFilesystem(f, repository, path);
	}
	
	void run(CmsCommitChange change) {
		if (change instanceof FileModification) {
			handle((FileModification) change);
		} else {
			throw new UnsupportedOperationException("Filesystem modification not supported for change type " + change.getClass().getSimpleName());
		}
	}
	
	void handle(FileModification change) {
		CmsItemFilesystem file = item(change.getPath());
		InputStream incoming = change.getWorkingFile();
		FileOutputStream out;
		try {
			out = new FileOutputStream(file.getFile());
		} catch (FileNotFoundException e) {
			// This happens if the dir does not exist but the file will be created if it does
			throw new CmsItemNotFoundException(file.getId());
		}
		try {
			byte[] buf = new byte[4096];
			int i = 0;
			while ((i = incoming.read(buf)) != -1) {
				out.write(buf, 0, i);
			}
		} catch (IOException e) {
			throw new RuntimeException("File write failed for item " + file, e);
		} finally {
			try {
				incoming.close();
			} catch (IOException e) {
				logger.warn("Failed to close source stream for modification of " + file);
			}			
			try {
				out.close();
			} catch (IOException e) {
				logger.warn("Failed to close output stream for modification of " + file);
			}
		}
	}

}
