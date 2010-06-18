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

import java.io.Serializable;
import java.util.List;


// TODO: Auto-generated Javadoc
/**
 * The Class Result.
 */
public class ResultRow implements Serializable{
	
	private String ID;
	private String databaseLink;
	private String molName;
	private String image;
	private int explainedPeaks;
	private Double score;
	private String mass;
	private String peaksFound;
	private String peaksNotFound;
	private String peaksNotUsed;
	private String peaksFoundInt;
	private String peaksNotFoundInt;
	private String peaksNotUsedInt;
	private List<ResultPic> frags;
	private Double bondEnergy;
	private Double hydrogenPenalty;
	private String molecularFormula;
	private String smiles;
	
	
	/**
	 * Instantiates a new result from MetFrag.
	 * 
	 * @param molName the mol name
	 * @param image the image
	 * @param explainedPeaks the explained peaks
	 * @param score the score
	 * @param frags the frags
	 * @param mass the mass
	 * @param keggID the kegg id
	 * @param databaseLink the database link
	 * @param peaksFound the peaks found
	 * @param peaksNotFound the peaks not found
	 * @param peaksNotUsed the peaks not used
	 * @param peaksFoundInt the peaks found int
	 * @param peaksNotFoundInt the peaks not found int
	 * @param peaksNotUsedInt the peaks not used int
	 */
	public ResultRow(String keggID, String molName, String image, int explainedPeaks, Double score, List<ResultPic> frags, String molecularFormula, String mass, String databaseLink, String peaksFound, String peaksNotFound, String peaksNotUsed, String peaksFoundInt, String peaksNotFoundInt, String peaksNotUsedInt, String smiles)
	{
		this.setID(keggID);
		this.setMolName(molName);
		this.setImage(image);
		this.setExplainedPeaks(explainedPeaks);
		this.setScore(score);
		this.setFrags(frags);
		this.setMass(mass);
		this.setDatabaseLink(databaseLink);
		this.setPeaksFound(peaksFound);
		this.setPeaksNotFound(peaksNotFound);
		this.setPeaksNotUsed(peaksNotUsed);
		this.setPeaksFoundInt(peaksFoundInt);
		this.setPeaksNotFoundInt(peaksNotFoundInt);
		this.setPeaksNotUsedInt(peaksNotUsedInt);
		this.setMolecularFormula(molecularFormula);
		this.setSmiles(smiles);
	}
	
	
	/**
	 * Instantiates a new result row with bond energy and hydrogen penalty
	 * 
	 * @param keggID the kegg id
	 * @param molName the mol name
	 * @param image the image
	 * @param explainedPeaks the explained peaks
	 * @param score the score
	 * @param bondEnergy the bond energy
	 * @param hydrogenPenalty the hydrogen penalty
	 * @param frags the frags
	 * @param mass the mass
	 * @param databaseLink the database link
	 * @param peaksFound the peaks found
	 * @param peaksNotFound the peaks not found
	 * @param peaksNotUsed the peaks not used
	 * @param peaksFoundInt the peaks found int
	 * @param peaksNotFoundInt the peaks not found int
	 * @param peaksNotUsedInt the peaks not used int
	 */
	public ResultRow(String keggID, String molName, String image, int explainedPeaks, Double score, Double bondEnergy, Double hydrogenPenalty, List<ResultPic> frags, String molecularFormula, String mass, String databaseLink, String peaksFound, String peaksNotFound, String peaksNotUsed, String peaksFoundInt, String peaksNotFoundInt, String peaksNotUsedInt, String smiles)
	{
		this.setID(keggID);
		this.setMolName(molName);
		this.setImage(image);
		this.setExplainedPeaks(explainedPeaks);
		this.setScore(score);
		this.setFrags(frags);
		this.setMass(mass);
		this.setDatabaseLink(databaseLink);
		this.setPeaksFound(peaksFound);
		this.setPeaksNotFound(peaksNotFound);
		this.setPeaksNotUsed(peaksNotUsed);
		this.setPeaksFoundInt(peaksFoundInt);
		this.setPeaksNotFoundInt(peaksNotFoundInt);
		this.setPeaksNotUsedInt(peaksNotUsedInt);
		this.setBondEnergy(bondEnergy);
		this.setHydrogenPenalty(hydrogenPenalty);
		this.setMolecularFormula(molecularFormula);
		this.setSmiles(smiles);
	}
	
