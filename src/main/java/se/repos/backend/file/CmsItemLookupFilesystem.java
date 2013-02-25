/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.backend.file;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import se.simonsoft.cms.item.CmsItem;
import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.CmsItemPath;
import se.simonsoft.cms.item.CmsRepository;
import se.simonsoft.cms.item.RepoRevision;
import se.simonsoft.cms.item.impl.CmsItemIdUrl;
import se.simonsoft.cms.item.impl.CmsRepositoryId;
import se.simonsoft.cms.item.info.CmsConnectionException;
import se.simonsoft.cms.item.info.CmsItemLookup;
import se.simonsoft.cms.item.info.CmsItemNotFoundException;
import se.simonsoft.cms.item.info.CmsLockQuery;

/**
 * The first step towards a local filesystem backend for Repos.
 * 
 * Impl that reads file paths under a local directory as if it was a subversion repo.
 * 
 * {@link #getItem(CmsItemId)}.{@link CmsItem#getRevisionChanged()}.{@link RepoRevision#getDate()} returns last modified date.
 */
public class CmsItemLookupFilesystem implements CmsItemLookup {
	
	private File rootFile;
	private CmsRepository repo;
	
	/**
	 * @param root corresponding to repository root in subversion
	 */
	public CmsItemLookupFilesystem(CmsRepository repo,File root) {
		this.repo = repo;
		this.rootFile = root;
	}
	
	@Override
	public CmsItem getItem(CmsItemId id) throws CmsConnectionException,
			CmsItemNotFoundException {
		CmsItemFilesystem cmsIFS = null;
		for(File reposFile : Arrays.asList(rootFile.listFiles())){
			if(id.getRelPath().getName().equals(reposFile.getName()))
				cmsIFS = new CmsItemFilesystem(reposFile,id.getRepository(),id.getRelPath());
		}
		if(cmsIFS == null)
			throw new CmsItemNotFoundException(id);
		return cmsIFS;
	}

	@Override
	public Set<CmsItem> getImmediates(CmsItemId parent)
			throws CmsConnectionException, CmsItemNotFoundException {
		Set<CmsItem> cmsSet = null;
		if(parent instanceof CmsRepositoryId){
			cmsSet = new HashSet<CmsItem>();
			for(File reposFile : Arrays.asList(rootFile.listFiles())){
				String filename = reposFile.getName();
				String extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
				if(!extension.equals("lock")){
					cmsSet.add(new CmsItemFilesystem(reposFile,parent.getRepository(),
								new CmsItemPath("/"+filename)));
				}
			}
			return cmsSet;
		}else{
			for(File reposFile : Arrays.asList(rootFile.listFiles())){
				if(parent.getRelPath().getName().equals(reposFile.getName())){
					if(reposFile.isDirectory() && reposFile.exists()){
						cmsSet = new HashSet<CmsItem>();
						for(File subFile : Arrays.asList(reposFile.listFiles())){
							String filename = subFile.getName();
							String extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
							if(!extension.equals("lock")){
								cmsSet.add(new CmsItemFilesystem(subFile,parent.getRepository(),
										parent.getRelPath().append(filename)));
							}
						}
					}
				}
			}
			if(cmsSet == null)
				throw new CmsItemNotFoundException(parent);
			return cmsSet;
		}
	}	
	
	@Override
	public Set<CmsItemId> getImmediateFolders(CmsItemId parent)
			throws CmsConnectionException, CmsItemNotFoundException {
		return getCmsItemIdSet(parent,true);
	}

	@Override
	public Set<CmsItemId> getImmediateFiles(CmsItemId parent)
			throws CmsConnectionException, CmsItemNotFoundException {
		return getCmsItemIdSet(parent,false);
	}

	@Override
	public Iterable<CmsItemId> getDescendants(CmsItemId parent) {
		if(parent instanceof CmsRepositoryId){
			Set<CmsItemId> cmsSet = new HashSet<CmsItemId>();
			for(File reposFile : Arrays.asList(rootFile.listFiles())){
				cmsSet.add(new CmsItemIdUrl(parent.getRepository(),
											parent.getRelPath().append(reposFile.getName())));
			}
			return cmsSet;
		}else{
			Set<CmsItemId> cmsSet = new HashSet<CmsItemId>();
			for(File reposFile : Arrays.asList(rootFile.listFiles())){
				if(parent.getRelPath().getName().equals(reposFile.getName())){
					if(reposFile.isDirectory() && reposFile.exists()){
						for(File subFile : Arrays.asList(reposFile.listFiles())){
							cmsSet.add(new CmsItemIdUrl(parent.getRepository(),
										parent.getRelPath().append(subFile.getName())));
						}
					}
				}
			}
			return cmsSet;
		}
	}

