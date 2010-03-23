package de.ipbhalle.metfrag.web.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.faces.event.ActionEvent;

import de.ipbhalle.metfrag.web.model.ResultRow;
import de.ipbhalle.metfrag.web.model.ResultRowGrouped;

public class ResultRowGroupedBean extends ResultRowGrouped {
	
	protected static final String DEFAULT_IMAGE_DIR =
	        "css/rime/css-images/";
	protected static final String SPACER_IMAGE =
	        "tree_line_blank.gif";
	
	// style for column that holds expand/contract image toggle, in the files
	// record row.
	protected String indentStyleClass = "";
	
	// style for all other columns in the files record row.
	protected String rowStyleClass = "";
	
	protected StyleBean styleBean;
	
	// Images used to represent expand/contract, spacer by default
	protected String expandImage;   // + or >
	protected String contractImage; // - or v
	
	// callback to list which contains all data in the dataTable.  This callback
	// is needed so that a node can be set in the expanded state at construction time.
	protected List<ResultRowGroupedBean> parentInventoryList;
	
	// indicates if node is in expanded state.
	protected boolean isExpanded;
	
	/**
	 * <p>Creates a new <code>FilesGroupRecordBean</code>.  This constructor
	 * should be used when creating FilesGroupRecordBeans which will contain
	 * children</p>
	 *
	 * @param isExpanded true, indicates that the specified node will be
	 *                   expanded by default; otherwise, false.
	 */
	public ResultRowGroupedBean(String indentStyleClass,
	                            String rowStyleClass,
	                            StyleBean styleBean,
	                            String expandImage,
	                            String contractImage,
	                            List<ResultRowGroupedBean> parentInventoryList,
	                            boolean isExpanded) {
	
	    this.indentStyleClass = indentStyleClass;
	    this.rowStyleClass = rowStyleClass;
	    this.styleBean = styleBean;
	    this.expandImage = expandImage;
	    this.contractImage = contractImage;
	    this.parentInventoryList = parentInventoryList;
	    this.parentInventoryList.add(this);
	    this.isExpanded = isExpanded;
	    // update the default state of the node.
	    if (this.isExpanded) {
	        expandNodeAction();
	    }
	}
	
	/**
	 * 
	 *
	 * @param indentStyleClass
	 * @param rowStyleClass
	 */
	public ResultRowGroupedBean(String indentStyleClass,
	                            String rowStyleClass) {
	
	    this.indentStyleClass = indentStyleClass;
	    this.rowStyleClass = rowStyleClass;
	}
	
	/**
	 * Gets the renderable state of the contract/expand image toggle.
	 *
	 * @return true if images should be drawn; otherwise, false.
	 */
	public boolean isRenderImage() {
	    return childResultRows != null && childResultRows.size() > 0;
	}
	
	/**
	 * Toggles the expanded state of this FilesGroup Record.
	 *
	 * @param event
	 */
	public void toggleSubGroupAction(ActionEvent event) {
	    // toggle expanded state
	    isExpanded = !isExpanded;
	
	    // add sub elements to list
	    if (isExpanded) {
	        expandNodeAction();
	    }
	    // remove items from list
	    else {
	        contractNodeAction();
	    }
	}
	
	/**
	 * Adds a child files record to this files group.
	 *
	 * @param resultRowGroup child files record to add to this record.
	 */
	public void addChildFilesGroupRecord(ResultRowGroupedBean resultRowGroup) {
	    if (this.childResultRows != null && resultRowGroup != null) {
	        this.childResultRows.add(resultRowGroup);
	        if (isExpanded) {
	            // to keep elements in order, remove all
	            contractNodeAction();
	            // then add them again.
	            expandNodeAction();
	        }
	    }
	}
	
	/**
	 * Removes the specified child files record from this files group.
	 *
	 * @param resultRowGroup child files record to remove.
	 */
	public void removeChildFilesGroupRecord(ResultRowGroupedBean resultRowGroup) {
	    if (this.childResultRows != null && resultRowGroup != null) {
	        if (isExpanded) {
	            // remove all, make sure we are removing the specified one too.
	            contractNodeAction();
	        }
	        // remove the current node
	        this.childResultRows.remove(resultRowGroup);
	        // update the list if needed.
	        if (isExpanded) {
	            // to keep elements in order, remove all
	            contractNodeAction();
	            // then add them again.
	            expandNodeAction();
	        }
	    }
	}
	
	/**
	 * Utility method to add all child nodes to the parent dataTable list.
	 */
	private void expandNodeAction() {
	    if (childResultRows != null && childResultRows.size() > 0) {
	        // get index of current node
	        int index = parentInventoryList.indexOf(this);
	
	        // add all items in childFilesRecords to the parent list
	        parentInventoryList.addAll(index + 1, childResultRows);
	    }
	
	}
	
	/**
	 * Utility method to remove all child nodes from the parent dataTable list.
	 */
	private void contractNodeAction() {
	    if (childResultRows != null && childResultRows.size() > 0) {
	        // remove all items in childFilesRecords from the parent list
	        parentInventoryList.removeAll(childResultRows);
	    }
	}
	
	/**
	 * Gets the style class name used to define the first column of a files
	 * record row.  This first column is where a expand/contract image is
	 * placed.
	 *
	 * @return indent style class as defined in css file
	 */
	public String getIndentStyleClass() {
	    return indentStyleClass;
	}
	
	/**
	 * Gets the style class name used to define all other columns in the files
	 * record row, except the first column.
	 *
	 * @return style class as defined in css file
	 */
	public String getRowStyleClass() {
	    return rowStyleClass;
	}
	
	/**
	 * Gets the image which will represent either the expanded or contracted
	 * state of the <code>FilesGroupRecordBean</code>.
	 *
	 * @return name of image to draw
	 */
	public String getExpandContractImage() {
	    if (styleBean != null) {
	        String dir = styleBean.getImageDirectory();
	        String img = isExpanded ? contractImage : expandImage;
	        return dir + img;
	    }
	    return DEFAULT_IMAGE_DIR + SPACER_IMAGE;
	}
	
	/**
     * Determines the sortColumnName order.
     *
     * @param   sortColumn to sortColumnName by.
     * @return  whether sortColumnName order is ascending or descending.
     */
    protected boolean isDefaultAscending(String sortColumn) {
        return false;
    }

}
