package se.repos.cms.backend.filehead;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;

import se.repos.authproxy.ReposCurrentUser;
import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.CmsItemLock;
import se.simonsoft.cms.item.CmsItemPath;
import se.simonsoft.cms.item.CmsRepository;
import se.simonsoft.cms.item.impl.CmsItemIdUrl;

public class LocalCmsItemLock implements CmsItemLock {
    private CmsItemPath lockPath;

    private CmsRepository repository;
    private ReposCurrentUser currentUser;

    @Inject
    public void setRepository(CmsRepository repository) {
        this.repository = repository;
    }

    @Inject
    public void setReposCurrentUser(ReposCurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    private LocalCmsItemLock(CmsItemPath lockPath) {
        this.lockPath = lockPath;
    }

    public static LocalCmsItemLock getLocalLock(CmsItemPath lockedItem, String lockComment) {
        Date creationDate = new Date();
        // The lock file has the same name as the item it locks, but with a
        // ".lock" suffix.
        CmsItemPath lockPath = new CmsItemPath(lockedItem.getPath() + ".lock");
        String lockFileContents = creationDate.getTime() + ":" + lockComment;
        try {
            FileUtils.write(new File(lockPath.getPath()), lockFileContents);
        } catch (IOException e) {
            throw new RuntimeException(e.getCause());
        }
        return new LocalCmsItemLock(lockPath);
    }

    private File getLockFile() {
        String filePath = this.repository.getPath() + this.lockPath.getPath();
        return new File(filePath);
    }

    @Override
    public String getComment() {
        try {
            return FileUtils.readFileToString(this.getLockFile()).split(":")[1];
        } catch (IOException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public Date getDateCreation() {
        try {
            long epochTime = Long.parseLong(FileUtils
                    .readFileToString(this.getLockFile()).split(":")[0]);
            return new Date(epochTime);
        } catch (NumberFormatException e) {
            throw new RuntimeException(e.getCause());
        } catch (IOException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public Date getDateExpiration() {
        // There is no defined expiration date for this lock.
        return null;
    }

    @Override
    public String getToken() {
        // TODO What is the lock token?
        return null;
    }

    @Override
    public String getOwner() {
        return this.currentUser.getUsername();
    }

    @Override
    public CmsItemId getItemId() {
        return new CmsItemIdUrl(this.repository, this.lockPath);
    }

    /**
     * Unlocks this lock by deleting the lock file. Using the receiving object
     * after calling this method is undefined.
     */
    public void unlock() {
        this.getLockFile().delete();
    }
}
