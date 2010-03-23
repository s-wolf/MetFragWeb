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
