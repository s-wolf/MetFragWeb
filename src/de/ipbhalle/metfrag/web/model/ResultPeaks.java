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
package de.ipbhalle.metfrag.web.model;

import java.util.Vector;

public class ResultPeaks {
	
	private Vector<Double> peaks;
	private Vector<Double> intensities;
	
	
	/**
	 * Instantiates a new result peaks.
	 */
	public ResultPeaks()
	{
		this.peaks = new Vector<Double>();
		this.intensities = new Vector<Double>();
	}
		
	/**
	 * Instantiates a new result peaks.
	 * 
	 * @param peak the peak
	 * @param intensity the intensity
	 */
	public ResultPeaks(Vector<Double> peaks, Vector<Double> intensities)
	{
		this.setIntensities(intensities);
		this.setPeaks(peaks);
	}

	/**
	 * Sets the peak.
	 * 
	 * @param peak the new peak
	 */
	public void setPeaks(Vector<Double> peaks) {
		this.peaks = peaks;
	}

	/**
	 * Gets the peak.
	 * 
	 * @return the peak
	 */
	public Vector<Double> getPeaks() {
		return peaks;
	}

	/**
	 * Sets the intensity.
	 * 
	 * @param intensity the new intensity
	 */
	public void setIntensities(Vector<Double> intensities) {
		this.intensities = intensities;
	}

	/**
	 * Gets the intensity.
	 * 
	 * @return the intensity
	 */
	public Vector<Double> getIntensities() {
		return intensities;
	}
	
	
	/**
	 * Gets all peaks in a String separated by ","
	 * 
	 * @return the peaks string
	 */
	public String getPeaksString(){
		String ret = "";
		for (int i = 0; i < this.peaks.size(); i++) {
			if(i == this.peaks.size() - 1)
				ret += this.peaks.get(i).toString();
			else
				ret += this.peaks.get(i).toString() + ",";
		}
		return ret;
	}
	
	
	/**
	 * Gets all intensities in a String separated by ","
	 * 
	 * @return the peaks string
	 */
	public String getIntensitiesString(){
		String ret = "";
		for (int i = 0; i < this.intensities.size(); i++) {
			if(i == this.intensities.size() - 1)
				ret += this.intensities.get(i).toString();
			else
				ret += this.intensities.get(i).toString() + ",";
		}
		return ret;
	}
}
