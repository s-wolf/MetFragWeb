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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.faces.component.UICommand;
import javax.faces.component.UIForm;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.MoleculeSet;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.formula.MolecularFormula;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IBond.Stereo;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import com.icesoft.faces.component.inputfile.FileInfo;
import com.icesoft.faces.component.inputfile.InputFile;
import com.icesoft.faces.context.Resource;
import com.icesoft.faces.context.effects.JavascriptContext;
import com.icesoft.faces.webapp.xmlhttp.PersistentFacesState;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import de.ipbhalle.metfrag.buildinfo.BuildInfo;
import de.ipbhalle.metfrag.chemspiderClient.ChemSpider;
import de.ipbhalle.metfrag.keggWebservice.KeggWebservice;
import de.ipbhalle.metfrag.spectrum.PeakMolPair;
import de.ipbhalle.metfrag.main.MetFrag;
import de.ipbhalle.metfrag.massbankParser.Peak;
import de.ipbhalle.metfrag.molDatabase.BeilsteinLocal;
import de.ipbhalle.metfrag.molDatabase.PubChemLocal;
import de.ipbhalle.metfrag.pubchem.PubChemWebService;
import de.ipbhalle.metfrag.read.SDFFile;
import de.ipbhalle.metfrag.scoring.Scoring;
import de.ipbhalle.metfrag.similarity.Similarity;
import de.ipbhalle.metfrag.similarity.SimilarityGroup;
import de.ipbhalle.metfrag.similarity.TanimotoClusterer;
import de.ipbhalle.metfrag.spectrum.WrapperSpectrum;
import de.ipbhalle.metfrag.tools.Constants;
import de.ipbhalle.metfrag.tools.MolecularFormulaTools;
import de.ipbhalle.metfrag.tools.PPMTool;
import de.ipbhalle.metfrag.web.buildinfo.BuildInfoWeb;
import de.ipbhalle.metfrag.web.model.FeedbackRow;
import de.ipbhalle.metfrag.web.model.MetFragObject;
import de.ipbhalle.metfrag.web.model.ResultPic;
import de.ipbhalle.metfrag.web.model.ResultRow;
import de.ipbhalle.metfrag.web.model.SortableList;




/**
 * 
 * @Named("metFragBean")
 * @RequestScoped
 * 
 */

public class MetFragBean extends SortableList{
	
	private boolean molFormulaRedundancyCheck = true;
	private String database = "kegg";
	private String molFormula = "";
	private String limit = "100";
	private String peakListString;
	private String mode = "1";
	private String charge = "+";
	private String exactMass = "272.06847";
	private int currentRow;
	private ResultRow currentItem;
//	private List<ResultRow> resultRows = new ArrayList<ResultRow>(); 
//	private List<Result> listFragSearch = new ArrayList<Result>();
	
	// dataTableColumn Names
    private static final String scoreCol = "Score";
    private static final String explainedPeaksCol = "# Explained Peaks";
    private static final String databaseIDCol = "Database ID";
	
	private Set<Integer> keys = new HashSet<Integer>();
	private String mzabs = "0.01";
	private String mzppm = "10";
	private String searchPPM = "10";
	// JavaServerFaces related variables
	private UIForm form;
	private UIForm tableForm;
	private UICommand addCommand;
	private int[] count = new int[1];
	private int percentDone = 0;
	private boolean enabled = false;
	private boolean rendered = true;
	private int hitsDatabase = 0;
	private boolean displayResults = false;
	private Vector<String> candidates;
	private String peaks = "119.051 467.616\n" +
	   "123.044 370.662\n" +
	   "147.044 6078.145\n" +
	   "153.019 10000.0\n" +
	   "179.036 141.192\n" +
	   "189.058 176.358\n" +
	   "273.076 10000.000\n" +
	   "274.083 318.003\n";
	private PubChemLocal pubchemLocal;
	private PubChemWebService pubchem;
	private BeilsteinLocal beilstein;
	private static boolean stop = false;
	private String revision = BuildInfo.revisionNumber;
	private String buildDate = BuildInfo.timeStamp;
	private String revisionWeb = BuildInfoWeb.revisionNumber;
	private String buildDateWeb = BuildInfoWeb.timeStamp;
	private String databaseID = "";
	private String svnLog = BuildInfo.svnLog.replace("#BR#", "<br />");
	private String svnLogWeb = BuildInfoWeb.svnLog.replace("#BR#", "<br />");
	private String sep = System.getProperty("file.separator");
	private boolean bioCompound = true;
	private boolean bondEnergyScoring = true;
	private String treeDepth = "2";
	private WrapperSpectrum spectrum = null;
	
	//JDBC connection
	private String user = "";
	private String db = "";
	private String pass = "";
	private String adminuser = "";
	private String adminpass = "";
	private String adminUserInput = "";
	private String adminPassInput = "";
	private String adminError = "";
	private boolean showLoginData = true;
	
	
	//feedback data
	private String email = "";
	private String comment = "";
	private boolean feedbackAllow = false;
	private String error = "";
	//popup data
	private boolean visible3 = false;
	public boolean isVisible3() { return visible3; }
	public void setVisible3(boolean visible3) { this.visible3 = visible3; }
	public void closePopup3() {comment = ""; visible3 = false;}
    public void openPopup3() {visible3 = true;}
    private boolean visible4 = false;
    //feedback admin
	public boolean isVisible4() { return visible4; }
	public void setVisible4(boolean visible4) { this.visible4 = visible4; }
	public void closePopup4() {visible4 = false;}
    public void openPopup4() { getFeedback(); visible4 = true;}
    //peak debug
    private boolean visible5 = false;
    public boolean isVisible5() { return visible5; }
	public void setVisible5(boolean visible5) { this.visible5 = visible5; }
	public void closePopup5() {visible5 = false;}
    public void openPopup5() { analyzePeaks(); visible5 = true;}
    
    //download fragments
    private boolean visible6 = false;
    public boolean isVisible6() { return visible6; }
	public void setVisible6(boolean visible6) { this.visible6 = visible6; }
	public void closePopup6() {visible6 = false;}
    public void openPopup6() { visible6 = true;}
    
    //masses
    private String massAdduct;
    private String massInput;
    
    private List<FeedbackRow> feedbackList = null; 
    private FeedbackRow currentFeedback = null;
	
	protected Thread progressThread;
	protected PersistentFacesState state;
	private List<ResultPic> currentFrags = null;
	
	//get folder names and session ID
    private FacesContext fc = FacesContext.getCurrentInstance();
	private HttpSession session = (HttpSession) fc.getExternalContext().getSession(false);
	private String sessionString = session.getId();
	//URL url = getClass().getResource("/../../.");
	private ServletContext scontext = (ServletContext) fc.getExternalContext().getContext();
	private String webRoot = scontext.getRealPath(sep);	
	
	//Expandable rows setup
    // css style related constants
    public static final String GROUP_INDENT_STYLE_CLASS = "groupRowIndentStyle";
    public static final String GROUP_ROW_STYLE_CLASS = "groupRowStyle";
    public static final String CHILD_INDENT_STYLE_CLASS = "childRowIndentStyle";
    public static final String CHILD_ROW_STYLE_CLASS = "childRowStyle";
    // toggle for expand contract
    public static final String CONTRACT_IMAGE = "tree_nav_top_close_no_siblings.gif";
    public static final String EXPAND_IMAGE = "tree_nav_top_open_no_siblings.gif";
    protected static final String SPACER_IMAGE = "tree_line_blank.gif";
    private List<ResultRowGroupedBean> resultRowGroupedBeans;
    private boolean isInit;
    private String mailServer, mailFrom, mailTo;
    
    private Map<String, ResultRow> candidateToResult = new HashMap<String, ResultRow>();
    
    private String databaseMessage = "";
    
    private boolean isSDFFile = false;
    private boolean sdfSelect = false;
    private String componentStatus;
    private String fileLocation;
    private FileInfo currentFile;
	private int fileProgress;
	private List<IAtomContainer> uploadedSDFCompounds;
	private int threads = 1;
	private ExecutorService threadExecutor = null;
	
	private String parsedPeaksDebug = "";
	
	//workaround for async synchronization
	// Remeber the viewID so we can use it later to restore the view.
	private PersistentFacesState persistentFacesState = null;
	private boolean isIpbAccess = false;
	
	private Resource outputResource = null;
	private String resourceName;
	private Resource outputResourceFrags = null;
	private String resourceNameFrags;
	private Resource outputResourceSDF = null;
	private String resourceNameSDF;

	
	private boolean showSDFLink = false;
	private String log = "";
	
	private String changelog;
	public static Log errorLog;
	   
	
	/**
	 * Instantiates a new metFrag controller.
	 */
	public MetFragBean(){
		super(scoreCol);
		persistentFacesState = PersistentFacesState.getInstance();
		new StyleBean();
		getConfig();
		//set the parameters set in the landing page
		getParameters();
		changelogParser();
	}
	
	
	/**
	 * Landing action.
	 *
	 * @param ae the ae
	 */
	public void landingAction(ActionEvent ae)
	{
		getParameters();
	}
	
	/**
	 * Gets the parameters as set in the landing page.
	 *
	 * @return the parameters
	 */
	private void getParameters()
	{
		FacesContext facesContext = persistentFacesState.getFacesContext();
		Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
		if(sessionMap.containsKey("landingBean"))
		{
			LandingBean landingBean = (LandingBean) sessionMap.get("landingBean");
			landingBean.setForward(true);
			this.peaks = landingBean.getParsedPeaks();
			this.molFormula = landingBean.getMolecularFormula();
			this.exactMass = landingBean.getMass();
			this.databaseID = landingBean.getDatabaseID();
			this.database = landingBean.getDatabase();
			landingBean.setForward(false);
		}
	}
	
	
	/**
	 * External access. This method is called when file MetFragICE.iface is requested
	 */
	public void externalAccess()
	{
		setThreads(1);
		setIpbAccess(false);
	}
	
	/**
	 * Ipb access. This method is called when file MetFragIPB.iface is requested
	 */
	public void ipbAccess()
	{
		setThreads(2);
		setIpbAccess(true);
	}
	
