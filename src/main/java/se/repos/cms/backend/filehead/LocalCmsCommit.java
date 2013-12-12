package se.repos.cms.backend.filehead;

import javax.inject.Inject;

import se.simonsoft.cms.item.CmsItemLock;
import se.simonsoft.cms.item.CmsItemLockCollection;
import se.simonsoft.cms.item.CmsItemPath;
import se.simonsoft.cms.item.CmsRepository;
import se.simonsoft.cms.item.RepoRevision;
import se.simonsoft.cms.item.commit.CmsCommit;
import se.simonsoft.cms.item.commit.CmsItemLockedException;
import se.simonsoft.cms.item.commit.CmsPatchset;

public class LocalCmsCommit implements CmsCommit {
    private CmsRepository repository;

    @Inject
    public void setRepository(CmsRepository repository) {
        this.repository = repository;
    }

    @Override
    public RepoRevision run(CmsPatchset fileModifications) throws CmsItemLockedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CmsItemLockCollection lock(String message, RepoRevision base,
            CmsItemPath... item) throws CmsItemLockedException {
        LocalCmsItemLockCollection locks = new LocalCmsItemLockCollection(this.repository);
        for (CmsItemPath toLock : item) {
            locks.add(LocalCmsItemLock.getLocalLock(toLock, message));
        }
        return locks;
    }

    @SuppressWarnings("serial")
    private class LocalCmsItemLockCollection extends CmsItemLockCollection {

        public LocalCmsItemLockCollection(CmsRepository repository) {
            super(repository);
        }

        public void add(LocalCmsItemLock lock) {
            super.add(lock);
        }
    }

    @Override
    public void unlock(CmsItemLock... lock) {
        for (CmsItemLock toUnlock : lock) {
            if (!(toUnlock instanceof LocalCmsItemLock)) {
                throw new IllegalArgumentException(
                        "Non local lock passed to local commit class!");
            }
            LocalCmsItemLock localLock = (LocalCmsItemLock) toUnlock;
            localLock.unlock();
        }
    }
}
