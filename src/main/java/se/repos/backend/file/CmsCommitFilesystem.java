/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.backend.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
	public CmsItemLock lock(String message, CmsItemPath pathItem)
			throws CmsItemLockedException {
		File lockFile = new File(root,pathItem.getPath() +".lock");
		if(lockFile.exists()){
			throw new CmsItemLockedException(repository,pathItem);
		}
		try {
			FileWriter fw = new FileWriter(lockFile);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(message);
			bw.close();
		} catch (IOException e) {
			String errorMsg = "Someting went wrong while writing to lock file";
			logger.error(errorMsg,e);
			throw new RuntimeException(errorMsg,e);
		}
		return new CmsItemLockFile(repository.getHost(), new Date(),message);
	}

	@Override
	public void unlock(CmsItemPath pathItem, CmsItemLock lockItem) {
		File lockFile = new File(root,pathItem.getPath()+".lock");
		if(lockFile.exists()){
			lockFile.delete();
		}
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
		if (!file.getFile().exists()) {
			throw new CmsItemNotFoundException(file.getId());
		}
		InputStream incoming = change.getWorkingFile();
		FileOutputStream out;
		try {
			out = new FileOutputStream(file.getFile());
		} catch (FileNotFoundException e) {
			throw new RuntimeException("File went missing during the operation"); // There'll probably be lots of such cases for the filesystem impls, should we define a conflict exception?
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