	public ResultRow(){}
	
	/**
	 * Sets the score.
	 * 
	 * @param score the new score
	 */
	public void setScore(Double score) {
		this.score = score;
	}

	/**
	 * Gets the score.
	 * 
	 * @return the score
	 */
	public Double getScore() {
		return score;
	}

	/**
	 * Sets the explained peaks.
	 * 
	 * @param explainedPeaks the new explained peaks
	 */
	public void setExplainedPeaks(int explainedPeaks) {
		this.explainedPeaks = explainedPeaks;
	}

	/**
	 * Gets the explained peaks.
	 * 
	 * @return the explained peaks
	 */
	public int getExplainedPeaks() {
		return explainedPeaks;
	}

	/**
	 * Sets the image.
	 * 
	 * @param image the new image
	 */
	public void setImage(String image) {
		this.image = image;
	}

	/**
	 * Gets the image.
	 * 
	 * @return the image
	 */
	public String getImage() {
		return image;
	}

	/**
	 * Sets the mol name.
	 * 
	 * @param molName the new mol name
	 */
	public void setMolName(String molName) {
		this.molName = molName;
	}

	/**
	 * Gets the mol name.
	 * 
	 * @return the mol name
	 */
	public String getMolName() {
		return molName;
	}

	/**
	 * Sets the kegg id.
	 * 
	 * @param keggID the new kegg id
	 */
	public void setID(String ID) {
		this.ID = ID;
	}

	/**
	 * Gets the kegg id.
	 * 
	 * @return the kegg id
	 */
	public String getID() {
		return ID;
	}


	/**
	 * Sets the frags.
	 * 
	 * @param frags the new frags
	 */
	public void setFrags(List<ResultPic> frags) {
		this.frags = frags;
	}


	/**
	 * Gets the frags.
	 * 
	 * @return the frags
	 */
	public List<ResultPic> getFrags() {
		return frags;
	}


	/**
	 * Sets the mass.
	 * 
	 * @param mass the new mass
	 */
	public void setMass(String mass) {
		this.mass = mass;
	}


	/**
	 * Gets the mass.
	 * 
	 * @return the mass
	 */
	public String getMass() {
		return mass;
	}

	/**
	 * Sets the database link.
	 * 
	 * @return the databse link
	 */
	public void setDatabaseLink(String databaseLink) {
		this.databaseLink = databaseLink;
	}

	/**
	 * Gets the database link.
	 * 
	 * @return the database link
	 */
	public String getDatabaseLink() {
		return databaseLink;
	}


	public void setPeaksFound(String peaksFound) {
		this.peaksFound = peaksFound;
	}


	public String getPeaksFound() {
		return peaksFound;
	}


	public void setPeaksNotFound(String peaksNotFound) {
		this.peaksNotFound = peaksNotFound;
	}


	public String getPeaksNotFound() {
		return peaksNotFound;
	}


	public void setPeaksNotUsed(String peaksNotUsed) {
		this.peaksNotUsed = peaksNotUsed;
	}


	public String getPeaksNotUsed() {
		return peaksNotUsed;
	}

	public void setPeaksFoundInt(String peaksFoundInt) {
		this.peaksFoundInt = peaksFoundInt;
	}

	public String getPeaksFoundInt() {
		return peaksFoundInt;
	}

	public void setPeaksNotFoundInt(String peaksNotFoundInt) {
		this.peaksNotFoundInt = peaksNotFoundInt;
	}

	public String getPeaksNotFoundInt() {
		return peaksNotFoundInt;
	}

	public void setPeaksNotUsedInt(String peaksNotUsedInt) {
		this.peaksNotUsedInt = peaksNotUsedInt;
	}

	public String getPeaksNotUsedInt() {
		return peaksNotUsedInt;
	}

	public void setBondEnergy(Double bondEnergy) {
		this.bondEnergy = bondEnergy;
	}

	public Double getBondEnergy() {
		return bondEnergy;
	}

	public void setHydrogenPenalty(Double hydrogenPenalty) {
		this.hydrogenPenalty = hydrogenPenalty;
	}

	public Double getHydrogenPenalty() {
		return hydrogenPenalty;
	}


	public void setMolecularFormula(String molecularFormula) {
		this.molecularFormula = molecularFormula;
	}


	public String getMolecularFormula() {
		return molecularFormula;
	}


	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}


	public String getSmiles() {
		return smiles;
	}
	
	

}
