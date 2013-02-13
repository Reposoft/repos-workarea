/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.backend.file;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

import se.simonsoft.cms.item.CmsItemLock;
import se.simonsoft.cms.item.CmsItemPath;
import se.simonsoft.cms.item.CmsRepository;
import se.simonsoft.cms.item.RepoRevision;
import se.simonsoft.cms.item.commit.CmsCommit;
import se.simonsoft.cms.item.commit.CmsCommitChangeset;
import se.simonsoft.cms.item.commit.CmsItemLockedException;
import se.simonsoft.cms.item.commit.FileModification;

/**
 * There's probably little use in implementing this now until it supports (un)locking,
 * because we need the specialized {@link WorkAreaCmsItemAdditionalOperations} anyway.
 */
public class CmsCommitFilesystem implements CmsCommit {

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
		// TODO 
		throw new UnsupportedOperationException("not implemented");
	}
	
	void handle(FileModification change) {
		
	}

}
