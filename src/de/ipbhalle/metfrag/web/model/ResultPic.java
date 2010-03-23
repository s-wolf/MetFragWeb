package de.ipbhalle.metfrag.web.model;

public class ResultPic {
	
	private String path = "";
	private String mass = "0";
	private String molecularFormula = "";
	
	public ResultPic(String path, String mass, String molecularFormula)
	{
		this.setMolecularFormula(molecularFormula);
		this.mass = mass;
		this.path = path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setMass(String mass) {
		this.mass = mass;
	}

	public String getMass() {
		return mass;
	}

	public void setMolecularFormula(String molecularFormula) {
		this.molecularFormula = molecularFormula;
	}

	public String getMolecularFormula() {
		return molecularFormula;
	}
	

}
