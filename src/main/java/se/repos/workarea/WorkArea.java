/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.workarea;

import java.util.*;

import se.simonsoft.cms.item.CmsItemId;

public interface WorkArea{

	void uploadFile(String folderName, List<CmsItemId> items);
	
	List<String> getFileList();

	List<String> updatedFileCheck();

	void commitFiles(List<CmsItemId> items);
}