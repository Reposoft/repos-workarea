/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */

package se.repos.backend.file;

import java.util.Date;

import se.simonsoft.cms.item.CmsItemLock;

public class CmsItemLockFile implements CmsItemLock{

	private String owner;
	private Date creationDate;
	private String comment;
	
	public CmsItemLockFile(String owner,Date creation, String comment){
		this.owner = owner;
		this.creationDate = creation;
		this.comment = comment;
	}
	
	@Override
	public String getComment() {
		return this.comment;
	}

	@Override
	public Date getDateCreation() {
		return this.creationDate;
	}

	@Override
	public Date getDateExpiration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getOwner() {
		return this.owner;
	}

}
