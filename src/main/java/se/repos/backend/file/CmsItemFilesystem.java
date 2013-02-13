/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.backend.file;

import java.io.File;
import java.io.OutputStream;

import se.simonsoft.cms.item.Checksum;
import se.simonsoft.cms.item.CmsItem;
import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.CmsItemKind;
import se.simonsoft.cms.item.CmsItemLock;
import se.simonsoft.cms.item.RepoRevision;
import se.simonsoft.cms.item.properties.CmsItemProperties;

public class CmsItemFilesystem implements CmsItem {

	/**
	 * Until locking is implemented we need the local file reference directly in {@link WorkAreaCmsItemAdditionalOperations}.
	 */
	File getFile() {
		throw new UnsupportedOperationException("not implemented");
	}
	
	@Override
	public Checksum getChecksum() {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void getContents(OutputStream arg0)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public CmsItemId getId() {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public CmsItemKind getKind() {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public CmsItemProperties getProperties() {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public RepoRevision getRevisionChanged() {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public String getStatus() {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public CmsItemLock getLock() {
		throw new UnsupportedOperationException("not implemented");
	}

}
