package se.repos.cms.backend.filehead;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import se.simonsoft.cms.item.CmsItem;
import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.CmsItemLock;
import se.simonsoft.cms.item.CmsItemPath;
import se.simonsoft.cms.item.CmsRepository;
import se.simonsoft.cms.item.impl.CmsItemIdUrl;
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
        Set<CmsItemId> immediates = new HashSet<CmsItemId>();
        for (LocalCmsItem item : this.getLocalImmediates(parent, ItemType.FOLDER)) {
            immediates.add(item.getId());
        }
        return immediates;
    }

    @Override
    public Set<CmsItemId> getImmediateFiles(CmsItemId parent)
            throws CmsConnectionException, CmsItemNotFoundException {
        Set<CmsItemId> immediates = new HashSet<CmsItemId>();
        for (LocalCmsItem item : this.getLocalImmediates(parent, ItemType.FILE)) {
            immediates.add(item.getId());
        }
        return immediates;
    }

    @Override
    public Set<CmsItem> getImmediates(CmsItemId parent) throws CmsConnectionException,
            CmsItemNotFoundException {
        Set<CmsItem> immediates = new HashSet<CmsItem>();
        immediates.addAll(this.getLocalImmediates(parent, ItemType.BOTH));
        return immediates;
    }

    private Set<LocalCmsItem> getLocalImmediates(CmsItemId parent, ItemType itemType) {
        return this.getLocalImmediates(this.getLocalCmsItem(parent), itemType);
    }

    private Set<LocalCmsItem> getLocalImmediates(LocalCmsItem parent, ItemType itemType) {
        Set<LocalCmsItem> localImmediates = new HashSet<LocalCmsItem>();
        CmsItemPath parentPath = parent.getId().getRelPath();
        for (File child : parent.getTrackedFile().listFiles()) {
            boolean add = false;
            switch (itemType) {
            case BOTH:
                add = true;
                break;
            case FILE:
                add = child.isFile();
                break;
            case FOLDER:
                add = child.isDirectory();
                break;
            }
            if (add) {
                CmsItemPath childPath = parentPath.append(child.getName());
                localImmediates.add(this.getLocalCmsItem(new CmsItemIdUrl(
                        this.repository, childPath)));
            }
        }
        return localImmediates;
    }

    @Override
    public Iterable<CmsItemId> getDescendants(CmsItemId parent) {
        Set<CmsItemId> children = new HashSet<CmsItemId>();
        this.getLocalDescendants(children, this.getLocalCmsItem(parent));
        return children;
    }

    private void getLocalDescendants(Set<CmsItemId> children, LocalCmsItem parent) {
        for (LocalCmsItem child : this.getLocalImmediates(parent, ItemType.BOTH)) {
            children.add(child.getId());
        }
        for (LocalCmsItem folder : this.getLocalImmediates(parent, ItemType.FOLDER)) {
            this.getLocalDescendants(children, folder);
        }
    }

    @Override
    public CmsItemLock getLocked(CmsItemId itemId) {
        // TODO Auto-generated method stub
        return null;
    }
}
