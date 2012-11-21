/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.workarea.rest;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import se.repos.authproxy.ReposCurrentUser;
import se.repos.workarea.WorkAreaConfiguration;
import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.CmsItemPath;
import se.simonsoft.cms.item.CmsRepository;

import se.simonsoft.cms.item.impl.CmsItemIdUrl;
import se.simonsoft.cms.item.info.CmsItemLookup;

@Path("/work")
public class WorkAreaResource {

	// global services
	private WorkAreaConfiguration workAreaConfiguration;
	
	// per repository services
	private Map<CmsRepository, CmsItemLookup> itemRead;
	// out of scope for first iteration //@Inject private Map<CmsRepository, CmsLocking> itemCommit;
	// out of scope for first iteration //@Inject private Map<CmsRepository, CmsCommit> itemCommit;

	@Inject
	public void setWorkAreaConfiguration(WorkAreaConfiguration workAreaConfiguration) {
		this.workAreaConfiguration = workAreaConfiguration;
	}

	@Inject
	public void setItemRead(Map<CmsRepository, CmsItemLookup> itemRead) {
		this.itemRead = itemRead;
	}
	
	/**
	 * Basic mapping of repository name to URL, until this module can access current host's repository configuration.
	 */
	private CmsRepository getRepository(String repositoryName) {
		// TODO loop itemRead keys and match on getName
		return null;
	}
	
	/**
	 * Reads selected files from a repository, locks them, and sends to work area with a reference back to the versioned file.
	 * The reference may require renaming of the file, quite possibly with a suffix.
	 * 
	 * @param repositoryId repository name, only singpe parent path setups are supported for now
	 * @param targets items to process, all from same repository
	 * @return status 204 if OK (or 200 if we have some content to present)
	 */
	@POST
	@Path("{repo}/checkout")
	public Response checkout(
			@PathParam("repo") String repositoryId,
			@QueryParam("target") List<CmsItemPath> targets) {
		CmsRepository repo = getRepository(repositoryId);
		
		// CmsItemLookup expects items
		List<CmsItemId> items = new LinkedList<CmsItemId>();
		for (CmsItemPath target : targets) {
			items.add(new CmsItemIdUrl(repo, target));
		}
				
		throw new UnsupportedOperationException("Not implemented");
	}
	
	/**
	 * 
	 * In first iteration we don't analyze work area contents, it's just a list of files.
	 * 
	 * There should be one method for JSON and one for HTML,
	 * or separate javax.ws.rs.ext.MessageBodyWriter for entity list to JSON and HTML,
	 * or a javascript based HTML that fetches JSON.
	 * 
	 * @return items in work area
	 */
	@GET
	@Path("list")
	public Response view() {
		
		throw new UnsupportedOperationException("Not implemented");
	}
	
	/**
	 * Stores changes from work area in central repository and unlocks included items.
	 * @param historyComment for versioned commit
	 * @param workAreaEntry idintifies entries in the work area, could be folders one day
	 * @return status
	 */
	@POST
	@Path("commit")
	public Response commit(
			@QueryParam("comment") String historyComment,
			@QueryParam("entry") List<String> workAreaEntry) {
		throw new UnsupportedOperationException("Not in scope for first iteration");
	}
	
}
