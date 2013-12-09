/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.workarea;

import javax.inject.Inject;

import se.repos.authproxy.ReposCurrentUser;
import se.repos.workarea.dropbox.WorkAreaDropBox;

public class WorkAreaConfigurationPerUserImpl implements WorkAreaConfiguration {

    private ReposCurrentUser reposCurrentUser;
    private WorkArea workareaCurrent;

    @Inject
    public void setReposCurrentUser(ReposCurrentUser reposCurrentUser) {
        this.reposCurrentUser = reposCurrentUser;
        // TODO Use reposCurrentUser here.
        this.workareaCurrent = new WorkAreaDropBox();
    }

    @Override
    public WorkArea getWorkArea() {
        return this.workareaCurrent;
    }
}
