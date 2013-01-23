/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.workarea;

import java.util.*;

public interface WorkArea{

	void uploadFile(String folderName, List<String> pathList);
	
	List getFileList();

	List<String> updatedFileCheck();

	void commitFiles(List<String> files);
}