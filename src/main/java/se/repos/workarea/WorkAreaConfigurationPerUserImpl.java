/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.workarea;

import javax.inject.Inject;

import se.repos.authproxy.ReposCurrentUser;

public class WorkAreaConfigurationPerUserImpl implements WorkAreaConfiguration {

	private ReposCurrentUser reposCurrentUser;
	
	@Inject public void setReposCurrentUser(ReposCurrentUser reposCurrentUser) {
		this.reposCurrentUser = reposCurrentUser;
	}	
	
	@Override
	public WorkArea getWorkArea() {
		String username = reposCurrentUser.getUsername();
		throw new UnsupportedOperationException("Not implemented");
	}

}
