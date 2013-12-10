/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.workarea;

import java.util.List;

import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.CmsItemPath;

public interface WorkArea {
    
    /**
     * Uploads the provided CMS items to this work area in the folder given as
     * the first argument.
     * 
     * @param items
     *            The CMS items to upload to the work area.
     * @param folderName
     *            The folder in the work area to upload them to.
     */
    void uploadItems(CmsItemPath folder, List<CmsItemId> items);

    /**
     * Lists all the CMS items in this work area.
     * 
     * @return A {@link List} of all the {@link CmsItemPath} in the work area.
     */
    List<CmsItemPath> getItems();

    /**
     * Returns the CMS items that have been changed since being uploaded to the
     * work area.
     * 
     * @return A {@link List} of all the {@link CmsItemPath} in the work area
     *         that have changed.
     */
    List<CmsItemPath> getChangedItems();

    /**
     * Removed the given CMS items from the work area and commits any changed
     * back to the CMS.
     * 
     * @param items
     *            The CMS items to commit.
     */
    void commitItems(List<CmsItemId> items);
}