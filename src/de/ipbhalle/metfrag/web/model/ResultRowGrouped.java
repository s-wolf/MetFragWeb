package de.ipbhalle.metfrag.web.model;

import java.util.ArrayList;
import java.util.List;

import de.ipbhalle.metfrag.web.controller.ResultRowGroupedBean;

public class ResultRowGrouped extends ResultRow{
	
	protected List<ResultRowGroupedBean> childResultRows = new ArrayList<ResultRowGroupedBean>();
	
	public List<ResultRowGroupedBean> getChildResultRows(){
		return childResultRows;
	}

}
