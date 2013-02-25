/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.backend.file;

import java.io.File;
import java.io.OutputStream;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import se.repos.lgr.Lgr;
import se.repos.lgr.LgrFactory;
import se.simonsoft.cms.item.Checksum;
import se.simonsoft.cms.item.CmsItem;
import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.CmsItemKind;
import se.simonsoft.cms.item.CmsItemLock;
import se.simonsoft.cms.item.CmsItemPath;
import se.simonsoft.cms.item.CmsRepository;
import se.simonsoft.cms.item.RepoRevision;
import se.simonsoft.cms.item.impl.CmsItemIdUrl;
import se.simonsoft.cms.item.properties.CmsItemProperties;

public class CmsItemFilesystem implements CmsItem {
	
	private File cmsItemFile;
	CmsRepository repository;
	CmsItemPath path;
	public static final Lgr logger = LgrFactory.getLogger();
	
	public CmsItemFilesystem(File cmsItemFile, CmsRepository repository, CmsItemPath path){
		this.cmsItemFile = cmsItemFile;
		this.repository = repository;
		this.path = path;
	}

	/**
	 * Until locking is implemented we need the local file reference directly in {@link WorkAreaCmsItemAdditionalOperations}.
	 */
	public File getFile() {
		return this.cmsItemFile;
	}
	
	@Override
	public Checksum getChecksum() {
		throw new UnsupportedOperationException("not implemented");
	}	

	@Override
	public void getContents(OutputStream arg0)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public CmsItemId getId() {
		return new CmsItemIdUrl(repository,path);
	}

	@Override
	public CmsItemKind getKind() {
		if(cmsItemFile.isDirectory()){
			return CmsItemKind.Folder;
		}else{ 
			return CmsItemKind.File;
		}
	}

	@Override
	public CmsItemProperties getProperties() {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public RepoRevision getRevisionChanged() {
		long lastModified = this.cmsItemFile.lastModified();
		return new RepoRevision(lastModified, new Date(lastModified));
	}

	@Override
	public String getStatus() {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public CmsItemLock getLock() {
		File lockFile = new File(cmsItemFile.getParent() + "/" +cmsItemFile.getName()+".lock");
		CmsItemLockFile cmsLock = null;
		if(lockFile.exists()){
			String lockContent = "";
			try{
				lockContent = FileUtils.readFileToString(lockFile);
			}catch(Exception e){
				String errorMsg =  "Someting went wrong while reading lock file";
				logger.error(errorMsg,e);
				throw new RuntimeException(errorMsg,e);
			}
			cmsLock = new CmsItemLockFile(repository.getHost(),new Date(lockFile.lastModified()),lockContent);
		}
		return cmsLock;
	}

}