	@Override
	public Set<CmsItemId> getLocked(CmsLockQuery lockQuery) {
		String messageGlob = lockQuery.getMessageGlob();
		Set<CmsItemId> cmsSet = new HashSet<CmsItemId>();
		List<File> rootFiles = Arrays.asList(rootFile.listFiles());
		String lockSuffix = ".lock";
		try{
			if(messageGlob.startsWith("*")){
				for(File file : rootFiles){
					if(file.isDirectory()){
						for(File subFile : Arrays.asList(file.listFiles())){
							String content = FileUtils.readFileToString(subFile);
							if(content.endsWith(messageGlob.substring(1, messageGlob.length()).trim())){
								CmsItemId cmsId = createCmsItemId(subFile,true,file,lockSuffix);
								cmsSet.add(cmsId);				
							}
						}
					}else{
					String content = FileUtils.readFileToString(file);
					if(content.endsWith(messageGlob.substring(1, messageGlob.length()).trim())){
						CmsItemId cmsId = createCmsItemId(file,false,null,lockSuffix);
						cmsSet.add(cmsId);						
					}
				}
				}
			}else if(messageGlob.endsWith("*")){
				for(File file : rootFiles){
					if(file.isDirectory()){
						for(File subFile : Arrays.asList(file.listFiles())){
							String content = FileUtils.readFileToString(subFile);
							if(content.startsWith(messageGlob.substring(0, messageGlob.length()-1).trim())){		
								CmsItemId cmsId = createCmsItemId(subFile,true,file,lockSuffix);
								cmsSet.add(cmsId);		
							}
						}
					}else{
					String content = FileUtils.readFileToString(file);
					if(content.startsWith(messageGlob.substring(0, messageGlob.length()-1).trim())){
						CmsItemId cmsId = createCmsItemId(file,false,null,lockSuffix);
						cmsSet.add(cmsId);				
					}
				}
				}
			}else{
				for(File file : rootFiles){
					if(file.isDirectory()){
						for(File subFile : Arrays.asList(file.listFiles())){
							String content = FileUtils.readFileToString(subFile);
							if(content.equals(messageGlob)){
								CmsItemId cmsId = createCmsItemId(subFile,true,file,lockSuffix);
								cmsSet.add(cmsId);
							}
						}
					}else{
					String content = FileUtils.readFileToString(file);
					if(content.equals(messageGlob)){
						CmsItemId cmsId = createCmsItemId(file,false,null,lockSuffix);
						cmsSet.add(cmsId);
					}
				}
				}
			}
		}catch(IOException e){
			
		}
		return cmsSet;
	}
	
	
	/**
	 * Returns a set of CmsItemId from parent CmsItemId and if wanted Folders or not
	 * 
	 * @param parent The parent CmsItemId item
	 * @param folder Boolean value to determine if you want folder or not
	 * @return The set of CmsItemId found under parent CmsItemId
	 * */
	private Set<CmsItemId> getCmsItemIdSet(CmsItemId parent, boolean folders)
			throws CmsConnectionException, CmsItemNotFoundException {
		Set<CmsItemId> cmsSet = null;
		if(parent instanceof CmsRepositoryId){
			cmsSet = new HashSet<CmsItemId>();
			for(File reposFile : Arrays.asList(rootFile.listFiles())){
				String filename = reposFile.getName();
				String extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
				if(folders == reposFile.isDirectory() && !extension.equals("lock")){
					cmsSet.add(new CmsItemIdUrl(parent.getRepository(),
							new CmsItemPath("/"+reposFile.getName())));
				}
			}
			return cmsSet;
		}else{
			for(File reposFile : Arrays.asList(rootFile.listFiles())){
				if(parent.getRelPath().getName().equals(reposFile.getName())){
					if(reposFile.isDirectory() && reposFile.exists()){
						cmsSet = new HashSet<CmsItemId>();
						for(File subFile : Arrays.asList(reposFile.listFiles())){
							String filename = reposFile.getName();
							String extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
							if(folders == subFile.isDirectory() && !extension.equals("lock")){
								cmsSet.add(new CmsItemIdUrl(parent.getRepository(),
											parent.getRelPath().append(subFile.getName())));
							}
						}
					}
				}
			}
			if(cmsSet == null)
				throw new CmsItemNotFoundException(parent);
			return cmsSet;
		}
	}
	
	
	/**
	 * Creates a CmsItemId item 
	 * @param file the file which we are creating the CmsItemId on
	 * @param inSubFolder boolean value to determine if file is in a subfolder
	 * @param subFolder if file is in subfolder then this is that folder
	 * @param lockSuffix a string representing the lock suffix for the lock files 
	 * @return CmsItemId the CmsItemId we created 
	 * */
	private CmsItemId createCmsItemId(File file, boolean inSubFolder, File subFolder, String lockSuffix){
		CmsItemId cmsItem = null;
		String lockFileName = file.getName();
		File lockedFile = null;
		if(lockFileName.endsWith(lockSuffix)){
			lockFileName = lockFileName.substring(0,lockFileName.indexOf(lockSuffix));
			if(inSubFolder){
				lockedFile = new File(subFolder,lockFileName);
				if(lockedFile != null && lockedFile.exists())
					cmsItem = new CmsItemIdUrl(this.repo,"/"+ subFolder.getName() +"/"+lockedFile.getName());
			}else{
				lockedFile = new File(rootFile,lockFileName);
				if(lockedFile != null && lockedFile.exists())
					cmsItem = new CmsItemIdUrl(this.repo,"/"+lockedFile.getName());
		}
		}
		return cmsItem;
	}

}
