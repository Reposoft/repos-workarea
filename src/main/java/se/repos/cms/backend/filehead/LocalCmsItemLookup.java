package se.repos.cms.backend.filehead;

import java.util.Set;

import se.simonsoft.cms.item.CmsItem;
import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.CmsItemLock;
import se.simonsoft.cms.item.info.CmsConnectionException;
import se.simonsoft.cms.item.info.CmsItemLookup;
import se.simonsoft.cms.item.info.CmsItemNotFoundException;

public class LocalCmsItemLookup implements CmsItemLookup {

    @Override
    public CmsItem getItem(CmsItemId id) throws CmsConnectionException,
            CmsItemNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<CmsItemId> getImmediateFolders(CmsItemId parent)
            throws CmsConnectionException, CmsItemNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<CmsItemId> getImmediateFiles(CmsItemId parent)
            throws CmsConnectionException, CmsItemNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<CmsItem> getImmediates(CmsItemId parent) throws CmsConnectionException,
            CmsItemNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<CmsItemId> getDescendants(CmsItemId parent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CmsItemLock getLocked(CmsItemId itemId) {
        // TODO Auto-generated method stub
        return null;
    }
}
