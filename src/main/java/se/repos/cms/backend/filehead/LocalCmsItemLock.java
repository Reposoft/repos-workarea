package se.repos.cms.backend.filehead;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;

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
        // TODO Create lock file, write lock comment and date to it.
        return null;
    }
    
    private File getLockFile() {
        String filePath = this.repository.getPath() + this.lockPath.getPath();
        return new File(filePath);
    }

    @Override
    public String getComment() {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(getLockFile());
            return IOUtils.toString(fis);
        } catch (IOException e) {
            throw new RuntimeException(e.getCause());
        } finally {
            IOUtils.closeQuietly(fis);
        }        
    }

    @Override
    public Date getDateCreation() {
        // TODO Read lock file, exctract creation date from it.
        return null;
    }

    @Override
    public Date getDateExpiration() {
        // There is no defined expiration Date for these locks.
        return null;
    }

    @Override
    public String getToken() {
        // TODO What is the lock tocken?
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
}
