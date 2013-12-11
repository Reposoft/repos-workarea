package se.repos.cms.backend.filehead;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import se.simonsoft.cms.item.CmsItem;
import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.CmsItemLock;
import se.simonsoft.cms.item.CmsItemPath;
import se.simonsoft.cms.item.CmsRepository;
import se.simonsoft.cms.item.info.CmsConnectionException;
import se.simonsoft.cms.item.info.CmsItemLookup;
import se.simonsoft.cms.item.info.CmsItemNotFoundException;

public class LocalCmsItemLookup implements CmsItemLookup {
    private CmsRepository repository;

    @Inject
    public void setRepository(CmsRepository repository) {
        this.repository = repository;
    }

    @Override
    public CmsItem getItem(CmsItemId id) throws CmsConnectionException,
            CmsItemNotFoundException {
        return this.getLocalCmsItem(id);
    }

    private LocalCmsItem getLocalCmsItem(CmsItemId id) throws CmsItemNotFoundException {
        CmsItemPath itemPath = id.getRelPath();
        LocalCmsItem file = new LocalCmsItem(itemPath);
        if (!file.getTrackedFile().exists()) {
            throw new CmsItemNotFoundException(this.repository, itemPath);
        }
        return file;
    }

    @Override
    public Set<CmsItemId> getImmediateFolders(CmsItemId parent)
            throws CmsConnectionException, CmsItemNotFoundException {
        return this.getLocalChildren(parent, true, ItemType.FOLDER);
    }

    @Override
    public Set<CmsItemId> getImmediateFiles(CmsItemId parent)
            throws CmsConnectionException, CmsItemNotFoundException {
        return this.getLocalChildren(parent, true, ItemType.FILE);
    }

    @Override
    public Set<CmsItem> getImmediates(CmsItemId parent) throws CmsConnectionException,
            CmsItemNotFoundException {
        Set<CmsItem> immediates = new HashSet<CmsItem>();
        for (CmsItemId id : this.getLocalChildren(parent, true, ItemType.BOTH)) {
            immediates.add(this.getLocalCmsItem(id));
        }
        return immediates;
    }

    @Override
    public Iterable<CmsItemId> getDescendants(CmsItemId parent) {
        return this.getLocalChildren(parent, false, ItemType.BOTH);
    }

    private Set<CmsItemId> getLocalChildren(CmsItemId parent, boolean justImmediate,
            ItemType itemType) {
        // TODO Method stub.
        return null;
    }

    @Override
    public CmsItemLock getLocked(CmsItemId itemId) {
        // TODO Auto-generated method stub
        return null;
    }
}
