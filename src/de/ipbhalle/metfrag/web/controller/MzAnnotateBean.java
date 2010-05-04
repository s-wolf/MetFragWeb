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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.xml.stream.XMLStreamException;

import org.metware.mzAnnotate.Fragment;
import org.metware.mzAnnotate.FragmentList;
import org.metware.mzAnnotate.MzAnnotate;
import org.metware.mzAnnotate.MzAnnotateReader;
import org.metware.mzAnnotate.SpectrumData;
import org.metware.mzAnnotate.Tools;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import com.icesoft.faces.context.effects.JavascriptContext;
import com.icesoft.faces.webapp.xmlhttp.PersistentFacesState;
import com.icesoft.faces.webapp.xmlhttp.RenderingException;

import de.ipbhalle.metfrag.TEMP.PeakMzAnno;
import de.ipbhalle.metfrag.tools.renderer.StructureToFile;

public class MzAnnotateBean {
	
	private String mzAnnotate = "";
	private String mzAnnoResultFragments = "";
	private String mzAnnoResultSpectrum = "";
	private String mzAnnoPeaksMZ = "";
	private String mzAnnoPeaksInt = "";
	private boolean displayResult = false;
	private static final String FRAGMENT_PICS_MZANNO = "FragmentPicsMzAnno";
	private String sep = System.getProperty("file.separator");
	
	public String mzAnnotateEx1()
	{
		FacesContext fc = FacesContext.getCurrentInstance();
		ServletContext scontext = (ServletContext) fc.getExternalContext().getContext();
		String webRoot = scontext.getRealPath(sep);	
		String pathToFile = webRoot + "MassBank.xml";
		
		String result = "";
		try {
	        BufferedReader in = new BufferedReader(new FileReader(pathToFile));
	        String str;
	        while ((str = in.readLine()) != null) {
	            result += str + "\n";
	        }
	        in.close();
	    } catch (IOException e) {
	    }
	    
	    mzAnnotate = result;
	    
		return "";
	}
	
	public String mzAnnotateEx2()
	{
		FacesContext fc = FacesContext.getCurrentInstance();
		ServletContext scontext = (ServletContext) fc.getExternalContext().getContext();
		String webRoot = scontext.getRealPath(sep);	
		String pathToFile = webRoot + "MassBankOnlySpectrum.xml";
		
		String result = "";
		try {
	        BufferedReader in = new BufferedReader(new FileReader(pathToFile));
	        String str;
	        while ((str = in.readLine()) != null) {
	            result += str + "\n";
	        }
	        in.close();
	    } catch (IOException e) {
	    }
	    
	    mzAnnotate = result;
	    
		return "";
	}
	
	public String mzAnnotateEx3()
	{
		FacesContext fc = FacesContext.getCurrentInstance();
		ServletContext scontext = (ServletContext) fc.getExternalContext().getContext();
		String webRoot = scontext.getRealPath(sep);	
		String pathToFile = webRoot + "MassBankAssigned.xml";
		
		String result = "";
		try {
	        BufferedReader in = new BufferedReader(new FileReader(pathToFile));
	        String str;
	        while ((str = in.readLine()) != null) {
	            result += str + "\n";
	        }
	        in.close();
	    } catch (IOException e) {
	    }
	    
	    mzAnnotate = result;
	    
		return "";
	}
    
	
	public String viewMzAnnotate(ActionEvent ae)
	{
		if(mzAnnotate == "")
		{
			setMzAnnoResultFragments("");
			setMzAnnoResultSpectrum("Error! No valid mzAnnotate file!");
			return "";
		}
		else
		{
			//TODO: check file!
			MzAnnotateReader reader = new MzAnnotateReader();
			try {
				MzAnnotate mzAnnot = reader.readMzAnnotateFromString(mzAnnotate);
				
				setMzAnnoResultSpectrum(getSpectrumDataHTML(mzAnnot.getSpecData(), mzAnnot.getAssignedFragments()));
				setMzAnnoResultFragments(getFragmentDataHTML(mzAnnot.getFragMap()));
				
			} catch (FileNotFoundException e) {
				setMzAnnoResultSpectrum("Error reading file!!");
				e.printStackTrace();
			} catch (XMLStreamException e) {
				setMzAnnoResultSpectrum("Error! No valid mzAnnotate file!\n\n" + e.getStackTrace().toString());
				e.printStackTrace();
			} catch (CDKException e) {
				setMzAnnoResultSpectrum("Error! CDK Exception!\n\n" + e.getStackTrace().toString());
				e.printStackTrace();
			} catch (Exception e) {
				setMzAnnoResultSpectrum("Error! Picture Generation!\n\n" + e.getStackTrace().toString());
				e.printStackTrace();
			}
			
			
		}
		setDisplayResult(true);
		JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "drawSpectrum(800, 200, 'spectrum', [" + mzAnnoPeaksMZ + "],[" + mzAnnoPeaksInt + "],[],[],[],[]);");

