package se.repos.cms.backend.filehead;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import se.simonsoft.cms.item.CmsItem;
import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.CmsItemKind;
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
        if (!file.exists()) {
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
        return LocalCmsItemLookup.getLocalImmediates(this.getLocalCmsItem(parent),
                itemType);
    }

    private static Set<LocalCmsItem> getLocalImmediates(LocalCmsItem parent,
            ItemType itemType) {
        Set<LocalCmsItem> localImmediates = new HashSet<LocalCmsItem>();
        for (LocalCmsItem child : parent.getChildItems()) {
            boolean add = false;
            switch (itemType) {
            case BOTH:
                add = true;
                break;
            case FILE:
                add = child.getKind() == CmsItemKind.File;
                break;
            case FOLDER:
                add = child.getKind() == CmsItemKind.Folder;
                break;
            }
            if (add) {
                localImmediates.add(child);
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
        for (LocalCmsItem child : LocalCmsItemLookup.getLocalImmediates(parent,
                ItemType.BOTH)) {
            children.add(child.getId());
        }
        for (LocalCmsItem folder : LocalCmsItemLookup.getLocalImmediates(parent,
                ItemType.FOLDER)) {
            this.getLocalDescendants(children, folder);
        }
    }

    @Override
    public CmsItemLock getLocked(CmsItemId itemId) {
        return LocalCmsItemLock.getLocalLock(this.repository, itemId.getRelPath());
    }
}