	private void getConfig()
	{
		File propFile = new File(webRoot + "WEB-INF/" + "settings.properties");
	    Properties props = new Properties();
	    try {
			props.load(new FileInputStream(propFile));
			db = props.getProperty("db");
			user = props.getProperty("user");
			pass = props.getProperty("password");
			adminuser = props.getProperty("adminuser");
			adminpass = props.getProperty("adminpass");
			mailServer = props.getProperty("mailserver");
			mailFrom = props.getProperty("mailfrom");
			mailTo = props.getProperty("mailto");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	/**
	 * Changelog parser parses the github rss feed
	 */
	private void changelogParser()
	{
		try {
            URL feedUrl = new URL("https://github.com/s-wolf/MetFragWeb/commits/develop.atom");

            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedUrl));
            
            StringBuilder sb = new StringBuilder();
            sb.append("<ul>");
            
            for (Object obj : feed.getEntries()) {
            	SyndEntryImpl item = (SyndEntryImpl)obj;
            	

            	String DATE_FORMAT = "dd/MM/yy";
            	SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            	
            	sb.append("<li>" + "<em>" + sdf.format(item.getUpdatedDate()) + "</em> " + item.getTitle() + "</li>");
			}
            
            sb.append("</ul>");
            
            this.changelog = sb.toString();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("ERROR: "+ex.getMessage());
        }

	}
	
	
	
	/**
	 * Checks if is admin.
	 * 
	 * @return true, if is admin
	 */
	private boolean isAdmin()
	{
		if(adminUserInput.equals(adminuser) && adminPassInput.equals(adminpass))
		{
			showLoginData = false;
			setAdminError("");
			return true;
		}
		else
		{
			setAdminError("Wrong username and or password!");
			return false;
		}
	}
	
	/**
	 * Logout.
	 */
	public void logout()
	{
		showLoginData = true;
		adminUserInput = "";
		adminPassInput = "";
		setAdminError("");
	}
	
	
	
	public FileInfo getCurrentFile() {
		return currentFile;
	}

    public int getFileProgress() {
        return fileProgress;
    }

    /**
     * <p>This method is bound to the inputFile component and is executed
     * multiple times during the file upload process.  Every call allows
     * the user to finds out what percentage of the file has been uploaded.
     * This progress information can then be used with a progressBar component
     * for user feedback on the file upload progress. </p>
     *
     * @param event holds a InputFile object in its source which can be probed
     *              for the file upload percentage complete.
     */

	public void progressListener(EventObject event) {
        InputFile ifile = (InputFile) event.getSource();
        fileProgress = ifile.getFileInfo().getPercent();
	}
    

    public void uploadActionListener(ActionEvent actionEvent) {
        InputFile inputFile =(InputFile) actionEvent.getSource();
        FileInfo fileInfo = inputFile.getFileInfo();
        //file has been saved
        if (fileInfo.isSaved()) {
            componentStatus = "Custom saved message.";
        }
        //upload failed, generate custom messages
        if (fileInfo.isFailed()) {
            if(fileInfo.getStatus() == FileInfo.INVALID){
                componentStatus = "Custom invalid message.";
            }
            if(fileInfo.getStatus() == FileInfo.SIZE_LIMIT_EXCEEDED){
                componentStatus = "Custom size limit exceeded message.";
            }
            if(fileInfo.getStatus() == FileInfo.INVALID_CONTENT_TYPE){
                componentStatus = "Custom invalid content type message.";
            }
            if(fileInfo.getStatus() == FileInfo.INVALID_NAME_PATTERN){
                componentStatus = "Custom invalid name pattern message - we have set the fileNamePattern attribute to only accept .pdf files.";
            }
        }
    }

    /**
     * Check if the SDF option is selected
     * 
     * @param event the event
     */
    public void checkForLocalDatabase(ValueChangeEvent event){
    	String newValue = (String)event.getNewValue();
    	
    	//delete previous query
    	reset();
    	databaseMessage = "";
    	
    	if(newValue.equals("sdf"))
    	{
    		sdfSelect = true;
    		this.exactMass = "2000";
    	}
    	else
    	{
    		sdfSelect = false;
    		isSDFFile = false;
    	}
    }
    
    /**
     * Set file location and if the file was uploaded
     * 
     * @param event the event
     */
    public void checkFileLocation(ActionEvent event){
        InputFile inputFile =(InputFile) event.getSource();
        FileInfo fileInfo = inputFile.getFileInfo();
        currentFile = fileInfo;
        //file has been saved
        if (fileInfo.isSaved())
        {
        	fileLocation = fileInfo.getPhysicalPath();
			isSDFFile = true;
		}
    }
    

    public String getComponentStatus() {
        return componentStatus;
    }

    public String getFileLocation() {
        return fileLocation;
    }

	
	/**
	 * Start computing
	 * 
	 * @return the string
	 * @throws Exception 
	 */
	public String start() throws Exception {
		
		reset();
		
		setStop(false);
		System.setProperty( "java.awt.headless" , "true" );
		setEnabled(true);
		setRendered(false);
		
		//TODO: read this data in from config file!
		this.pubchemLocal = new PubChemLocal(db, user, pass);
		this.pubchem = new PubChemWebService();
		
		//fix for added space and numbers
		if(this.molFormula != null && !this.molFormula.equals(""))
			this.molFormula = MolecularFormulaManipulator.getString(MolecularFormulaManipulator.getMolecularFormula(this.molFormula.trim(), NoNotificationChemObjectBuilder.getInstance()));
			
		
		List<String> notFound = null;
		
		if(isSDFFile)
		{
			uploadedSDFCompounds = SDFFile.ReadSDFFile(fileLocation);
			Vector<String> tempCandidates = new Vector<String>();
			for (int i = 0; i < uploadedSDFCompounds.size(); i++) {
				tempCandidates.add(i + "");
			}
			candidates = tempCandidates;
		}	
		else if(this.database.equals("kegg") && databaseID.equals(""))
		{
			if(this.molFormula != "")
				candidates = KeggWebservice.KEGGbySumFormula(this.molFormula);
			else
			{
				//peakListString = peaks;
				double exactMass = 0.0;
				if(this.massInput != "")
					exactMass = Double.parseDouble(this.massAdduct) + Double.parseDouble(this.massInput);
				else
					exactMass = Double.parseDouble(this.exactMass);
				
				//double mzabs = Double.parseDouble(this.mzabs);
				//double mzppm = Double.parseDouble(this.mzppm);
				
				System.out.println("Exact mass: " + exactMass +  " Search PPM: " + this.searchPPM);
				candidates = KeggWebservice.KEGGbyMass(exactMass, (PPMTool.getPPMDeviation(exactMass, Double.parseDouble(this.searchPPM))));
			}
		}
		else if(this.database.equals("chemspider") && databaseID.equals(""))
		{
			if(this.molFormula != "")
				candidates = ChemSpider.getChemspiderBySumFormula(this.molFormula);
			else
			{
				//peakListString = peaks;
				double exactMass = 0.0;
				if(this.massInput != "")
					exactMass = Double.parseDouble(this.massAdduct) + Double.parseDouble(this.massInput);
				else
					exactMass = Double.parseDouble(this.exactMass);
				
				//double mzabs = Double.parseDouble(this.mzabs);
				//double mzppm = Double.parseDouble(this.mzppm);
				
				System.out.println("Exact mass: " + exactMass +  " Search PPM: " + this.searchPPM);
				candidates = ChemSpider.getChemspiderByMass(exactMass, (PPMTool.getPPMDeviation(exactMass, Double.parseDouble(this.searchPPM))));
			}
		}
		else if(this.database.equals("pubchem") && databaseID.equals(""))
		{			
			if(this.molFormula != "")
				candidates = pubchem.getHitsbySumFormula(molFormula, false);
			else
			{
				//peakListString = peaks;
				double exactMass = 0.0;
				if(this.massInput != "")
					exactMass = Double.parseDouble(this.massAdduct) + Double.parseDouble(this.massInput);
				else
					exactMass = Double.parseDouble(this.exactMass);
				
				//double mzabs = Double.parseDouble(this.mzabs);
				//double mzppm = Double.parseDouble(this.mzppm);
				
				System.out.println("Exact mass: " + exactMass +  " Search PPM: " + this.searchPPM);
				double lowerBound = exactMass - PPMTool.getPPMDeviation(exactMass, Double.parseDouble(this.searchPPM)); 
				double upperBound = exactMass + PPMTool.getPPMDeviation(exactMass, Double.parseDouble(this.searchPPM));
				candidates = pubchemLocal.getHitsVector(lowerBound, upperBound);
			}
		}
		else if(this.database.equals("beilstein") && databaseID.equals(""))
		{
			this.beilstein = new BeilsteinLocal(db, user, pass);
			//peakListString = peaks;
			double exactMass = 0.0;
			if(this.massInput != "")
				exactMass = Double.parseDouble(this.massAdduct) + Double.parseDouble(this.massInput);
			else
				exactMass = Double.parseDouble(this.exactMass);
			
			//double mzabs = Double.parseDouble(this.mzabs);
			//double mzppm = Double.parseDouble(this.mzppm);
			
			System.out.println("Exact mass: " + exactMass +  " Search PPM: " + this.searchPPM);
			double lowerBound = exactMass - PPMTool.getPPMDeviation(exactMass, Double.parseDouble(this.searchPPM)); 
			double upperBound = exactMass + PPMTool.getPPMDeviation(exactMass, Double.parseDouble(this.searchPPM));

			candidates = beilstein.getHitsVector(lowerBound, upperBound);
		}
		else if (!databaseID.equals(""))
		{
			candidates = new Vector<String>();
			String[] idList = databaseID.split(",");
			for (int i = 0; i < idList.length; i++) {
				if(existingDBEntries(idList[i].trim()))
					candidates.add(idList[i].trim());
				else
					databaseMessage += "Error retrieving: " + idList[i].trim() + "\n";
			}
		}
		
		//no hits
		if(candidates == null || candidates.size() == 0)
		{
			databaseMessage += "No hits!";
			candidates = new Vector<String>();
			hitsDatabase = 0;
		}
		else
		{	
			if(Integer.parseInt(limit) < candidates.size())
			{
				hitsDatabase = Integer.parseInt(limit);
				databaseMessage += candidates.size() + " hits!";
			}
			else
			{
				hitsDatabase = candidates.size();
				databaseMessage += candidates.size() + " hits!";
			}

		}
		
		JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "document.getElementById(\"searchUpstream\").firstChild.disabled = false;");

