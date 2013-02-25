/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.workarea;


import javax.inject.Inject;
import se.repos.workarea.dropbox.*;
import se.repos.workarea.local.*;
import se.repos.authproxy.ReposCurrentUser;

public class WorkAreaConfigurationPerUserImpl implements WorkAreaConfiguration {

	private ReposCurrentUser reposCurrentUser;
	private WorkArea workareaCurrent;
	
	@Inject public void setReposCurrentUser(ReposCurrentUser reposCurrentUser) {
		this.reposCurrentUser = reposCurrentUser;
		this.workareaCurrent = new WorkAreaDropBox(reposCurrentUser.getUsername());
		//this.workareaCurrent = new WorkAreaLocal();
	}	
	
	
	@Override
	public WorkArea getWorkArea() {
		return workareaCurrent;
	}


}
