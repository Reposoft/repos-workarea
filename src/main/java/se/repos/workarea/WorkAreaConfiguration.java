/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.workarea;

/**
 * Each user may have her own work area configuration, including storage type and credentials.
 */
public interface WorkAreaConfiguration {

	/**
	 * @return work area implementation, typically different class and instance per user
	 */
	WorkArea getWorkArea();
	
}
