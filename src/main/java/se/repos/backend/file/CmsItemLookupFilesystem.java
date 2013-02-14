/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.backend.file;

import java.io.File;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import se.simonsoft.cms.item.CmsItem;
import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.CmsRepository;
import se.simonsoft.cms.item.RepoRevision;
import se.simonsoft.cms.item.info.CmsConnectionException;
import se.simonsoft.cms.item.info.CmsItemLookup;
import se.simonsoft.cms.item.info.CmsItemNotFoundException;
import se.simonsoft.cms.item.info.CmsLockQuery;

/**
 * The first step towards a local filesystem backend for Repos.
 * 
 * Impl that reads file paths under a local directory as if it was a subversion repo.
 * 
 * {@link #getItem(CmsItemId)}.{@link CmsItem#getRevisionChanged()}.{@link RepoRevision#getDate()} returns last modified date.
 */
public class CmsItemLookupFilesystem implements CmsItemLookup {
	
	private CmsRepository repository = null;
	private File root;

	/**
	 * @param root corresponding to repository root in subversion
	 */
	@Inject
	public CmsItemLookupFilesystem(CmsRepository repository, @Named("root") File repositoryRoot) {
		this.repository = repository;
		this.root = repositoryRoot;
	}
	
	@Override
	public CmsItem getItem(CmsItemId id) throws CmsConnectionException,
			CmsItemNotFoundException {
		File f = new File(root, id.getRelPath().getPathTrimmed());
		CmsItem item = new CmsItemFilesystem(f, id);
		return item;
	}

	@Override
	public Set<CmsItem> getImmediates(CmsItemId parent)
			throws CmsConnectionException, CmsItemNotFoundException {
		// for each file in dir do
			//parent.withRelPath(parent.getRelPath().append(filename));
		throw new UnsupportedOperationException("not implemented");
	}	
	
	@Override
	public Set<CmsItemId> getImmediateFolders(CmsItemId parent)
			throws CmsConnectionException, CmsItemNotFoundException {
		throw new UnsupportedOperationException("Out of scope in first iteration");
	}

	@Override
	public Set<CmsItemId> getImmediateFiles(CmsItemId parent)
			throws CmsConnectionException, CmsItemNotFoundException {
		throw new UnsupportedOperationException("Out of scope in first iteration");
	}

	@Override
	public Iterable<CmsItemId> getDescendants(CmsItemId parent) {
		throw new UnsupportedOperationException("Out of scope in first iteration");
	}

	@Override
	public Set<CmsItemId> getLocked(CmsLockQuery query) {
		throw new UnsupportedOperationException("not implemented");
	}

}
