/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.workarea.local;

import java.util.List;

import se.repos.workarea.WorkArea;
import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.CmsItemPath;

public class WorkAreaLocal implements WorkArea {

    @Override
    public void uploadItems(String folderName, List<CmsItemId> items) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<CmsItemPath> getItems() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<CmsItemPath> getChangedItems() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void commitItems(List<CmsItemId> items) {
        // TODO Auto-generated method stub

    }
}
