/*
*
* Copyright (C) 2009-2010 IPB Halle, Sebastian Wolf
*
* Contact: swolf@ipb-halle.de
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*
*/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ipbhalle.metfrag.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.io.File;

/**
 *
 * @author mgerlich
 */
public class PictureCleaner implements HttpSessionListener {

    public static final Log log = LogFactory.getLog(PictureCleaner.class);
    private String sep = System.getProperty("file.separator");
    public static final String FILE_UPLOAD_DIRECTORY = "FragmentPics";

    /**
     * This method is called by the servlet container when the session
     * is about to expire. This method will attempt to delete all files that
     * where uploaded into the folder which has the same name as the session
     * id.
     *
     * @param event JSF session event.
     */
    public void sessionDestroyed(HttpSessionEvent event) {
        	
    	// get the session id, so we know which folder to remove
        String sessionId = event.getSession().getId();

        String applicationPath = event.getSession().getServletContext().getRealPath(
                event.getSession().getServletContext().getServletContextName());

        String picDirectory =
                applicationPath + sep + FILE_UPLOAD_DIRECTORY + sep + sessionId;
        
        System.out.println("Dir: " + picDirectory);
        
        File picDir = new File(picDirectory);

        if (picDir.isDirectory()) {
            try {
            	//recursively delete complete folder
        		boolean isDeleted = deleteDir(picDir);
        		System.out.println("Pictures with with Session: " + sessionId + " were deleted? " + isDeleted);
                //picDir.delete();
            }
            catch (SecurityException e) {
                log.error("Error deleting file upload directory: ", e);
            }
        }

    }

    public void sessionCreated(HttpSessionEvent event) {

    }
    
    /**
	 * Deletes all files and subdirectories under dir.
     * Returns true if all deletions were successful.
     * If a deletion fails, the method stops attempting to delete and returns false.
	 * 
	 * @param dir the dir
	 * 
	 * @return true, if successful
	 */
	private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    }
}