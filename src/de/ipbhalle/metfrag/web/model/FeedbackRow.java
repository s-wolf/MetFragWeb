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

import java.util.Date;

public class FeedbackRow {
	
	private Integer id = 0;
	private String peaklist = "";
	private Double exactMass = 0.0;
	private Integer searchPPM = 0;
	private String molecularFormula = "";
	private String databaseUsed = "";
	private Boolean biologicalCompound = false;
	private Integer limit = 100;
	private String databaseIDs = "";
	private Integer mode = 1;
	private Double mzAbs = 0.0;
	private Integer mzPPM = 10;
	private String eMail = "";
	private String comment = "";
	private String fixed;
	private String answered;
	private Date date = null;
	
	/**
	 * Instantiates a new feedback row.
	 * 
	 * @param peaklist the peaklist
	 * @param exactMass the exact mass
	 * @param searchPPM the search ppm
	 * @param molecularFormula the molecular formula
	 * @param databaseUsed the database used
	 * @param biologicalCompound the biological compound
	 * @param limit the limit
	 * @param databaseIDs the database i ds
	 * @param mode the mode
	 * @param mzAbs the mz abs
	 * @param mzPPM the mz ppm
	 * @param eMail the e mail
	 * @param comment the comment
	 * @param fixed the fixed
	 * @param answered the answered
	 * @param date the date
	 * @param id the id
	 */
	public FeedbackRow(Integer id, String peaklist, Double exactMass, Integer searchPPM, String molecularFormula, String databaseUsed, Boolean biologicalCompound, Integer limit, String databaseIDs, Integer mode, Double mzAbs, Integer mzPPM, String eMail, String comment, String fixed, String answered, Date date)
	{
		this.id = id;
		this.peaklist = peaklist;
		this.exactMass = exactMass;
		this.searchPPM = searchPPM;
		this.molecularFormula = molecularFormula;
		this.databaseUsed = databaseUsed;
		this.biologicalCompound = biologicalCompound;
		this.limit = limit;
		this.databaseIDs = databaseIDs;
		this.mode = mode;
		this.mzAbs = mzAbs;
		this.mzPPM = mzPPM;
		this.eMail = eMail;
		this.comment = comment;
		this.fixed = fixed;
		this.answered = answered;
		this.date = date;
	}

	public String getPeaklist() {
		return peaklist;
	}

	public void setPeaklist(String peaklist) {
		this.peaklist = peaklist;
	}

	public Double getExactMass() {
		return exactMass;
	}

	public void setExactMass(Double exactMass) {
		this.exactMass = exactMass;
	}

	public Integer getSearchPPM() {
		return searchPPM;
	}

	public void setSearchPPM(Integer searchPPM) {
		this.searchPPM = searchPPM;
	}

	public String getMolecularFormula() {
		return molecularFormula;
	}

	public void setMolecularFormula(String molecularFormula) {
		this.molecularFormula = molecularFormula;
	}

	public String getDatabaseUsed() {
		return databaseUsed;
	}

	public void setDatabaseUsed(String databaseUsed) {
		this.databaseUsed = databaseUsed;
	}

	public Boolean getBiologicalCompound() {
		return biologicalCompound;
	}

	public void setBiologicalCompound(Boolean biologicalCompound) {
		this.biologicalCompound = biologicalCompound;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public String getDatabaseIDs() {
		return databaseIDs;
	}

	public void setDatabaseIDs(String databaseIDs) {
		this.databaseIDs = databaseIDs;
	}

	public Integer getMode() {
		return mode;
	}

	public void setMode(Integer mode) {
		this.mode = mode;
	}

	public Double getMzAbs() {
		return mzAbs;
	}

	public void setMzAbs(Double mzAbs) {
		this.mzAbs = mzAbs;
	}

	public Integer getMzPPM() {
		return mzPPM;
	}

	public void setMzPPM(Integer mzPPM) {
		this.mzPPM = mzPPM;
	}

	public String geteMail() {
		return eMail;
	}

	public void seteMail(String eMail) {
		this.eMail = eMail;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getFixed() {
		return fixed;
	}

	public void setFixed(String fixed) {
		this.fixed = fixed;
	}

	public String getAnswered() {
		return answered;
	}

	public void setAnswered(String answered) {
		this.answered = answered;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}
}
