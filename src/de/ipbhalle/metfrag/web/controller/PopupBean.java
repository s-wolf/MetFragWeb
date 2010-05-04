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
package de.ipbhalle.metfrag.web.controller;

import java.io.Serializable;

/**
 * Class used to allow the dynamic opening and closing of panelPopups
 * That means the visibility status is tracked, as well as supporting
 *  methods for button clicks on the page
 */
public class PopupBean implements Serializable {
	
	private boolean visible = false;
	private boolean visible1 = false;
	private boolean visible2 = false;
    
	public boolean isVisible() { return visible; }
    public boolean isVisible1() { return visible1; }
    public boolean isVisible2() { return visible2; }
    
    public void setVisible(boolean visible) { this.visible = visible; }
    public void setVisible1(boolean visible1) { this.visible1 = visible1; }
    public void setVisible2(boolean visible2) { this.visible2 = visible2; }
    
    
    public void closePopup() {
        visible = false;
    }
    
    public void openPopup() {
        visible = true;
    }
    
    public void closePopup1() {
        visible1 = false;
    }
    
    public void openPopup1() {
        visible1 = true;
    }
    
    public void closePopup2() {
        visible2 = false;
    }
    
    public void openPopup2() {
        visible2 = true;
    }

}

