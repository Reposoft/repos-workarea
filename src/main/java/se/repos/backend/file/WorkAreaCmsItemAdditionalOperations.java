/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.backend.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import se.repos.lgr.Lgr;
import se.repos.lgr.LgrFactory;
import se.simonsoft.cms.item.CmsItem;
import se.simonsoft.cms.item.CmsItemId;

/**
 * Operations that are currently not supported in Repos CMS APIs.
 * 
 * Casts all {@link CmsItem} instances to {@link CmsItemFilesystem}.
 */
public class WorkAreaCmsItemAdditionalOperations {
	public static final Lgr logger = LgrFactory.getLogger();

	public void lock(CmsItem item) {
		throw new UnsupportedOperationException();
	}
	
	public void unlock(CmsItem item) {
		throw new UnsupportedOperationException();
	}

	
	public List<String> getRepositoryFileNames(Set<CmsItemId> cmsItemId){
		List<String> fileNames = new LinkedList<String>();
		for(CmsItemId cms : cmsItemId){
			fileNames.add(cms.getRelPath().getName());
		}
		return fileNames;
	}
	
	public String readLockFile(File file){
		String content = "";
		try{
			FileInputStream in = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			content = br.readLine();
			br.close();	
		}catch(FileNotFoundException fne){
			String errorMsg =  "Someting went wrong while reading lock file";
			logger.error(errorMsg,fne);
		}catch(IOException IOe){
			String errorMsg =  "Someting went wrong while reading lock file";
			logger.error(errorMsg,IOe);
		}
		return content;
	}
	
}
