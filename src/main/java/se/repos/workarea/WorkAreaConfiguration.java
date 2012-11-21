/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.workarea;

/**
 * Each user may have her own work area configuration, including storage type and credentials.
 */
public interface WorkAreaConfiguration {

	/**
	 * Retrieves configured impl for a specific user.
	 * @param username Login name
	 * @return This user's work area
	 */
	WorkArea getWorkArea(String username);
	
}
