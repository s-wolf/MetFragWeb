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

import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfrag.spectrum.WrapperSpectrum;


public class MetFragObject {
	
	private boolean stop = false;
	private String database = "";
	private String databaseID = "";
	private String candidate = "";
	private IAtomContainer molecule = null;
	private Double mzabs = 0.0;
	private Double mzppm = 0.0;
	private boolean molFormulaRedundancyCheck;
	private int mode = 1;
	private WrapperSpectrum spectrum = null;
	private boolean bondEnergyScoring = false;
	private String treeDepth = "";
	
	public MetFragObject()
	{
		
	}
	
	public boolean isStop() {
		return stop;
	}


	public void setStop(boolean stop) {
		this.stop = stop;
	}


	public String getDatabase() {
		return database;
	}


	public void setDatabase(String database) {
		this.database = database;
	}


	public String getDatabaseID() {
		return databaseID;
	}


	public void setDatabaseID(String databaseID) {
		this.databaseID = databaseID;
	}


	public String getCandidate() {
		return candidate;
	}


	public void setCandidate(String candidate) {
		this.candidate = candidate;
	}


	public Double getMzabs() {
		return mzabs;
	}


	public void setMzabs(Double mzabs) {
		this.mzabs = mzabs;
	}


	public Double getMzppm() {
		return mzppm;
	}


	public void setMzppm(Double mzppm) {
		this.mzppm = mzppm;
	}

	public void setMolecule(IAtomContainer molecule) {
		this.molecule = molecule;
	}

	public IAtomContainer getMolecule() {
		return molecule;
	}

	public void setMolFormulaRedundancyCheck(boolean molFormulaRedundancyCheck) {
		this.molFormulaRedundancyCheck = molFormulaRedundancyCheck;
	}

	public boolean isMolFormulaRedundancyCheck() {
		return molFormulaRedundancyCheck;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getMode() {
		return mode;
	}

	public void setSpectrum(WrapperSpectrum spectrum) {
		this.spectrum = spectrum;
	}

	public WrapperSpectrum getSpectrum() {
		return spectrum;
	}

	public void setBondEnergyScoring(boolean bondEnergyScoring) {
		this.bondEnergyScoring = bondEnergyScoring;
	}

	public boolean isBondEnergyScoring() {
		return bondEnergyScoring;
	}

	public void setTreeDepth(String treeDepth) {
		this.treeDepth = treeDepth;
	}

	public String getTreeDepth() {
		return treeDepth;
	}

}
