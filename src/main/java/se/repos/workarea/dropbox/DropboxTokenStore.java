/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */

package se.repos.workarea.dropbox;


import java.util.List;

public interface DropboxTokenStore{
	
	List<String> read(String userName);
	
	void write(String userName, List<String> tokens);
	
}