		return "";
	}
	
	
	private String getSpectrumDataHTML(SpectrumData spectData, HashMap<PeakMzAnno, List<String>> assignedPeaks)
	{
		//set the peak data
		Vector<Double> peaks = new Vector<Double>();
		Vector<Double> intensities = new Vector<Double>();
		for (PeakMzAnno peak : spectData.getPeakList()) {
			peaks.add(peak.getMass());
			intensities.add(peak.getIntensity());
		}
		setMzAnnoPeaksMZ(getPeaksString(peaks));
		setMzAnnoPeaksInt(getIntensitiesString(intensities));
		
		String htmlOutput = "<div id=\"spectrumData\">";
		htmlOutput += "Exact Mass: " + spectData.getExactMass() + "<br />";
		htmlOutput += "Collision Energy: " + spectData.getCollisionEnergy() + " eV<br />";
		if(spectData.getMode() == 1)
			htmlOutput += "Mode: positive<br />";
		else if(spectData.getMode() == -1)
			htmlOutput += "Mode: negative<br />";
		String peakListHTML = "<div id=\"peakList\">Peaklist:<br />";
		for (PeakMzAnno peak : spectData.getPeakList()) {
			String molRef = "";
			if(assignedPeaks.get(peak) != null)
			{
				for (String ref : assignedPeaks.get(peak)) {
					molRef += ref + " ";
				}
			}
			peakListHTML += peak.getMass() + "<span style=\"margin-left: 10px;\">" + peak.getIntensity() + "</span><span style=\"margin-left: 10px;\">" + molRef + "</span><br />";
		}
		peakListHTML += "</div>";
		
		htmlOutput += peakListHTML;
		htmlOutput += "</div>";
		return htmlOutput;
	}
	
	
	private String getFragmentDataHTML(FragmentList fragList) throws Exception
	{
		if(!fragList.isFragGiven() && !fragList.isStructureGiven())
			return "";
		
		
		String htmlOutput = "";
		//get folder names and session ID
        FacesContext fc = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fc.getExternalContext().getSession(false);
		String sessionString = session.getId();
		
		
		int count = 0;
		ServletContext scontext = (ServletContext) fc.getExternalContext().getContext();
		String webRoot = scontext.getRealPath(sep);	
		String currentFolder = webRoot + sep + FRAGMENT_PICS_MZANNO + sep + sessionString + sep + "MzAnnotPics" + sep;
		new File(currentFolder).mkdirs();
		
		Fragment fragMeasured = Tools.getMeasuredCompound(fragList.getFragList());
		if(fragMeasured.isStructureKnown())
		{
			StructureToFile dsvOrigLarge = new StructureToFile(250,250, currentFolder, false, true);
//			DisplayStructureVector dsvOrigLarge = new DisplayStructureVector(250,250, currentFolder, false, true);
			dsvOrigLarge.writeMOL2PNGFile(fragMeasured.getMolecule(), "mzAnnoMeasured_" + count + ".png");
			htmlOutput += "<table cellspcing='0' cellpadding='2' border='0'>" +
							"<tr>" +
								"<td> Measured compound: </td>" +
								"<td>" + "<img src='." + sep + FRAGMENT_PICS_MZANNO + sep + sessionString + sep + "MzAnnotPics" + sep + "mzAnnoMeasured_" + count + ".png" + "' alt='Measured Compound' />" + "</td>" +
							"</tr>" +
						   "</table>";
			count++;				
		}
		else if(fragList.isStructureGiven())
		{
			htmlOutput += "<table cellspcing='0' cellpadding='2' border='0'>" +
			"<tr>" +
				"<td><strong> Measured compound: </strong></td>" +
				"<td>" + MolecularFormulaManipulator.getHTML(fragMeasured.getMolecularFormula()) + "</td>" +
			"</tr>" +
		   "</table>";
		}
		
		StructureToFile dsv = new StructureToFile(200,200, currentFolder, false, true);
//		DisplayStructureVector dsv = new DisplayStructureVector(200,200, currentFolder, false, true);
		htmlOutput += "<table cellspacing='0' cellpadding='2' border='0'><tr>";
		int numCols = 3;
		int colCount = 0;
		
		for (String string : fragList.getFragList().keySet()) {
			Fragment currentFrag = fragList.getFragList().get(string);
			if(currentFrag.isMeasuredCompound())
				continue;
			else if(currentFrag.isStructureKnown())
			{
				//show structure
				
				dsv.writeMOL2PNGFile(currentFrag.getMolecule(), "mzAnno_" + count + ".png");
				htmlOutput += "<td> Fragment " + currentFrag.getMolecule().getID() + ": </td>" +
									"<td>" + "<img src='." + sep + FRAGMENT_PICS_MZANNO + sep + sessionString + sep + "MzAnnotPics" + sep + "mzAnno_" + count + ".png" + "' alt='Fragment' />" + "</td>";
			}
			else
			{
				//only show molecular formula
				htmlOutput += "<td> Fragment " + count + ": </td>" +
					"<td>" + MolecularFormulaManipulator.getHTML(currentFrag.getMolecularFormula()) + "</td>";
			}
			colCount++;
			count++;
			if(numCols == colCount)
			{
				colCount = 0;
				htmlOutput += "</tr><tr>";
			}
		}
		htmlOutput += "</tr></table>";

		count++;
		return htmlOutput;
	}
	
	/**
	 * Gets all peaks in a String separated by ","
	 * 
	 * @return the peaks string
	 */
	private String getPeaksString(Vector<Double> peaks){
		String ret = "";
		for (int i = 0; i < peaks.size(); i++) {
			if(i == peaks.size() - 1)
				ret += peaks.get(i).toString();
			else
				ret += peaks.get(i).toString() + ",";
		}
		return ret;
	}
	
	/**
	 * Gets all intensities in a String separated by ","
	 * 
	 * @return the peaks string
	 */
	private String getIntensitiesString(Vector<Double> intensities){
		String ret = "";
		for (int i = 0; i < intensities.size(); i++) {
			if(i == intensities.size() - 1)
				ret += intensities.get(i).toString();
			else
				ret += intensities.get(i).toString() + ",";
		}
		return ret;
	}

	public void setMzAnnoResultFragments(String mzAnnoResultFragments) {
		this.mzAnnoResultFragments = mzAnnoResultFragments;
	}

	public String getMzAnnoResultFragments() {
		return mzAnnoResultFragments;
	}

	public void setMzAnnoResultSpectrum(String mzAnnoResultSpectrum) {
		this.mzAnnoResultSpectrum = mzAnnoResultSpectrum;
	}

	public String getMzAnnoResultSpectrum() {
		return mzAnnoResultSpectrum;
	}

	public void setMzAnnoPeaksMZ(String mzAnnoPeaksMZ) {
		this.mzAnnoPeaksMZ = mzAnnoPeaksMZ;
	}

	public String getMzAnnoPeaksMZ() {
		return mzAnnoPeaksMZ;
	}

	public void setMzAnnoPeaksInt(String mzAnnoPeaksInt) {
		this.mzAnnoPeaksInt = mzAnnoPeaksInt;
	}

	public String getMzAnnoPeaksInt() {
		return mzAnnoPeaksInt;
	}

	public String getMzAnnotate() {
		return mzAnnotate;
	}

	public void setMzAnnotate(String mzAnnotate) {
		this.mzAnnotate = mzAnnotate;
	}

	public void setDisplayResult(boolean displayResult) {
		this.displayResult = displayResult;
	}

	public boolean isDisplayResult() {
		return displayResult;
	}


}
