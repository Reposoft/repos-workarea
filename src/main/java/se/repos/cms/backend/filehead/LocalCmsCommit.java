package se.repos.cms.backend.filehead;

import se.simonsoft.cms.item.CmsItemLock;
import se.simonsoft.cms.item.CmsItemLockCollection;
import se.simonsoft.cms.item.CmsItemPath;
import se.simonsoft.cms.item.RepoRevision;
import se.simonsoft.cms.item.commit.CmsCommit;
import se.simonsoft.cms.item.commit.CmsItemLockedException;
import se.simonsoft.cms.item.commit.CmsPatchset;

public class LocalCmsCommit implements CmsCommit {

    @Override
    public RepoRevision run(CmsPatchset fileModifications) throws CmsItemLockedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CmsItemLockCollection lock(String message, RepoRevision base,
            CmsItemPath... item) throws CmsItemLockedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void unlock(CmsItemLock... lock) {
        // TODO Auto-generated method stub

    }
}
