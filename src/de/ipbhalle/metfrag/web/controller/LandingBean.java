package de.ipbhalle.metfrag.web.controller;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import com.icesoft.faces.context.effects.JavascriptContext;
import com.icesoft.faces.webapp.xmlhttp.PersistentFacesState;

public class LandingBean {
	
	private String peaks;
	private String parsedPeaks;
	private String peaksHTML;
	private String mass;
	private String molecularFormula;
	private HttpServletRequest requestMap;
	private boolean error;
	private boolean forward;
	

	public LandingBean()
	{
		forward = false;
	}

	//initialize
	public void init()
	{
		error = false;
		requestMap = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		String[] peakArray = (String[])requestMap.getParameterMap().get("peaks");
		if(peakArray == null)
			error = true;
		else
		{
			peaks = peakArray[0];
			peaksHTML = peaks.replaceAll(";", "<br />");
			parsePeaksGET();
		}
		
		String[] massArray = (String[]) requestMap.getParameterMap().get("mass");
		if(massArray == null)
			error = true;
		else
			mass = massArray[0];
		
		String[] formulaArray = (String[]) requestMap.getParameterMap().get("formula");
		if(formulaArray != null)
			molecularFormula = formulaArray[0];
		
		PersistentFacesState persistentFacesState = PersistentFacesState.getInstance();
        FacesContext facesContext = persistentFacesState.getFacesContext();
        Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
        sessionMap.put("landingBean", this);
	}
	
	
	private void parsePeaksGET()
	{
		this.parsedPeaks = this.peaks.replaceAll(";", "\n");
	}
	
	/**
	 * Validate the GET data.
	 *
	 * @return the string
	 */
	public String validate()
	{
		if(error)
			return "failed";
		else
			return "success";
	}
	
	/* Getters + Setters */
	public String getLanguage() {
		return peaks;
	}

	public void setLanguage(String language) {
		this.peaks = language;
	}

	public void setMass(String mass) {
		this.mass = mass;
	}

	/**
	 * Gets the mass.
	 *
	 * @return the mass
	 */
	public String getMass() {
		if(!forward)
			init();
		return mass;
	}

	public void setMolecularFormula(String molecularFormula) {
		this.molecularFormula = molecularFormula;
	}

	/**
	 * Gets the molecular formula.
	 *
	 * @return the molecular formula
	 */
	public String getMolecularFormula() {
		return molecularFormula;
	}
	
	/**
	 * Gets the peaks.
	 *
	 * @return the peaks
	 */
	public String getPeaks() {
		return peaks;
	}

	public void setPeaks(String peaks) {
		this.peaks = peaks;
	}
	
	public boolean isForward() {
		return forward;
	}

	public void setForward(boolean forward) {
		this.forward = forward;
	}

	public void setParsedPeaks(String parsedPeaks) {
		this.parsedPeaks = parsedPeaks;
	}

	public String getParsedPeaks() {
		return parsedPeaks;
	}

	public void setPeaksHTML(String peaksHTML) {
		this.peaksHTML = peaksHTML;
	}

	public String getPeaksHTML() {
		return peaksHTML;
	}
	
}
