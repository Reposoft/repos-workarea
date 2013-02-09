/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.backend.file;

import java.io.File;
import java.util.Set;

import se.simonsoft.cms.item.CmsItem;
import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.RepoRevision;
import se.simonsoft.cms.item.info.CmsConnectionException;
import se.simonsoft.cms.item.info.CmsItemLookup;
import se.simonsoft.cms.item.info.CmsItemNotFoundException;

/**
 * The first step towards a local filesystem backend for Repos.
 * 
 * Impl that reads file paths under a local directory as if it was a subversion repo.
 * 
 * {@link #getItem(CmsItemId)}.{@link CmsItem#getRevisionChanged()}.{@link RepoRevision#getDate()} returns last modified date.
 */
public class CmsItemLookupFilesystem implements CmsItemLookup {
	
	/**
	 * @param root corresponding to repository root in subversion
	 */
	public CmsItemLookupFilesystem(File root) {
	}
	
	@Override
	public CmsItem getItem(CmsItemId id) throws CmsConnectionException,
			CmsItemNotFoundException {
		id.getRelPath(); // to get path from local storage parent folder
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public Set<CmsItem> getImmediates(CmsItemId parent)
			throws CmsConnectionException, CmsItemNotFoundException {
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

}