		return "";
	}
	
	
	/**
	 * Gets the non existing db entries.
	 * 
	 * @param db the db
	 * @param idList the id list
	 * 
	 * @return the non existing db entries
	 */
	private boolean existingDBEntries(String id)
	{
		IAtomContainer test = null;
		try
		{
			if(this.database.equals("kegg"))
			{
				test = KeggWebservice.getMol(id, "/vol/mirrors/kegg/mol/", true);
			}
			else if(this.database.equals("chemspider"))
			{
				test = ChemSpider.getMol(id, true);
			}
			else if(this.database.equals("pubchem"))
			{
				test = pubchemLocal.getMol(id, true);
			}
			else if(this.database.equals("beilstein"))
			{
				test = beilstein.getMol(id, true);
			}
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.err.println("Error retrieving compound.");
		}
		
		if(test == null)
			return false;
		else
			return true;

	}
	
	public String startMetFrag()
	{	
		try {
			metFragParallel();
		} catch (Exception e) {
			e.printStackTrace();
		}	
		this.rendered = false;
		return "";
	}
	
	
	/**
	 * Reset the objects and delete the pictures from the webserver.
	 * 
	 * @return the string
	 */
	public String reset()
	{
		
		databaseMessage = "";
		setStop(true);
		percentDone = 0;
		count[0] = 0;
		this.enabled = false;
		this.rendered = true;
		this.hitsDatabase = 0;
		this.candidateToResult = new HashMap<String, ResultRow>();
		this.candidates = new Vector<String>();
		//tableForm.setRendered(false);
		setDisplayResults(false);
		//reset old sort to none....so the sorting is working more than once
		oldSort = "";
		this.log = "";
		errorLog = new Log();
		
		HttpSession session = (HttpSession) fc.getExternalContext().getSession(false);
		String sessionString = session.getId();
		
		//get folder names
		String currentFolder = webRoot + sep + "FragmentPics" + sep + sessionString + sep;
		//now delete the previous generated pics
		File dirToDelete = new File(currentFolder);
		boolean isDeleted = deleteDir(dirToDelete);
		System.out.println("Pictures with with Session: " + sessionString + " were deleted? " + isDeleted);
		
		return "";
	}
	
	
	/**
	 * Stop the processing.
	 * 
	 * @return the string
	 */
	public String stop()
	{
		setStop(true);	
		this.threadExecutor.shutdownNow();
		this.enabled = false;
		return "";
	}
	
	
	/**
	 * Write feedback data into the table.
	 * 
	 * @return the string
	 */
	public String feedback()
	{
		Connection con = null; 
		error = "";
		
		
		if(feedbackAllow)
		{
		
		    try {
		    	String driver = "com.mysql.jdbc.Driver"; 
				Class.forName(driver); 
				DriverManager.registerDriver (new com.mysql.jdbc.Driver()); 
		        // JDBC-driver
		        Class.forName(driver);
		        con = DriverManager.getConnection(db, user, pass);
			    
	
	            String sql = "INSERT into Feedback (Peaklist, ExactMass, SearchPPM, MolecularFormula, DatabaseUsed, BiologicalCompound, LimitHits, " +
	            		"DatabaseIDs, Mode, MzAbs, MzPPM, Email, Comment, Date, Charge) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?, now(),?)";
	            PreparedStatement pst = null;
	            pst = con.prepareStatement(sql);
	
	            pst.setString(1, peaks.replace("\n", "%BR%"));
	            
	            String tempExactMass = "";
	            if(exactMass == "")
	            	tempExactMass = "-1.0";
	            else
	            	tempExactMass = exactMass;
	            
	            pst.setDouble(2, Double.parseDouble(tempExactMass));
	            int searchPPM = 0;
	            if(this.searchPPM == null || this.searchPPM.equals(""))
	            	searchPPM = 0;
	            else
	            	searchPPM = Integer.parseInt(this.searchPPM);
	            
	            pst.setInt(3, searchPPM);
	            pst.setString(4, molFormula);
	            pst.setString(5, database);
	            
	            int bioTemp = 0;
	            if(bioCompound)
	            	bioTemp = 1;
	            
	            pst.setInt(6, bioTemp);
	            pst.setInt(7, Integer.parseInt(limit));
	            pst.setString(8, databaseID);
	            
	            int modeTemp = 1;
	            if(mode.equals("1"))
	            	modeTemp = 1;
	            else if(mode.equals("-1"))
	            	modeTemp = -1;
	            else if(mode.equals("0"))
	            	modeTemp = 0;
	            
	            pst.setInt(9, modeTemp);
	            pst.setDouble(10, Double.parseDouble(mzabs));
	            pst.setInt(11, Integer.parseInt(mzppm));
	            pst.setString(12, email);
	            pst.setString(13, comment);
	            pst.setString(14, charge);
	
	            pst.executeUpdate();
	            pst.close();
	
	        } catch (Exception e) {
	            System.out.println(e);
	            e.printStackTrace();
	        }
	        finally{
	            try {

                    MailClient client = new MailClient();
                    String server= "mail.ipb-halle.de";
                    String from= "swolf@ipb-halle.de";
                    String to = "swolf@ipb-halle.de";
                    String subject= "MetFrag feedback! " + email;
                    String message= comment;
                    client.sendMailWithoutAttach(server,from,to,subject,message);
                    //send also a copy to sender
                    client.sendMailWithoutAttach(server,from,email,subject,"Your message:\n\n" + message);

					con.close();
					closePopup3();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch(Exception e)
                {
                    e.printStackTrace(System.out);
                }
	        }
		}
        else
        {
        	error = "Please click the checkbox so we can investigate the problem for you!";
        }
	    
		return "";
	}
	
	
	/**
	 * Gets the feedback.
	 * 
	 * @return the feedback
	 */
	public void getFeedback()
	{
		Connection con = null; 
		error = "";
		this.feedbackList = new ArrayList<FeedbackRow>();
		
		if(isAdmin())
		{
		    try {
		    	String driver = "com.mysql.jdbc.Driver"; 
				Class.forName(driver); 
				DriverManager.registerDriver (new com.mysql.jdbc.Driver()); 
		        // JDBC-driver
		        Class.forName(driver);
		        con = DriverManager.getConnection(db, user, pass);
			    
	
	            String sql = "SELECT ID, Peaklist, ExactMass, SearchPPM, MolecularFormula, DatabaseUsed, BiologicalCompound, LimitHits, " +
	            		"DatabaseIDs, Mode, MzAbs, MzPPM, Email, Comment, Date, Fixed, Answered, Charge FROM Feedback ORDER BY ID desc;";
	            Statement stmt = con.createStatement();
	            ResultSet rs = stmt.executeQuery(sql);
	            while(rs.next())
	            {	            
	            	String knobRed = "./images/knob_red.png";
	            	String knobGreen = "./images/knob_green.png";
	            	
	            	String fixed = knobRed;
	            	if(rs.getBoolean("Fixed"))
	            		fixed = knobGreen;
	            	
	            	String answered = knobRed;
	            	if(rs.getBoolean("Answered"))
	            		answered = knobGreen;
	            		
	            	
	            	feedbackList.add(new FeedbackRow(rs.getInt("ID"), rs.getString("Peaklist").replace("%BR%", "\n"), rs.getDouble("ExactMass"), rs.getInt("SearchPPM"), 
	            			rs.getString("MolecularFormula"), rs.getString("databaseUsed"), rs.getBoolean("BiologicalCompound"), rs.getInt("LimitHits"), 
	            			rs.getString("DatabaseIDs"), rs.getInt("Mode"), rs.getDouble("MzAbs"), rs.getInt("MzPPM"), rs.getString("Email"), rs.getString("Comment"),
	            			fixed, answered, rs.getDate("Date"), rs.getString("Charge")));
	            }
	
	
	        } catch (Exception e) {
	            System.out.println(e);
	            e.printStackTrace();
	        }
	        finally{
	            try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
		}
	}
	
	
	
    /**
	 * Deletes all files and subdirectories under dir.
     * Returns true if all deletions were successful.
     * If a deletion fails, the method stops attempting to delete and returns false.
	 * 
	 * @param dir the dir
	 * 
	 * @return true, if successful
	 */
	private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    }
	
	
	 /**
     * Cleans up the resources used by this class.  This method could be called
     * when a session destroyed event is called.
     */
    public void dispose() {
        isInit = false;
        // clean up the array list
        if (resultRowGroupedBeans != null) {
        	ResultRowGroupedBean tmp;
            List<ResultRowGroupedBean> tmpList;
            for (int i = 0; i < resultRowGroupedBeans.size(); i++) {
                tmp = (ResultRowGroupedBean) resultRowGroupedBeans.get(i);
                tmpList = tmp.getChildResultRows();
                if (tmpList != null) {
                    tmpList.clear();
                }
            }
            resultRowGroupedBeans.clear();
        }
    }

    /**
     * Gets the list of FilesGroupRecordBean which will be used by the
     * ice:dataTable component.
     *
     * @return array list of parent FilesGroupRecordBeans
     */
    public List getFilesGroupRecordBeans() {
        return resultRowGroupedBeans;
    }
	
	
    /**
     * Parses candidate string
     * 
     * @param database the database
     * @param candidateString the candidate string
     * 
     * @return the candidate id
     */
    private String getCandidateID(String database, String candidateString)
    {
    	String candidate = "";
    	try
		{
			if(database.equals("kegg"))
			{
				if(candidateString.split(":").length > 1)
					candidate = candidateString.split(":")[1];
				else
					candidate = candidateString;
			}
			else
			{
				candidate = candidateString;
			}
		}
		//some error in the molecule!?
		catch(ArrayIndexOutOfBoundsException e)
		{
			System.out.println("Some error reading the molecule: " + candidate + " (" + database + ")");
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
		
		return candidate;
    }
    
    
    /**
     * Gets the molecule from the selected database.
     * 
     * @param database the database
     * @param candidate the candidate
     * 
     * @return the molecule
     */
    private IAtomContainer getMolecule(String database, String candidate)
    {
    	IAtomContainer molecule = null;
    	try
		{
    		if(isSDFFile)
    		{
    			molecule = uploadedSDFCompounds.get(Integer.parseInt(candidate));
    		}
    		else if(database.equals("kegg") && databaseID.equals(""))
			{
				molecule = KeggWebservice.getMol(candidate, "/vol/data/pathways/kegg/mol/", !isBioCompound());
				//fix for kegg returning also formulas with a substring of the input formula
				if(this.molFormula != "" && molecule != null)
				{
					molecule = AtomContainerManipulator.removeHydrogens(molecule);
					CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(molecule.getBuilder());
			        for (IAtom atom : molecule.atoms()) {
			          IAtomType type = matcher.findMatchingAtomType(molecule, atom);
			          AtomTypeManipulator.configure(atom, type);
			        }
			        CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(molecule.getBuilder());
			        hAdder.addImplicitHydrogens(molecule);
			        AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);
			        
					IMolecularFormula formula = MolecularFormulaManipulator.getMolecularFormula(molecule);
					String formulaString = MolecularFormulaManipulator.getString(formula);
					if(!this.molFormula.equals(formulaString))
					{
						this.log += "KEGG compound: " + candidate + " (" + formulaString + ") does not match given formula!<br />"; 
						return null;
					}
				}
			}
			else if(database.equals("kegg") && !databaseID.equals(""))
			{
				molecule = KeggWebservice.getMol(candidate, "/vol/data/pathways/kegg/mol/", true);
			}
			else if(database.equals("chemspider") && databaseID.equals(""))
			{
				molecule = ChemSpider.getMol(candidate, !isBioCompound());
			}
			else if(database.equals("chemspider") && !databaseID.equals(""))
			{
				molecule = ChemSpider.getMol(candidate, true);
			}
			else if(database.equals("pubchem") && databaseID.equals(""))
			{
				try
				{
					molecule = pubchemLocal.getMol(candidate, !isBioCompound());
				}
				catch(InvalidSmilesException e)
				{
					System.err.println("Skipped invalid smiles!");
				}
			}
			else if(database.equals("pubchem") && !databaseID.equals(""))
			{
				molecule = pubchemLocal.getMol(candidate, true);
			}
			else if(database.equals("beilstein") && databaseID.equals(""))
			{
		        molecule = beilstein.getMol(candidate, !isBioCompound());
			}
			else if(database.equals("beilstein") && !databaseID.equals(""))
			{
		        molecule = beilstein.getMol(candidate, true);
			}
			else
			{
				System.err.println("ERROR: no database selected!!!!");
			}
		}
		//some error in the molecule!?
		catch(ArrayIndexOutOfBoundsException e)
		{
			System.out.println("Some error reading the molecule: " + candidate + " (" + database + ")");
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
		} catch (InvalidSmilesException e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
		} catch (RemoteException e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
		} catch (CDKException e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
		
		return molecule;
    }
    
    
    public void analyzePeaks()
    {
    	double exactMassThread = 1000.0;
		if(exactMass != null && exactMass != "")
			exactMassThread = Double.parseDouble(exactMass);
		else if(molFormula != "")
		{
			IMolecularFormula formula = new MolecularFormula();
			formula = MolecularFormulaManipulator.getMolecularFormula(molFormula.trim(), formula);
			exactMassThread = MolecularFormulaTools.getMonoisotopicMass(formula);
		}
		
		//get charge
		boolean isPositive = true;
		if(charge.contains("-"))
			isPositive = false;
			
		
    	WrapperSpectrum spectrum = new WrapperSpectrum(peaks, Integer.parseInt(mode), exactMassThread, isPositive);
    	Vector<Peak> peakListParsed = spectrum.getPeakList();
    	parsedPeaksDebug = "<table border='0' cellspacing='4' cellpadding='6'>" +
    			"<tr>" +
    				"<td>Peak#</td><td>m/z</td><td>abs. int.</td><td>rel. int.</td>" +
    			"</tr>";
    	int count = 1;
    	
    	String peakString = "";
    	String peakIntString = "";
    	int temp = 0;
    	
    	for (Peak peak : peakListParsed) {
    		if(temp == (peakListParsed.size() - 1))
    		{
    			peakString += peak.getMass();
        		peakIntString += peak.getRelIntensity();
    		}
    		else
    		{
    			peakString += peak.getMass() + ",";
        		peakIntString += peak.getRelIntensity() + ",";
    		}
    		parsedPeaksDebug += "<tr><td>" + count + "</td><td>" + peak.getMass() + "</td><td>" + peak.getIntensity() + "</td><td>" + peak.getRelIntensity() + "</td><td></tr>";
			count++;
		}
    	parsedPeaksDebug += "</table>";
    	
    	JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "drawSpectrum(800, 200, 'spectrumAnalyze', [" + peakString + "],[" + peakIntString + "],[],[],[],[]);");

    }
    
    
    /**
	 * MetFrag!!!! :)
	 * 
	 * @throws Exception the exception
	 */
	public void metFragParallel()
	{
		this.enabled = true;
//		final String viewID = persistentFacesState.getFacesContext().getViewRoot().getViewId();
		
		// Create the progress thread
        progressThread = new Thread(new Runnable() {
        	public void run() {
        		
        		// Begin of the workaround
        		state = PersistentFacesState.getInstance(); 
                // End workaround

        		try
        		{
        			
        			//now fill executor!!!
    			    
    			    //thread executor
    			    System.out.println("Used Threads: " + threads);
    			    threadExecutor = Executors.newFixedThreadPool(threads);

    			    
        			
					double exactMassThread = 1000.0;
					if(exactMass != "")
						exactMassThread = Double.parseDouble(exactMass);
					else if(molFormula != "")
					{
						IMolecularFormula formula = new MolecularFormula();
						formula = MolecularFormulaManipulator.getMolecularFormula(molFormula, formula);
						exactMassThread = MolecularFormulaTools.getMonoisotopicMass(formula);
					}
					double mzabsThread = Double.parseDouble(mzabs);
					double mzppmThread = Double.parseDouble(mzppm);
					
					//get charge
					boolean isPositive = true;
					if(charge.contains("-"))
						isPositive = false;
					
					spectrum = new WrapperSpectrum(peaks, Integer.parseInt(mode), exactMassThread, isPositive);
					
					if(Integer.parseInt(limit) < candidates.size())
					{
						log += "Limiting the hits (" + candidates.size() + ") to the given maximum number of " + limit + "<br />";
						hitsDatabase = Integer.parseInt(limit);
					}
					else
						hitsDatabase = candidates.size();
						
					percentDone = 0;
					count[0] = 0;
					Map<Double, Vector<String>> realScoreMap = new HashMap<Double, Vector<String>>();
					Map<String, IAtomContainer> candidateToStructure = new HashMap<String, IAtomContainer>();
					
					int tempCount = 0;
					
					for (int c = 0; c < candidates.size(); c++) {
						
				        //get mol file from kegg....remove "cpd:"
						String candidateID = getCandidateID(database, candidates.get(c));
						IAtomContainer molecule = getMolecule(database, candidateID);
						
						
						//there was an error retrieving this molecule or it was no biological compound
						if(molecule == null)
						{
							log += "Compound: " + candidateID +  " is no biological compound or could not be retrieved!<br />"; 
							continue;
						}
						
						
						//skip if molecule is not connected
						boolean isConnected = ConnectivityChecker.isConnected(molecule);
						if(!isConnected)
						{
							log += "Compound: " + candidateID +  " is not connected!<br />"; 
							continue;
						}
						
						tempCount++;
						
						//fill map with structures
				        candidateToStructure.put(candidateID, molecule);
						
						MetFragObject metFragData = new MetFragObject();
						metFragData.setBondEnergyScoring(bondEnergyScoring);
						metFragData.setCandidate(candidateID);
						metFragData.setDatabase(database);
						metFragData.setDatabaseID(databaseID);
						metFragData.setMode(Integer.parseInt(mode));
						metFragData.setMolecule(molecule);
						metFragData.setMolFormulaRedundancyCheck(molFormulaRedundancyCheck);
						metFragData.setMzabs(mzabsThread);
						metFragData.setMzppm(mzppmThread);
						metFragData.setSpectrum(spectrum);
						metFragData.setStop(stop);
						metFragData.setTreeDepth(getTreeDepth());
						
						if(threadExecutor.isShutdown() || threadExecutor.isTerminated())
							break;
							
						
						if(stop)
							break;
							
						try
						{
							threadExecutor.execute(new ParallelFragmentation(metFragData, pubchemLocal, beilstein, candidateToResult, realScoreMap, sessionString, webRoot, count));
						}
						catch (NullPointerException e) {
							System.err.println("ERROR" + e.getMessage());
						}
		
						float test = (count[0] / (float)candidates.size()) * (float)100;
						percentDone = Math.round(test);
					
						state.executeAndRender();
						
						
						if(tempCount == Integer.parseInt(limit) && isSDFFile == false)
						{
							break;
						}
					}
					
					
					
					
					threadExecutor.shutdown();
					//wait until all threads are finished
					while(!threadExecutor.isTerminated())
					{						
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}//sleep for 1000 ms
						
						float test = (count[0] / (float)candidates.size()) * (float)100;
						percentDone = Math.round(test);
						state.executeAndRender();
					}
					
			        log += "<br />" + errorLog.getLog();
					
					// initiate the list
			        if (resultRowGroupedBeans != null) {
			            resultRowGroupedBeans.clear();
			        } else {
			            resultRowGroupedBeans = new ArrayList(10);
			        }

//			        StyleBean styleBean = (StyleBean)state.getApplication().evaluateExpressionGet(facesContext, "#{styleBean}", StyleBean.class);
//			        Application application = PersistentFacesState.getApplication();
			        
			        FacesContext facesContext = persistentFacesState.getFacesContext();
			        Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
			        StyleBean styleBean = (StyleBean) sessionMap.get("styleBean");

					if(!bondEnergyScoring)
						normalize();
					else
						normalizeWithBondEnergy(realScoreMap);
			        		
					Similarity sim = new Similarity(candidateToStructure, true, false);
			        for (Double score : realScoreMap.keySet()) {
						List<String> candidates = realScoreMap.get(score);
						
						
						TanimotoClusterer tanimoto = new TanimotoClusterer(sim.getSimilarityMatrix(), sim.getCandidateToPosition());
						List<SimilarityGroup> clusteredCpds = tanimoto.clusterCandididates(candidates, 0.95f);
						List<SimilarityGroup> clusteredCpdsCleaned = tanimoto.getCleanedClusters(clusteredCpds);

						for (SimilarityGroup similarityGroup : clusteredCpdsCleaned) {
							//cluster
							if(similarityGroup.getSimilarCompounds().size() > 1)
							{
								ResultRowGroupedBean filesRecordGroup = new ResultRowGroupedBean(GROUP_INDENT_STYLE_CLASS,
				                        GROUP_ROW_STYLE_CLASS,
				                        styleBean,
				                        EXPAND_IMAGE, CONTRACT_IMAGE,
				                        resultRowGroupedBeans, false);
								String baseCand = similarityGroup.getCandidateTocompare();
								addToResultsList(baseCand, filesRecordGroup);
								System.out.print("Group of " + similarityGroup.getSimilarCompounds().size() + " " + similarityGroup.getCandidateTocompare() +  ": ");
								
								for (int i = 0; i < similarityGroup.getSimilarCompounds().size(); i++) {
									if(similarityGroup.getSimilarCompounds().get(i).getCompoundID().equals(baseCand))
										continue;									
									ResultRowGroupedBean childFilesGroup = new ResultRowGroupedBean(CHILD_INDENT_STYLE_CLASS, CHILD_ROW_STYLE_CLASS);
									addToResultsList(similarityGroup.getSimilarCompounds().get(i).getCompoundID(), childFilesGroup);
							        filesRecordGroup.addChildFilesGroupRecord(childFilesGroup);
								}
							}
							//single
							else
							{
								ResultRowGroupedBean filesRecordGroup = new ResultRowGroupedBean("",
										"",
				                        styleBean,
				                        SPACER_IMAGE, SPACER_IMAGE,
				                        resultRowGroupedBeans, false);
								String candidate = similarityGroup.getSimilarCompounds().get(0).getCompoundID();
								addToResultsList(candidate, filesRecordGroup);		
								
							}
						}
					}

					resultRowGroupedBeans = getResults();
					
					setDisplayResults(true);

					//dont show progress bar and stop button anymore
					enabled = false;
					state.executeAndRender();					
        		}
        		catch(Exception e)
        		{
        			System.err.println("Error in MetFrag!" +  e.getMessage());
        			e.printStackTrace();
        		}
        		
        	}
        });
        
        progressThread.start();
        
	}
    

	/**
	 * Sort backwards.
	 * 
	 * @param original the original
	 * 
	 * @return the vector< peak mol pair>
	 */
	private Vector<PeakMolPair> sortBackwards(Vector<PeakMolPair> original)
	{
		Vector<PeakMolPair> ret = new Vector<PeakMolPair>();
		for (int i = original.size() - 1; i >= 0 ; i--) {
			ret.add(original.get(i));
		}
		return ret;
	}
	
	
	/**
	 * Get data out of map into the grouped records.
	 * 
	 * @param resultRow the result row
	 */
	private void addToResultsList(String candidate, ResultRowGroupedBean resultRowGroup)
	{
		ResultRow resultRow = candidateToResult.get(candidate);
		resultRowGroup.setBondEnergy(resultRow.getBondEnergy());
		resultRowGroup.setDatabaseLink(resultRow.getDatabaseLink());
		resultRowGroup.setExplainedPeaks(resultRow.getExplainedPeaks());
		resultRowGroup.setFrags(resultRow.getFrags());
		resultRowGroup.setHydrogenPenalty(resultRow.getHydrogenPenalty());
		resultRowGroup.setID(candidate);
		resultRowGroup.setImage(resultRow.getImage());
		resultRowGroup.setMass(resultRow.getMass());
		resultRowGroup.setMolName(resultRow.getMolName());
		resultRowGroup.setPeaksFound(resultRow.getPeaksFound());
		resultRowGroup.setPeaksFoundInt(resultRow.getPeaksFoundInt());
		resultRowGroup.setPeaksNotFound(resultRow.getPeaksNotFound());
		resultRowGroup.setPeaksNotFoundInt(resultRow.getPeaksNotFoundInt());
		resultRowGroup.setPeaksNotUsed(resultRow.getPeaksNotUsed());
		resultRowGroup.setPeaksNotUsedInt(resultRow.getPeaksNotUsedInt());
		resultRowGroup.setScore(resultRow.getScore());
		resultRowGroup.setMolecularFormula(resultRow.getMolecularFormula());
		resultRowGroup.setSmiles(resultRow.getSmiles());
		resultRowGroup.setMoleculeDescriptors(resultRow.getMoleculeDescriptors());
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
	
	/**
     * Gets the inventoryItem array of car data.
     * @return array of car inventory data.
     */
    public List<ResultRowGroupedBean> getResults() {
		
    	//results = new ResultRow[resultRows.size()];
		//results = resultRows.toArray(results);
		
        // we only want to sortColumnName if the column or ordering has changed.
        if (!oldSort.equals(sortColumnName) || oldAscending != ascending){
             sort();
             oldSort = sortColumnName;
             oldAscending = ascending;
        }
        
        //generate xls output
        generateXLSResource();
        
        return resultRowGroupedBeans;
    }
	
    
	protected void sort() {
		
		//results = new ResultRow[resultRows.size()];
		//results = resultRows.toArray(results);
		
        if(resultRowGroupedBeans != null)
        	Collections.sort(resultRowGroupedBeans, new Comparator(){
        		public int compare(Object o1, Object o2) {
                    ResultRow c1 = (ResultRow) o1;
                    ResultRow c2 = (ResultRow) o2;
                    if (sortColumnName == null) {
                        return 0;
                    }
                    if (sortColumnName.equals(scoreCol)) {
                        return ascending ?
                        Double.valueOf(c1.getScore()).compareTo(new Double(c2.getScore())) :
                        Double.valueOf(c2.getScore()).compareTo(new Double(c1.getScore()));
                    } else if (sortColumnName.equals(explainedPeaksCol)) {
                        return ascending ? Integer.valueOf(c1.getExplainedPeaks()).compareTo(new Integer(c2.getExplainedPeaks())) :
                        Integer.valueOf(c2.getExplainedPeaks()).compareTo(new Integer(c1.getExplainedPeaks()));
                    } else if (sortColumnName.equals(databaseIDCol)) {
                        return ascending ? c1.getID().compareTo(c2.getID()) :
                        c2.getID().compareTo(c1.getID());
                    }  else return 0;
                }
			});
    }
	
	
	/**
	 * Normalize the score between 0 and 1.
	 */
	private void normalize()
	{
		double maxScore = 0; 
		for (String cand : candidateToResult.keySet()) {
			if(candidateToResult.get(cand).getScore() > maxScore)
			{
				maxScore = candidateToResult.get(cand).getScore();
			}
		}
		
		for (String cand : candidateToResult.keySet()){
			candidateToResult.get(cand).setScore(Math.round((candidateToResult.get(cand).getScore() / maxScore) * 1000) / 1000.0);
		}
	}	
	
	
	/**
	 * Normalize the score between 0 and 1 with bond energies.
	 */
	private void normalizeWithBondEnergy(Map<Double, Vector<String>> realScoreMap)
	{
		double maxScore = 0; 
		Map<String, Double> candidateToEnergy = new HashMap<String, Double>();
		Map<String, Double> candidateToHydrogenPenalty = new HashMap<String, Double>();

		for (String cand : candidateToResult.keySet()){
			if(candidateToResult.get(cand).getScore() > maxScore)
			{
				maxScore = candidateToResult.get(cand).getScore();
			}
			candidateToEnergy.put(candidateToResult.get(cand).getID(), candidateToResult.get(cand).getBondEnergy());
			candidateToHydrogenPenalty.put(candidateToResult.get(cand).getID(), candidateToResult.get(cand).getHydrogenPenalty());
		}
		
		
		Map<Double, Vector<String>> normalizedScore = Scoring.getCombinedScore(realScoreMap, candidateToEnergy, candidateToHydrogenPenalty);
		Map<String, Double> candidateToCombinedScore = new HashMap<String, Double>();
		
		//hack to write back the scores
		Double maxNormalizedScore = 0.0;
		for (Double key : normalizedScore.keySet()) {
			for (String databaseID : normalizedScore.get(key)) {
				candidateToCombinedScore.put(databaseID, key);
				
				if(key > maxNormalizedScore)
					maxNormalizedScore = key;
			}
		}

		//now set the score
		for (String cand : candidateToResult.keySet()){
			Double score = Math.round((candidateToCombinedScore.get(candidateToResult.get(cand).getID()) / maxNormalizedScore ) * 1000.0) / 1000.0;
			candidateToResult.get(cand).setScore(score);
		}
	}	
	
	
	/**
	 * Listen row action. Gets the clicked row
	 * 
	 * @param event the event
	 */
	public void listenRowAction(ActionEvent event) {
		//gets the current row from the data table
		ResultRow currentRow = (ResultRow) event.getComponent().getAttributes().get("currentRow");
		currentItem = currentRow;
		//fragment pics
		currentFrags = currentRow.getFrags();
		//render the js on client side
		JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "drawSpectrum(650, 200, 'spectrum_small', [" + currentRow.getPeaksFound() + "],[" + currentRow.getPeaksFoundInt() + "], [" + currentRow.getPeaksNotFound() + "], [" + currentRow.getPeaksNotFoundInt() + "], [" + currentRow.getPeaksNotUsed() + "], [" + currentRow.getPeaksNotUsedInt() + "]);");

	}
	
	
	/**
	 * Listen row feedback action. Sets the current feedback data as input.
	 * 
	 * @param event the event
	 */
	public void listenRowFeedbackAction(ActionEvent event) {
		//gets the current row from the data table
		currentFeedback = (FeedbackRow) event.getComponent().getAttributes().get("currentFeedbackRow");
		
		//now set the the data accordingly
		bioCompound = currentFeedback.getBiologicalCompound();
		databaseID = currentFeedback.getDatabaseIDs();
		database = currentFeedback.getDatabaseUsed();
		
		if(currentFeedback.getExactMass() == 0.0)
			exactMass = "";
		else
			exactMass = currentFeedback.getExactMass().toString();
		
		limit = currentFeedback.getLimit().toString();
		mode = currentFeedback.getMode().toString();
		molFormula = currentFeedback.getMolecularFormula();
		mzabs = currentFeedback.getMzAbs().toString();
		mzppm = currentFeedback.getMzPPM().toString();
		peaks = currentFeedback.getPeaklist();
		searchPPM = currentFeedback.getSearchPPM().toString();
		charge = currentFeedback.getCharge();		
		if(charge == null || charge.equals(""))
			charge = "+";
	}
	
	/**
	 * Set the fixed attribute of this row to true 
	 * 
	 * @param event the event
	 */
	public void listenFixedFeedbackAction(ActionEvent event) {
		//gets the current row from the data table
		currentFeedback = (FeedbackRow) event.getComponent().getAttributes().get("currentFeedbackRow");
		
		//now set the the data accordingly
		Integer id = currentFeedback.getId();
		Connection con = null;
		
		try {
	    	String driver = "com.mysql.jdbc.Driver"; 
			Class.forName(driver); 
			DriverManager.registerDriver (new com.mysql.jdbc.Driver()); 
	        // JDBC-driver
	        Class.forName(driver);
	        con = DriverManager.getConnection(db, user, pass);
		    

            String sql = "Update Feedback set Fixed = 1 where ID = ?";
            PreparedStatement pst = null;
            pst = con.prepareStatement(sql);
            pst.setInt(1, id);
            pst.executeUpdate();
            pst.close();

        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
        finally{
            try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        getFeedback();
		
	}
	
	/**
	 * Set the fixed attribute of this row to true 
	 * 
	 * @param event the event
	 */
	public void listenAnsweredFeedbackAction(ActionEvent event) {
		//gets the current row from the data table
		currentFeedback = (FeedbackRow) event.getComponent().getAttributes().get("currentFeedbackRow");
		
		//now set the the data accordingly
		Integer id = currentFeedback.getId();
		Connection con = null;
		
		try {
	    	String driver = "com.mysql.jdbc.Driver"; 
			Class.forName(driver); 
			DriverManager.registerDriver (new com.mysql.jdbc.Driver()); 
	        // JDBC-driver
	        Class.forName(driver);
	        con = DriverManager.getConnection(db, user, pass);
		    

            String sql = "Update Feedback set Answered = 1 where ID = ?";
            PreparedStatement pst = null;
            pst = con.prepareStatement(sql);
            pst.setInt(1, id);
            pst.executeUpdate();
            pst.close();

        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
        finally{
            try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        getFeedback();
		
	}
	
	
	
	/**  
	 * Write out the complete results table
	 *  based on method from Michael Gerlich 
	 *  */
	private void generateXLSResource() {
		
		ExternalContext ec = fc.getExternalContext();
		HttpSession session = (HttpSession) ec.getSession(false);
		String sessionString = session.getId();	
		long time = new Date().getTime();
		
		String currentFolder = webRoot + "FragmentPics" + sep + sessionString + sep;
		String relPath = "./FragmentPics" + sep + sessionString + sep;
		new File(currentFolder).mkdirs();
		
		File dir = new File(currentFolder);
		if(!dir.exists())
			dir.mkdirs();
		
		// skip creation of output resource if file access is denied
		if(!dir.canWrite())
			return;
		resourceName = "MetFragResults_" + time +  ".xls";
		File f = new File(dir, resourceName);
		try {
			f.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// create new Excel file
		WritableSheet sheet = null;
		WritableWorkbook workbook = null;
		WorkbookSettings settings = new WorkbookSettings();
//		settings.setLocale(fc.getViewRoot().getLocale());
		try {
			workbook = Workbook.createWorkbook(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// set sheet name (output port) and position
		sheet = workbook.createSheet("MetFrag", 0);
		WritableFont arial10font = null;
		WritableCellFormat arial10format = null;
		// set header for sheet, name it after output port name 
		try {
			arial10font = new WritableFont(WritableFont.ARIAL, 10);
			arial10format = new WritableCellFormat(arial10font);
			arial10font.setBoldStyle(WritableFont.BOLD);
			Label label = new Label(0, 0, "MetFrag Results Table", arial10format);
			sheet.addCell(label);
		} catch (WriteException we) {
			we.printStackTrace();
		}
		
		
		// for each workflow output port, create new sheet inside Excel file and store results
		int i = 1;
		
		//also add seetings to excel sheet
		WritableCell db = new Label(0,0, "Database: " + database, arial10format);
		WritableCell em = new Label(0,1, "Exact mass: " + exactMass + " (search ppm: " + searchPPM + ")", arial10format);
		WritableCell mf = new Label(0,2, "Formula: " + molFormula, arial10format);
		WritableCell bc = new Label(1,0, "Biological compounds: " + bioCompound, arial10format);
		WritableCell ls = new Label(1,1, "Limit # of structures: " + limit, arial10format);
		WritableCell di = new Label(1,2, "Database ID's: " + databaseID, arial10format);
		
		String modeTemp = "[M+H]" + charge;
		if(mode.equals("-1"))
			modeTemp = "[M-H]" + charge;
		else if(mode.equals("0"))
			modeTemp = "[M]" + charge;
		
		
		WritableCell mo = new Label(2,0, "Mode : " + modeTemp, arial10format);
		WritableCell mza = new Label(2,1, "Mzabs: " + mzabs, arial10format);
		WritableCell mzp = new Label(2,2, "Mzppm: " + mzppm, arial10format);
		
		
		
		
		WritableCell header00 = new Label(0, 4, "Rank", arial10format);
		WritableCell header0 = new Label(1, 4, "Score", arial10format);
		WritableCell header1 = new Label(2, 4, "# of Peaks Explained", arial10format);
		WritableCell header2 = new Label(3, 4, "Molecular Formula", arial10format);
		WritableCell header3 = new Label(4, 4, "Exact Mass", arial10format);
		WritableCell header4 = new Label(5, 4, "Database ID", arial10format);
		WritableCell header5 = new Label(6, 4, "XlogP", arial10format);
		WritableCell header6 = new Label(7, 4, "AlogP", arial10format);
		WritableCell header7 = new Label(8, 4, "Peaks Explained", arial10format);
		WritableCell header8 = new Label(9, 4, "Image", arial10format);
		WritableCell header9 = new Label(10, 4, "Smiles", arial10format);
		
		try
		{
			sheet.addCell(db);
			sheet.addCell(em);
			sheet.addCell(mf);
			sheet.addCell(bc);
			sheet.addCell(ls);
			sheet.addCell(di);
			sheet.addCell(mo);
			sheet.addCell(mza);
			sheet.addCell(mzp);
			
			
			sheet.addCell(header00);
			sheet.addCell(header0);
			sheet.addCell(header1);
			sheet.addCell(header2);
			sheet.addCell(header3);
			sheet.addCell(header4);
			sheet.addCell(header5);
			sheet.addCell(header6);
			sheet.addCell(header7);
			sheet.addCell(header8);
			sheet.addCell(header9);
		} catch (WriteException e) {
			System.out.println("Could not write excel cell");
			e.printStackTrace();
		}
		int rank = 0;
		for (ResultRowGroupedBean row : resultRowGroupedBeans) {
			int currentRow = i*4 + 1;
			rank++;

			WritableImage wi = null;
			// output is image
			String imgPath = webRoot + row.getImage() + ".png";
			File image = new File(imgPath);
			// write each image into the second column, leave one row space between them and 
			// resize the image to 1 column width and 2 rows height
			wi = new WritableImage(9, currentRow, 1, 3, image);
			sheet.addImage(wi);
			
			// output is text
			WritableCell cellRank = new Label(0, currentRow, Integer.toString(rank));
			WritableCell cellScore = new Label(1, currentRow, row.getScore().toString());
			WritableCell cellExplainedPeaks = new Label(2, currentRow, row.getExplainedPeaks() + "");
			WritableCell cellMolecularFormula = new Label(3, currentRow, row.getMolecularFormula().replaceAll("\\<.*?\\>", ""));
			WritableCell cellMass = new Label(4, currentRow, row.getMass());
			WritableCell cellLink = new Label(5, currentRow, row.getID());
			
			Map<String, Object> descriptors = row.getMoleculeDescriptors();
			
			DescriptorValue xLogP = (DescriptorValue)descriptors.get("XLogP");
			WritableCell cellxLogP = new Label(6, currentRow, xLogP.getValue().toString());
			DescriptorValue aLogP = (DescriptorValue)descriptors.get("ALogP");
			WritableCell cellaLogP = new Label(7, currentRow, aLogP.getValue().toString());
			
			
			String[] peaksArr = row.getPeaksFound().split(",");
			String[] peaksIntArr = row.getPeaksFoundInt().split(",");
			String peaksExplainedString = "";
			for (int j = 0; j < peaksArr.length; j++) {
				if(j == (peaksArr.length - 1))
					peaksExplainedString += peaksArr[j] + " " + peaksIntArr[j];
				else
					peaksExplainedString += peaksArr[j] + " " + peaksIntArr[j] + " ";
			}
			WritableCell peaksExplained = new Label(8, currentRow, peaksExplainedString);
			WritableCell cellSmiles = new Label(10, currentRow, row.getSmiles());
		
			try
			{
				sheet.addCell(cellRank);
				sheet.addCell(cellLink);
				sheet.addCell(cellScore);
				sheet.addCell(cellMolecularFormula);
				sheet.addCell(cellMass);
				sheet.addCell(cellExplainedPeaks);
				sheet.addCell(cellxLogP);
				sheet.addCell(cellaLogP);
				sheet.addCell(peaksExplained);
				sheet.addCell(cellSmiles);
			} catch (WriteException e) {
				System.out.println("Could not write excel cell");
				e.printStackTrace();
			}
			
			i++;
				
	
			if(row.getChildResultRows() != null && row.getChildResultRows().size() > 0)
			{
				for (ResultRowGroupedBean rowChild : row.getChildResultRows()) {
					currentRow = i*4 + 1;
					wi = null;
					// output is image
					imgPath = webRoot + rowChild.getImage() + ".png";
					image = new File(imgPath);
					// write each image into the second column, leave one row space between them and 
					// resize the image to 1 column width and 2 rows height
					wi = new WritableImage(9, currentRow, 1, 3, image);
					sheet.addImage(wi);
					
					// output is text
					cellRank = new Label(0, currentRow, Integer.toString(rank));
					cellScore = new Label(1, currentRow, rowChild.getScore().toString());
					cellExplainedPeaks = new Label(2, currentRow, rowChild.getExplainedPeaks() + "");
					cellMolecularFormula = new Label(3, currentRow, rowChild.getMolecularFormula().replaceAll("\\<.*?\\>", ""));
					cellMass = new Label(4, currentRow, rowChild.getMass());
					cellLink = new Label(5, currentRow, rowChild.getID());
					
					descriptors = rowChild.getMoleculeDescriptors();
					
					xLogP = (DescriptorValue)descriptors.get("XLogP");
					cellxLogP = new Label(6, currentRow, xLogP.getValue().toString());
					aLogP = (DescriptorValue)descriptors.get("ALogP");
					cellaLogP = new Label(7, currentRow, aLogP.getValue().toString());
					
					peaksArr = rowChild.getPeaksFound().split(",");
					peaksIntArr = rowChild.getPeaksFoundInt().split(",");
					peaksExplainedString = "";
					for (int j = 0; j < peaksArr.length; j++) {
						if(j == (peaksArr.length - 1))
							peaksExplainedString += peaksArr[j] + " " + peaksIntArr[j];
						else
							peaksExplainedString += peaksArr[j] + " " + peaksIntArr[j] + " ";
					}
					peaksExplained = new Label(8, currentRow, peaksExplainedString);
					cellSmiles = new Label(10, currentRow, rowChild.getSmiles());
				
					try
					{
						sheet.addCell(cellRank);
						sheet.addCell(cellLink);
						sheet.addCell(cellScore);
						sheet.addCell(cellMolecularFormula);
						sheet.addCell(cellMass);
						sheet.addCell(cellExplainedPeaks);
						sheet.addCell(cellxLogP);
						sheet.addCell(cellaLogP);
						sheet.addCell(peaksExplained);
						sheet.addCell(cellSmiles);
					} catch (WriteException e) {
						System.out.println("Could not write excel cell");
						e.printStackTrace();
					}
					
					i++;
				}				
			}
		}
		
		// write the Excel file
		try {
			workbook.write();
			workbook.close();
		} catch (WriteException ioe) {
			ioe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		// store the current Excel file as output resource for the current workflow
        MyResource xlsResource = new MyResource(ec, resourceName, relPath);
		outputResource = xlsResource;

	}

	
	/** Write out the fragments of one compound 
	 *  
	 *  
	 *  based on method from Michael Gerlich 
	 *  */
	private void generateXLSResourceFragments(String candidate, List<ResultPic> fragPics) {
		
		ExternalContext ec = fc.getExternalContext();
		HttpSession session = (HttpSession) ec.getSession(false);
		String sessionString = session.getId();	
		long time = new Date().getTime();
		
		String currentFolder = webRoot + "FragmentPics" + sep + sessionString + sep;
		String relPath = "./FragmentPics" + sep + sessionString + sep;
		new File(currentFolder).mkdirs();
		
		File dir = new File(currentFolder);
		if(!dir.exists())
			dir.mkdirs();
		
		// skip creation of output resource if file access is denied
		if(!dir.canWrite())
			return;
		resourceNameFrags = candidate + "Fragments_" + time +  ".xls";
		File f = new File(dir, resourceNameFrags);
		try {
			f.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// create new Excel file
		WritableSheet sheet = null;
		WritableWorkbook workbook = null;
		WorkbookSettings settings = new WorkbookSettings();
//		settings.setLocale(fc.getViewRoot().getLocale());
		try {
			workbook = Workbook.createWorkbook(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// set sheet name (output port) and position
		sheet = workbook.createSheet("MetFrag fragments", 0);
		WritableFont arial10font = null;
		WritableCellFormat arial10format = null;
		// set header for sheet, name it after output port name 
		try {
			arial10font = new WritableFont(WritableFont.ARIAL, 10);
			arial10format = new WritableCellFormat(arial10font);
			arial10font.setBoldStyle(WritableFont.BOLD);
			Label label = new Label(0, 0, "MetFrag Fragments Results Table", arial10format);
			sheet.addCell(label);
		} catch (WriteException we) {
			we.printStackTrace();
		}
		
		
		// for each workflow output port, create new sheet inside Excel file and store results
		int i = 0;
		WritableCell header0 = new Label(0, 0, "Fragment", arial10format);
		WritableCell header1 = new Label(3, 0, "Mass", arial10format);
		WritableCell header2 = new Label(4, 0, "Formula", arial10format);
		
		try
		{
			sheet.addCell(header0);
			sheet.addCell(header1);
			sheet.addCell(header2);

		} catch (WriteException e) {
			System.out.println("Could not write excel cell");
			e.printStackTrace();
		}
		
		for (ResultPic frag : fragPics) {
			int currentRow = i*7 + 1;
			
			WritableImage wi = null;
			// output is image
			String imgPath = webRoot + frag.getPath() + ".png";
			File image = new File(imgPath);
			// write each image into the second column, leave one row space between them and 
			// resize the image to 1 column width and 2 rows height
			wi = new WritableImage(0, currentRow, 3, 7, image);
			sheet.addImage(wi);
			
			// output is text
			WritableCell cellExplainedMass = new Label(3, currentRow, frag.getMass().replaceAll("\\<.*?\\>", ""));
			WritableCell cellMolecularFormula = new Label(4, currentRow, frag.getMolecularFormula().replaceAll("\\<.*?\\>", ""));
			
			
			try
			{
				sheet.addCell(cellMolecularFormula);
				sheet.addCell(cellExplainedMass);
			} catch (WriteException e) {
				System.out.println("Could not write excel cell");
				e.printStackTrace();
			}
			
			i++;
		}
		
		// write the Excel file
		try {
			workbook.write();
			workbook.close();
		} catch (WriteException ioe) {
			ioe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		// store the current Excel file as output resource for the current workflow
        MyResource xlsResource = new MyResource(ec, resourceNameFrags, relPath);
		setOutputResourceFrags(xlsResource);

	}
	
	
	
	/**
	 * Writes out the fragments from one compound to SDF file
	 * 
	 * @param event the event
	 */
	public void listenSDFDownloadFragments(ActionEvent event) {
		//gets the current row from the data table
		ResultRow row = (ResultRow) event.getComponent().getAttributes().get("currentRow");
		
		//now set the the data accordingly
		String id = row.getID();
		resourceNameSDF = id + "_" + "fragments.sdf";
		try {
			
			//get charge
			boolean isPositive = true;
			if(charge.contains("-"))
				isPositive = false;
			Vector<PeakMolPair> fragments = MetFrag.startConvenienceWeb(this.peaks, row.getSmiles(), Integer.parseInt(this.mode), molFormulaRedundancyCheck, Double.parseDouble(mzabs), Double.parseDouble(mzppm), Integer.parseInt(this.treeDepth), isPositive);
			MoleculeSet setOfFragments = new MoleculeSet();
			
			ExternalContext ec = fc.getExternalContext();
			HttpSession session = (HttpSession) ec.getSession(false);
			String sessionString = session.getId();	
			
			String currentFolder = webRoot + "FragmentPics" + sep + sessionString + sep;
			String relPath = "./FragmentPics" + sep + sessionString + sep;
			new File(currentFolder).mkdirs();
			
			SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
			IAtomContainer molecule = sp.parseSmiles(row.getSmiles());
			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
			IMolecule molOrig = new Molecule(AtomContainerManipulator.removeHydrogens(molecule));
			molOrig.setProperty("Mass", row.getFrags().get(0).getMass().replaceAll("\\<.*?\\>", ""));
			molOrig.setProperty("Formula", row.getFrags().get(0).getMolecularFormula().replaceAll("\\<.*?\\>", ""));
			setOfFragments.addAtomContainer(molOrig);
			
			int count = 1;
			for (PeakMolPair frag : fragments) {
				
				//fix for bug in mdl reader setting where it happens that bond.stereo is null when the bond was read in as UP/DOWN (4)
				for (IBond bond : frag.getFragment().bonds()) {
					if(bond.getStereo() == null)
						bond.setStereo(Stereo.UP_OR_DOWN);		
				} 
				IMolecule mol = new Molecule(AtomContainerManipulator.removeHydrogens(frag.getFragment()));
				mol.setProperty("Mass", row.getFrags().get(count).getMass().replaceAll("\\<.*?\\>", ""));
				mol.setProperty("Formula", row.getFrags().get(count).getMolecularFormula().replaceAll("\\<.*?\\>", ""));
				setOfFragments.addAtomContainer(mol);
				count++;
			}
			
			//write results file
			try {
				SDFWriter writer = new SDFWriter(new FileWriter(new File(currentFolder + resourceNameSDF)));
				writer.write(setOfFragments);
				writer.close();
			} catch (CDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//generate xls file as well
			generateXLSResourceFragments(id, row.getFrags());
			
			// store the current Excel file as output resource for the current workflow
	        MyResource sdfResource = new MyResource(ec, resourceNameSDF, relPath);
			outputResourceSDF = sdfResource;
		
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       

	}

	
	/**
	 * Delete the current Feedback
	 * 
	 * @param event the event
	 */
	public void listenDeleteFeedbackAction(ActionEvent event) {
		//gets the current row from the data table
		currentFeedback = (FeedbackRow) event.getComponent().getAttributes().get("currentFeedbackRow");
		
		//now set the the data accordingly
		Integer id = currentFeedback.getId();
		Connection con = null;
		
		try {
	    	String driver = "com.mysql.jdbc.Driver"; 
			Class.forName(driver); 
			DriverManager.registerDriver (new com.mysql.jdbc.Driver()); 
	        // JDBC-driver
	        Class.forName(driver);
	        con = DriverManager.getConnection(db, user, pass);
		    

            String sql = "Delete from Feedback where ID = ?";
            PreparedStatement pst = null;
            pst = con.prepareStatement(sql);
            pst.setInt(1, id);
            pst.executeUpdate();
            pst.close();

        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
        finally{
            try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        getFeedback();
		
	}


	/**
	 * Sets the peak list.
	 * 
	 * @param peakList the new peak list
	 */
	public void setPeakList(String peakList) {
		this.peakListString = peakList;
	}

	/**
	 * Gets the peak list.
	 * 
	 * @return the peak list
	 */
	public String getPeakList() {
		return peakListString;
	}

	
	/**
	 * Sets the result row.
	 * 
	 * @param mode the mode
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * Gets the mode.
	 * 
	 * @return the mode
	 */
	public String getMode() {
		return this.mode;
	}
	
	/**
	 * Sets the result row.
	 * 
	 * @param exactMass the exact mass
	 */
	public void setExactMass(String exactMass) {
		this.exactMass = exactMass;
	}

	/**
	 * Gets the exact mass.
	 * 
	 * @return the exact mass
	 */
	public String getExactMass() {
		return exactMass;
	}

	
	/**
	 * Sets the mzabs.
	 * 
	 * @param mzabs the new mzabs
	 */
	public void setMzabs(String mzabs) {
		this.mzabs = mzabs;
	}

	/**
	 * Gets the mz abs.
	 * 
	 * @return the mz abs
	 */
	public String getMzabs() {
		return this.mzabs;
	}
	
	/**
	 * Sets the mz ppm.
	 * 
	 * @param mzppm the new mz ppm
	 */
	public void setMzppm(String mzppm) {
		this.mzppm = mzppm;
	}

	/**
	 * Gets the mz ppm.
	 * 
	 * @return the mz ppm
	 */
	public String getMzppm() {
		return this.mzppm;
	}


	public void setAddCommand(UICommand addCommand) {
		this.addCommand = addCommand;
	}


	public UICommand getAddCommand() {
		return addCommand;
	}


	public void setForm(UIForm form) {
		this.form = form;
	}


	public UIForm getForm() {
		return form;
	}


	public void setTableForm(UIForm tableForm) {
		this.tableForm = tableForm;
	}


	public UIForm getTableForm() {
		return tableForm;
	}
	
	
	public void setCount(int count) {
		this.count[0] = count;
	}


	public int getCount() {
		return count[0];
	}


	public void setHitsDatabase(int hitsDatabase) {
		this.hitsDatabase = hitsDatabase;
	}


	public int getHitsDatabase() {
		return hitsDatabase;
	}


	public void setRendered(boolean rendered) {
		this.rendered = rendered;
	}


	public boolean isRendered() {
		return rendered;
	}


	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}


	public boolean isEnabled() {
		return enabled;
	}


	public void setDisplayResults(boolean displayResults) {
		this.displayResults = displayResults;
	}


	public boolean isDisplayResults() {
		return displayResults;
	}


	public void setPeaks(String peaks) {
		this.peaks = peaks;
	}


	public String getPeaks() {
		return peaks;
	}


	public void setCurrentRow(int currentRow) {
		this.currentRow = currentRow;
	}


	public int getCurrentRow() {
		return currentRow;
	}


	public void setKeys(Set<Integer> keys) {
		this.keys = keys;
	}


	public Set<Integer> getKeys() {
		return keys;
	}


	public void setCurrentItem(ResultRow currentItem) {
		this.currentItem = currentItem;
	}


	public ResultRow getCurrentItem() {
		return currentItem;
	}
	
	public String getDatabase()
	{
		return this.database;
	}
	
	public void setDatabase(String database)
	{
		this.database = database;
	}


	public void setSumFormula(String sumFormula) {
		this.molFormula = sumFormula;
	}


	public String getSumFormula() {
		return molFormula;
	}
	
	public void setLimit(String limit) {
		this.limit = limit;
	}


	public String getLimit() {
		return this.limit;
	}


	public void setSumFormulaRedundancyCheck(boolean sumFormulaRedundancyCheck) {
		this.molFormulaRedundancyCheck = sumFormulaRedundancyCheck;
	}


	public boolean isSumFormulaRedundancyCheck() {
		return molFormulaRedundancyCheck;
	}


	public void setSearchPPM(String searchPPM) {
		this.searchPPM = searchPPM;
	}


	public String getSearchPPM() {
		return searchPPM;
	}


	public void setStop(boolean stop) {
		this.stop = stop;
	}


	public static boolean isStop() {
		return stop;
	}


	public void setRevision(String revision) {
		this.revision = revision;
	}


	public String getRevision() {
		return revision;
	}


	public void setBuildDate(String buildDate) {
		this.buildDate = buildDate;
	}


	public String getBuildDate() {
		return buildDate;
	}


	public void setDatabaseID(String databaseID) {
		this.databaseID = databaseID;
	}


	public String getDatabaseID() {
		return databaseID;
	}


	public void setBuildDateWeb(String buildDateWeb) {
		this.buildDateWeb = buildDateWeb;
	}


	public String getBuildDateWeb() {
		return buildDateWeb;
	}


	public void setRevisionWeb(String revisionWeb) {
		this.revisionWeb = revisionWeb;
	}


	public String getRevisionWeb() {
		return revisionWeb;
	}
	
	public String getsvnLog(){
		return svnLog;		
	}
	
	public String getsvnLogWeb(){
		return svnLogWeb;		
	}


	public void setBioCompound(boolean bioCompound) {
		this.bioCompound = bioCompound;
	}


	public boolean isBioCompound() {
		return bioCompound;
	}

	public static String getScorecol() {
		return scoreCol;
	}


	public static String getExplainedpeakscol() {
		return explainedPeaksCol;
	}


	public static String getDatabaseidcol() {
		return databaseIDCol;
	}


	public List<ResultPic> getCurrentFrags() {
		return currentFrags;
	}


	public void setCurrentFrags(List<ResultPic> currentFrags) {
		this.currentFrags = currentFrags;
	}


	public int getPercentDone() {
		return percentDone;
	}


	public void setPercentDone(int percentDone) {
		this.percentDone = percentDone;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getComment() {
		return comment;
	}


	public void setComment(String comment) {
		this.comment = comment;
	}


	public boolean isFeedbackAllow() {
		return feedbackAllow;
	}


	public void setFeedbackAllow(boolean feedbackAllow) {
		this.feedbackAllow = feedbackAllow;
	}


	public void setError(String error) {
		this.error = error;
	}


	public String getError() {
		return error;
	}
	public void setFeedbackList(List<FeedbackRow> feedbackList) {
		this.feedbackList = feedbackList;
	}
	public List<FeedbackRow> getFeedbackList() {
		return feedbackList;
	}
	public void setAdminUserInput(String adminUserInput) {
		this.adminUserInput = adminUserInput;
	}
	public String getAdminUserInput() {
		return adminUserInput;
	}
	public void setAdminPassInput(String adminPassInput) {
		this.adminPassInput = adminPassInput;
	}
	public String getAdminPassInput() {
		return adminPassInput;
	}
	public void setShowLoginData(boolean showLoginData) {
		this.showLoginData = showLoginData;
	}
	public boolean isShowLoginData() {
		return showLoginData;
	}
	public void setAdminError(String adminError) {
		this.adminError = adminError;
	}
	public String getAdminError() {
		return adminError;
	}
	public void setTreeDepth(String treeDepth) {
		this.treeDepth = treeDepth;
	}
	public String getTreeDepth() {
		return treeDepth;
	}
	public void setSDFFile(boolean isSDFFile) {
		this.isSDFFile = isSDFFile;
	}
	public boolean isSDFFile() {
		return isSDFFile;
	}
	public void setSDFSelected(boolean isSDFSelected) {
		this.sdfSelect = isSDFSelected;
	}
	public boolean isSDFSelected() {
		return sdfSelect;
	}
	public void setParsedPeaksDebug(String parsedPeaksDebug) {
		this.parsedPeaksDebug = parsedPeaksDebug;
	}
	public String getParsedPeaksDebug() {
		return parsedPeaksDebug;
	}
	public void setDatabaseMessage(String databaseMessage) {
		this.databaseMessage = databaseMessage;
	}
	public String getDatabaseMessage() {
		return databaseMessage;
	}
	public void setThreads(int threads) {
		this.threads = threads;
	}
	public int getThreads() {
		return threads;
	}
	public void setIpbAccess(boolean isIpbAccess) {
		this.isIpbAccess = isIpbAccess;
	}
	public boolean isIpbAccess() {
		return isIpbAccess;
	}
	public void setOutputResource(Resource outputResource) {
		this.outputResource = outputResource;
	}
	public Resource getOutputResource() {
		return outputResource;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	public String getResourceName() {
		return resourceName;
	}
	public void setOutputResourceSDF(Resource outputResourceSDF) {
		this.outputResourceSDF = outputResourceSDF;
	}
	public Resource getOutputResourceSDF() {
		return outputResourceSDF;
	}
	public void setResourceNameSDF(String resourceNameSDF) {
		this.resourceNameSDF = resourceNameSDF;
	}
	public String getResourceNameSDF() {
		return resourceNameSDF;
	}
	public void setOutputResourceFrags(Resource outputResourceFrags) {
		this.outputResourceFrags = outputResourceFrags;
	}
	public Resource getOutputResourceFrags() {
		return outputResourceFrags;
	}
	public String getResourceNameFrags() {
		return resourceNameFrags;
	}
	public void setResourceNameFrags(String resourceNameFrags) {
		this.resourceNameFrags = resourceNameFrags;
	}
	public void setLog(String log) {
		this.log = log;
	}
	public String getLog() {
		return log;
	}
	public void setCharge(String charge) {
		this.charge = charge;
	}
	public String getCharge() {
		return charge;
	}
	public void setMassInput(String massInput) {
		this.massInput = massInput;
	}
	public String getMassInput() {
		return massInput;
	}
	public void setMassAdduct(String massAdduct) {
		this.massAdduct = massAdduct;
	}
	public String getMassAdduct() {
		return massAdduct;
	}
	public void setChangelog(String changelog) {
		this.changelog = changelog;
	}
	public String getChangelog() {
		return changelog;
	}


}
