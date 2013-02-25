/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.workarea.rest;


import java.util.LinkedList;
import java.util.List;
import java.util.Map;



import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import se.repos.workarea.WorkAreaConfiguration;
import se.repos.workarea.WorkArea;
import se.repos.workarea.dropbox.*;
import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.CmsItemPath;
import se.simonsoft.cms.item.CmsRepository;

import se.simonsoft.cms.item.impl.CmsItemIdUrl;
import se.simonsoft.cms.item.info.CmsItemLookup;

@Path("/work")
public class WorkAreaResource {

	// global services
	private WorkAreaConfiguration workAreaConfiguration;
	//private WorkArea workarea;
	
	// per repository services
	private Map<CmsRepository, CmsItemLookup> itemRead;
	// out of scope for first iteration //@Inject private Map<CmsRepository, CmsLocking> itemCommit;
	// out of scope for first iteration //@Inject private Map<CmsRepository, CmsCommit> itemCommit;
	
	private DropboxTokenStore tokenStore;
	
	

	@Inject
	public void setWorkAreaConfiguration(WorkAreaConfiguration workAreaConfiguration) {
		this.workAreaConfiguration = workAreaConfiguration;
	}

	@Inject
	public void setItemRead(Map<CmsRepository, CmsItemLookup> itemRead) {
		this.itemRead = itemRead;
	}
	
	@Inject public void setDropboxTokenStore(@Named("tokenstore")DropboxTokenStore tokenStore) {
		this.tokenStore = tokenStore;
	}
	
	
	
	/**
	 * Basic mapping of repository name to URL, until this module can access current host's repository configuration.
	 */
	private CmsRepository getRepository(String repositoryName) {
		CmsRepository repository = null;
		for(CmsRepository cmsrep : itemRead.keySet()){
			if(repositoryName.equals(cmsrep.getName())){
				repository = cmsrep;
				break;
			}
		}
		return repository;
	}
	
	/**
	 * Reads selected files from a repository, locks them, and sends to work area with a reference back to the versioned file.
	 * The reference may require renaming of the file, quite possibly with a suffix.
	 * 
	 * @param repositoryId repository name, only single parent path setups are supported for now
	 * @param targets items to process, all from same repository
	 * @return status 204 if OK (or 200 if we have some content to present)
	 *
	 * List<CmsItemPath>
	 */
	@POST
	@Path("{repo}/checkout")
	public Response checkout(
			@PathParam("repo") String repositoryId,
			@QueryParam("target") List<String> targets) {
		CmsRepository repo = getRepository(repositoryId);
		//Files to be sent to workarea
		List<CmsItemId> items = new LinkedList<CmsItemId>();
		
		//Setting up the Cms files 
		for(String s : targets){
			CmsItemPath cmsPath = new CmsItemPath(s);
			CmsItemIdUrl cmsUrl = new CmsItemIdUrl(repo,cmsPath);
			items.add(cmsUrl);
		}
		if(!items.isEmpty())
			workAreaConfiguration.getWorkArea().uploadFile(repositoryId,items);

		String answer = "<h1>" + repositoryId + "</h1>";
		return Response.ok(answer).build();
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
	@Produces(MediaType.TEXT_HTML)
	public Response viewHTML() {	
		return Response.ok(buildResponse(), MediaType.TEXT_HTML).build();
	}

	 /* 
	 * @return items in work area
	 */
	@GET
	@Path("list")
	@Produces(MediaType.APPLICATION_JSON)
	public Response viewJSON(){
		return Response.ok(buildResponse(), MediaType.APPLICATION_JSON).build();
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
			@QueryParam("comment") String comment,
			@QueryParam("entry") List<String> entry) {
		//Files to be sent to workarea
		CmsRepository repo = getRepository(comment);
		List<CmsItemPath> cmstargets = new LinkedList<CmsItemPath>();
		List<CmsItemId> items = new LinkedList<CmsItemId>();
		
		//Setting up the Cms files for future use and adding files to be checked out
		for(String s : entry){
			CmsItemPath cmsPath = new CmsItemPath(s);
			cmstargets.add(cmsPath);
			CmsItemIdUrl cmsUrl = new CmsItemIdUrl(repo,cmsPath);
			items.add(cmsUrl);
		}
		if(!items.isEmpty())
			workAreaConfiguration.getWorkArea().commitFiles(items);

		return Response.ok(comment).build();
	}

	/**
	 * Shows link for dropbox users to accept application to use their account
	 * @return status
	 */
	@GET
	@Path("admin")
	public Response dropboxAdmin(){
		WorkArea workarea = workAreaConfiguration.getWorkArea();
		String listURL = "'http://localhost:8088/repos/work/list'";
		String response ="";
		if(workarea instanceof WorkAreaDropBox){
			WorkAreaDropBox workAreaDropbox = (WorkAreaDropBox) workarea;
			workAreaDropbox.initializeDropbox(this.tokenStore);
			String dropboxURL = workAreaDropbox.getAcceptUrl();
			response = "<p>Follow" + "<a href='" + dropboxURL + "' target='_blank'> This </a> link";
			response += " and click on allow and then come back and follow";
			response += "<a href ="+ listURL +">this</a> link to list files in repository</p>";
		}else{
			response = "<p>You have not choosen to work with dropbox,";
			response +=" please follow <a href ="+ listURL +">this</a> link to list files in repository</p>";
		}
		return Response.ok(response,MediaType.TEXT_HTML).build();
	}
	
	private String buildResponse(){
			WorkArea workArea = workAreaConfiguration.getWorkArea();
			List<String> fileList = workArea.getFileList(); 
			List<String> updated = workArea.updatedFileCheck();
			String files = "<h1 id=div1>Files in repository</h1>\n";
			files += "<select id='multipleSelect' size= "+fileList.size()  +"  multiple='multiple'>\n" ;
			for(String s : fileList){	
				files += "<option value =" + s + ">" + s + " </option> \n";  
			}
			files += "</select> \n";
			//Setting up div with list for files that has been changed
			files += "<div style='display: none;' id='commit' title='Commit files'>These files have been changed:";
			files += "<ul>";
			for(String path : updated){
				files += "<li>" + path + "</li>";
			}
			files += "</ul>\n";
			files += "Do you want to commit these files?</div>\n";
			//Setting up div for files to check out to workarea with textarea for folder name
			files += "<div style='display: none;' id='checkedOut' title='Checkout files'>\n";
			files += "<label for='my-text'>Send files to Work Area.\n Keep or change this folder name:</label>\n";
    		files += "<textarea id='my-text'></textarea></div>\n";
			files += "<input id='checkout' type='button' value='Send to Work Area' />\n";
			//Adding the required javascript and jquery files
			files += "<link rel='stylesheet' href='http://code.jquery.com/ui/1.9.2/themes/base/jquery-ui.css'> \n";
			files += "<script type='text/javascript' src='http://code.jquery.com/jquery-1.8.3.js'></script> \n";
			files += "<script type='text/javascript' src='http://code.jquery.com/ui/1.9.2/jquery-ui.js'></script> \n" ;
			files += "<script type='text/javascript' src='/javascript/jquery-main.js'></script>";
			//Setting up div for files to be commited
			if(!updated.isEmpty()){
				files += "<script type='text/javascript' src='/javascript/jquery-commit.js'></script>";
				String commitList = "http://localhost:8088/repos/work/commit?comment=changed";
				for(String s : updated){
					commitList += "&&entry=" + s ;
				}
				files += "<div id='commitFiles' style='visibility: hidden'>" + commitList +"</div>";
			}
		return files ;
	}
	
}
