/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.workarea;

import javax.inject.Inject;

import se.repos.authproxy.ReposCurrentUser;
import se.repos.workarea.dropbox.WorkAreaDropBox;
import se.simonsoft.cms.item.info.CmsItemLookup;

public class WorkAreaConfigurationPerUserImpl implements WorkAreaConfiguration {

	private ReposCurrentUser reposCurrentUser;
	private CmsItemLookup lookup;
	
	@Inject public void setReposCurrentUser(ReposCurrentUser reposCurrentUser) {
		this.reposCurrentUser = reposCurrentUser;
	}
	
	@Inject public void setCmsItemLookup(CmsItemLookup lookup) {
		this.lookup = lookup;
	}
	
	@Override
	public WorkArea getWorkArea() {
		String username = reposCurrentUser.getUsername();
		// types are svn, svn-wc or filesystem, currently we use filesystem when evaluating this module
		CmsItemLookup lookupPerRepositoryType = lookup;
		// configuration per user not in scope for first iteration
		WorkArea workareaCurrent = new WorkAreaDropBox(); // TODO get services from dependency injection
		return workareaCurrent;
	}

}
