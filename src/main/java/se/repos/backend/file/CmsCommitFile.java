/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.backend.file;

import se.simonsoft.cms.item.RepoRevision;
import se.simonsoft.cms.item.commit.CmsCommit;
import se.simonsoft.cms.item.commit.CmsCommitChangeset;
import se.simonsoft.cms.item.commit.FileModification;

/**
 * There's probably little use in implementing this now until it supports (un)locking,
 * because we need the specialized {@link WorkAreaCmsItemAdditionalOperations} anyway.
 */
public class CmsCommitFile implements CmsCommit {

	@Override
	public RepoRevision run(CmsCommitChangeset changeset) {
		// TODO 
		throw new UnsupportedOperationException("not implemented");
	}
	
	void handle(FileModification change) {
		
	}

}
