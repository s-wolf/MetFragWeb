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
