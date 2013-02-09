/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.backend.file;

import java.io.InputStream;

import se.simonsoft.cms.item.CmsItem;

/**
 * Operations that are currently not supported in Repos CMS APIs.
 * 
 * Casts all {@link CmsItem} instances to {@link CmsItemFilesystem}.
 */
public class WorkAreaCmsItemAdditionalOperations {

	public void lock(CmsItem item) {
		throw new UnsupportedOperationException();
	}
	
	public void unlock(CmsItem item) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * 
	 * @param item to be written
	 * @param baseFile original content (null to overwrite without diffing)
	 * @param workingFile new content
	 */
	public void writeNewVersion(CmsItem item, InputStream baseFile, InputStream workingFile) {
		throw new UnsupportedOperationException();
	}
	
}
