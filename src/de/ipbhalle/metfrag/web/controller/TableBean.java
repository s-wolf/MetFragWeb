package de.ipbhalle.metfrag.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import de.ipbhalle.metfrag.web.model.ResultPic;
import de.ipbhalle.metfrag.web.model.ResultRow;
import de.ipbhalle.metfrag.web.model.SortableList;

public class TableBean extends SortableList {
	
	// dataTableColumn Names
    private static final String scoreCol = "Score";
    private static final String explainedPeaksCol = "# Explained Peaks";
    private static final String databaseIDCol = "Database ID";
    
    private ResultRow[] results = null;
    private List<ResultRow> resultsList = new ArrayList<ResultRow>();
    
	
	public TableBean() {
        super(scoreCol);
    }
	
	
	public void addRow(String keggID, String molName, String image, int explainedPeaks, double score, List<ResultPic> frags, String molecularFormula, String mass, String databaseLink, String peaksFound, String peaksNotFound, String peaksNotUsed, String peaksFoundInt, String peaksNotFoundInt, String peaksNotUsedInt)
	{
		resultsList.add(new ResultRow(keggID, molName, image, explainedPeaks, score, frags, molecularFormula, mass, databaseLink, peaksFound, peaksNotFound, peaksNotUsed, peaksFoundInt, peaksNotFoundInt, peaksNotUsedInt));
	}
	
	/**
     * Determines the sortColumnName order.
     *
     * @param   sortColumn to sortColumnName by.
     * @return  whether sortColumnName order is ascending or descending.
     */
    protected boolean isDefaultAscending(String sortColumn) {
        return true;
    }
	
	/**
     * Gets the inventoryItem array of car data.
     * @return array of car inventory data.
     */
    public ResultRow[] getResultRows() {
		
    	results = new ResultRow[resultsList.size()];
		results = resultsList.toArray(results);
		
        // we only want to sortColumnName if the column or ordering has changed.
        if (!oldSort.equals(sortColumnName) ||
                oldAscending != ascending){
             sort();
             oldSort = sortColumnName;
             oldAscending = ascending;
        }
        return results;
    }
	
	protected void sort() {
		
		results = new ResultRow[resultsList.size()];
		results = resultsList.toArray(results);
		
        Comparator comparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                ResultRow c1 = (ResultRow) o1;
                ResultRow c2 = (ResultRow) o2;
                if (sortColumnName == null) {
                    return 0;
                }
                if (sortColumnName.equals(scoreCol)) {
                    return ascending ?
                    c1.getScore().compareTo(c2.getScore()) :
                    c2.getScore().compareTo(c1.getScore());
                } else if (sortColumnName.equals(explainedPeaksCol)) {
                    return ascending ? Integer.valueOf(c1.getExplainedPeaks()).compareTo(Integer.valueOf(c2.getExplainedPeaks())) :
                    Integer.valueOf(c2.getExplainedPeaks()).compareTo(Integer.valueOf(c1.getExplainedPeaks()));
                } else if (sortColumnName.equals(databaseIDCol)) {
                    return ascending ? c1.getID().compareTo(c2.getID()) :
                    c2.getID().compareTo(c1.getID());
                }  else return 0;
            }
        };
        Arrays.sort(results, comparator);
    }


}
