package de.ipbhalle.metfrag.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

import javax.faces.application.Application;
import javax.faces.component.UICommand;
import javax.faces.component.UIForm;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.formula.MolecularFormula;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import com.icesoft.faces.component.inputfile.FileInfo;
import com.icesoft.faces.component.inputfile.InputFile;
import com.icesoft.faces.context.effects.JavascriptContext;
import com.icesoft.faces.webapp.xmlhttp.PersistentFacesState;

import de.ipbhalle.metfrag.buildinfo.BuildInfo;
import de.ipbhalle.metfrag.chemspiderClient.ChemSpider;
import de.ipbhalle.metfrag.fragmenter.Fragmenter;
import de.ipbhalle.metfrag.keggWebservice.KeggWebservice;
import de.ipbhalle.metfrag.main.AssignFragmentPeak;
import de.ipbhalle.metfrag.main.PeakMolPair;
import de.ipbhalle.metfrag.massbankParser.Peak;
import de.ipbhalle.metfrag.molDatabase.BeilsteinLocal;
import de.ipbhalle.metfrag.molDatabase.PubChemLocal;
import de.ipbhalle.metfrag.pubchem.PubChemWebService;
import de.ipbhalle.metfrag.read.Molfile;
import de.ipbhalle.metfrag.read.SDFFile;
import de.ipbhalle.metfrag.scoring.Scoring;
import de.ipbhalle.metfrag.similarity.Similarity;
import de.ipbhalle.metfrag.similarity.SimilarityGroup;
import de.ipbhalle.metfrag.spectrum.CleanUpPeakList;
import de.ipbhalle.metfrag.spectrum.WrapperSpectrum;
import de.ipbhalle.metfrag.tools.DisplayStructureVector;
import de.ipbhalle.metfrag.tools.MolecularFormulaTools;
import de.ipbhalle.metfrag.tools.PPMTool;
import de.ipbhalle.metfrag.web.buildinfo.BuildInfoWeb;
import de.ipbhalle.metfrag.web.model.FeedbackRow;
import de.ipbhalle.metfrag.web.model.MetFragObject;
import de.ipbhalle.metfrag.web.model.ResultPeaks;
import de.ipbhalle.metfrag.web.model.ResultPic;
import de.ipbhalle.metfrag.web.model.ResultRow;
import de.ipbhalle.metfrag.web.model.SortableList;




/**
 * The Class FragSearchController.
 */
public class MetFragBean extends SortableList{
	
	private boolean molFormulaRedundancyCheck = true;
	private String database = "kegg";
	private String molFormula = "";
	private String limit = "100";
	private String peakListString;
	private String mode = "1";
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
	private String peaks = "119.051 467.616 45\n" +
	   "123.044 370.662 36\n" +
	   "147.044 6078.145 606\n" +
	   "153.019 10000.0 999\n" +
	   "179.036 141.192 13\n" +
	   "189.058 176.358 16\n" +
	   "273.076 10000.000 999\n" +
	   "274.083 318.003 30\n";	
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
    
    
    private boolean isSDFFile = false;
    private boolean sdfSelect = false;
    private String componentStatus;
    private String fileLocation;
    private FileInfo currentFile;
	private int fileProgress;
	private List<IAtomContainer> uploadedSDFCompounds;
	
	private String parsedPeaksDebug = "";

	   
	
	/**
	 * Instantiates a new metFrag controller.
	 */
	public MetFragBean(){
		super(scoreCol);
		getConfig();
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
    	if(newValue.equals("sdf"))
    	{
    		sdfSelect = true;
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
		
		
		//peakListString = peaks;
		double exactMass = 0.0;
		if(this.exactMass != "")
			exactMass = Double.parseDouble(this.exactMass);
		
		//double mzabs = Double.parseDouble(this.mzabs);
		//double mzppm = Double.parseDouble(this.mzppm);
		
		System.out.println("Search PPM: " + this.searchPPM);
		
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
				candidates = KeggWebservice.KEGGbyMass(exactMass, (PPMTool.getPPMDeviation(exactMass, Double.parseDouble(this.searchPPM))));
		}
		else if(this.database.equals("chemspider") && databaseID.equals(""))
		{
			if(this.molFormula != "")
				candidates = ChemSpider.getChemspiderBySumFormula(this.molFormula);
			else
				candidates = ChemSpider.getChemspiderByMass(exactMass, (PPMTool.getPPMDeviation(exactMass, Double.parseDouble(this.searchPPM))));
		}
		else if(this.database.equals("pubchem") && databaseID.equals(""))
		{
			double lowerBound = exactMass - PPMTool.getPPMDeviation(exactMass, Double.parseDouble(this.searchPPM)); 
			double upperBound = exactMass + PPMTool.getPPMDeviation(exactMass, Double.parseDouble(this.searchPPM));
			
			if(this.molFormula != "")
				candidates = pubchem.getHitsbySumFormula(molFormula);
			else
				candidates = pubchemLocal.getHitsVector(lowerBound, upperBound);
		}
		else if(this.database.equals("beilstein") && databaseID.equals(""))
		{
			this.beilstein = new BeilsteinLocal(db, user, pass);
			double lowerBound = exactMass - PPMTool.getPPMDeviation(exactMass, Double.parseDouble(this.searchPPM)); 
			double upperBound = exactMass + PPMTool.getPPMDeviation(exactMass, Double.parseDouble(this.searchPPM));

			candidates = beilstein.getHitsVector(lowerBound, upperBound);
		}
		else if (!databaseID.equals(""))
		{
			candidates = new Vector<String>();
			String[] idList = databaseID.split(",");
			for (int i = 0; i < idList.length; i++) {
				candidates.add(idList[i]);
			}
		}
		
		if(Integer.parseInt(this.limit) < this.hitsDatabase)
			this.hitsDatabase = Integer.parseInt(this.limit);
		else
			this.hitsDatabase = candidates.size();
		
		return "";
	}
	
	public String startMetFrag()
	{	
		try {
			metFrag();
		} catch (Exception e) {
			e.printStackTrace();
		}	
		this.rendered = false;
		
		return "";
	}
	
	public String startMetFragParallel()
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
		
		
//		setExactMass("272.06847");
//		setPeaks("119.051 467.616 45\n" +
//				   "123.044 370.662 36\n" +
//				   "147.044 6078.145 606\n" +
//				   "153.019 10000.0 999\n" +
//				   "179.036 141.192 13\n" +
//				   "189.058 176.358 16\n" +
//				   "273.076 10000.000 999\n" +
//				   "274.083 318.003 30\n");
//		setMzabs("0.01");
//		setMzppm("50");
//		setSumFormula("");
//		setLimit("100");
//		setSumFormulaRedundancyCheck(false);
//		setDatabase("kegg");

		
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
		// Interrupt the thread so the user gets an immediate response
        if (progressThread != null) {
            progressThread.interrupt();
        }
        
        
//        percentDone = 0;
//		count[0] = 0;
//		this.hitsDatabase = 0;
		
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
	            		"DatabaseIDs, Mode, MzAbs, MzPPM, Email, Comment, Date) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?, now())";
	            PreparedStatement pst = null;
	            pst = con.prepareStatement(sql);
	
	            pst.setString(1, peaks.replace("\n", "%BR%"));
	            
	            String tempExactMass = "";
	            if(exactMass == "")
	            	tempExactMass = "-1.0";
	            else
	            	tempExactMass = exactMass;
	            
	            pst.setDouble(2, Double.parseDouble(tempExactMass));
	            pst.setInt(3, Integer.parseInt(searchPPM));
	            pst.setString(4, molFormula);
	            pst.setString(5, database);
	            
	            int bioTemp = 0;
	            if(bioCompound)
	            	bioTemp = 1;
	            
	            pst.setInt(6, bioTemp);
	            pst.setInt(7, Integer.parseInt(limit));
	            pst.setString(8, databaseID);
	            
	            int modeTemp = 1;
	            if(mode.equals("-1"))
	            	modeTemp = -1;
	            
	            pst.setInt(9, modeTemp);
	            pst.setDouble(10, Double.parseDouble(mzabs));
	            pst.setInt(11, Integer.parseInt(mzppm));
	            pst.setString(12, email);
	            pst.setString(13, comment);
	
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
	            		"DatabaseIDs, Mode, MzAbs, MzPPM, Email, Comment, Date, Fixed, Answered FROM Feedback;";
	            Statement stmt = con.createStatement();
	            ResultSet rs = stmt.executeQuery(sql);
	            while(rs.next())
	            {
	            	feedbackList.add(new FeedbackRow(rs.getInt("ID"), rs.getString("Peaklist").replace("%BR%", "\n"), rs.getDouble("ExactMass"), rs.getInt("SearchPPM"), 
	            			rs.getString("MolecularFormula"), rs.getString("databaseUsed"), rs.getBoolean("BiologicalCompound"), rs.getInt("LimitHits"), 
	            			rs.getString("DatabaseIDs"), rs.getInt("Mode"), rs.getDouble("MzAbs"), rs.getInt("MzPPM"), rs.getString("Email"), rs.getString("Comment"),
	            			rs.getBoolean("Fixed"), rs.getBoolean("Answered"), rs.getDate("Date")));
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
    	double exactMassThread = 0.0;
		if(exactMass != "")
			exactMassThread = Double.parseDouble(exactMass);
		else
		{
			IMolecularFormula formula = new MolecularFormula();
			formula = MolecularFormulaManipulator.getMolecularFormula(molFormula, formula);
			exactMassThread = MolecularFormulaTools.getMonoisotopicMass(formula);
		}
    	WrapperSpectrum spectrum = new WrapperSpectrum(peaks, Integer.parseInt(mode), exactMassThread);
    	Vector<Peak> peakListParsed = spectrum.getPeakList();
    	parsedPeaksDebug = "<table border='0' cellspacing='4' cellpadding='6'>" +
    			"<tr>" +
    				"<td>Peak#</td><td>m/z</td><td>abs. int.</td><td>rel. int.</td>" +
    			"</tr>";
    	int count = 1;
    	for (Peak peak : peakListParsed) {
			parsedPeaksDebug += "<tr><td>" + count + "</td><td>" + peak.getMass() + "</td><td>" + peak.getIntensity() + "</td><td>" + peak.getRelIntensity() + "</td><td></tr>";
			count++;
		}
    	parsedPeaksDebug += "</table>";
    }
    
    
    /**
	 * MetFrag!!!! :) Just will eventually replace the old version
	 * 
	 * @throws Exception the exception
	 */
	public void metFragParallel()
	{
		this.enabled = true;
		state = PersistentFacesState.getInstance();
		// Create the progress thread
        progressThread = new Thread(new Runnable() {
        	public void run() {
        		try
        		{
        			
        			//now fill executor!!!
    				//number of threads depending on the available processors
    			    int threads = Runtime.getRuntime().availableProcessors();
    			    
    			    //thread executor
    			    ExecutorService threadExecutor = null;
    			    System.out.println("Used Threads: " + threads);
    			    threadExecutor = Executors.newFixedThreadPool(threads);

    			    
        			
					double exactMassThread = 0.0;
					if(exactMass != "")
						exactMassThread = Double.parseDouble(exactMass);
					else
					{
						IMolecularFormula formula = new MolecularFormula();
						formula = MolecularFormulaManipulator.getMolecularFormula(molFormula, formula);
						exactMassThread = MolecularFormulaTools.getMonoisotopicMass(formula);
					}
					double mzabsThread = Double.parseDouble(mzabs);
					double mzppmThread = Double.parseDouble(mzppm);
					WrapperSpectrum spectrum = new WrapperSpectrum(peaks, Integer.parseInt(mode), exactMassThread);
					
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
						
						if(molecule == null)
							continue;
						
						
						//skip if molecule is not connected
						boolean isConnected = ConnectivityChecker.isConnected(molecule);
						if(!isConnected)
							continue;
						
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
						
						threadExecutor.execute(new ParallelFragmentation(metFragData, pubchemLocal, beilstein, candidateToResult, realScoreMap, sessionString, webRoot, count));
		
						float test = (count[0] / (float)candidates.size()) * (float)100;
						percentDone = Math.round(test);
						
						state.render();
						
						
						if(tempCount == Integer.parseInt(limit))
						{
							break;
						}
					}
					
					
					threadExecutor.shutdown();
					//wait until all threads are finished
					while(!threadExecutor.isTerminated())
					{
						try {
							Thread.currentThread().sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}//sleep for 1000 ms
						
						float test = (count[0] / (float)candidates.size()) * (float)100;
						percentDone = Math.round(test);
						
						state.render();
					}
					
					// initiate the list
			        if (resultRowGroupedBeans != null) {
			            resultRowGroupedBeans.clear();
			        } else {
			            resultRowGroupedBeans = new ArrayList(10);
			        }
					
					
					Application application = fc.getApplication();
			        StyleBean styleBean = ((StyleBean) application.createValueBinding("#{styleBean}").getValue(fc));
			        Similarity sim = new Similarity(candidateToStructure, 0.95f, true);
			        
					if(!bondEnergyScoring)
						normalize();
					else
						normalizeWithBondEnergy(realScoreMap);
			        
			        
			        for (Double score : realScoreMap.keySet()) {
						List<String> candidates = realScoreMap.get(score);
						List<SimilarityGroup> simGroups = sim.getTanimotoDistanceList(candidates);
						for (SimilarityGroup similarityGroup : simGroups) {
							if(similarityGroup.getSimilarCompounds().size() == 0)
							{
								ResultRowGroupedBean filesRecordGroup = new ResultRowGroupedBean("",
										"",
				                        styleBean,
				                        SPACER_IMAGE, SPACER_IMAGE,
				                        resultRowGroupedBeans, false);
								String candidate = similarityGroup.getSimilarCandidatesWithBase().get(0);
								addToResultsList(candidate, filesRecordGroup);								
							}
							else
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
									ResultRowGroupedBean childFilesGroup = new ResultRowGroupedBean(CHILD_INDENT_STYLE_CLASS, CHILD_ROW_STYLE_CLASS);
									addToResultsList(similarityGroup.getSimilarCompounds().get(i), childFilesGroup);
							        filesRecordGroup.addChildFilesGroupRecord(childFilesGroup);
								}
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
	 * MetFrag!!!! :)
	 * 
	 * @throws Exception the exception
	 */
	public void metFrag()
	{
		this.enabled = true;
		state = PersistentFacesState.getInstance();
		// Create the progress thread
        progressThread = new Thread(new Runnable() {
        	public void run() {
        		try
        		{
					double exactMassThread = 0.0;
					if(exactMass != "")
						exactMassThread = Double.parseDouble(exactMass);
					else
					{
						IMolecularFormula formula = new MolecularFormula();
						formula = MolecularFormulaManipulator.getMolecularFormula(molFormula, formula);
						exactMassThread = MolecularFormulaTools.getMonoisotopicMass(formula);
					}
					double mzabsThread = Double.parseDouble(mzabs);
					double mzppmThread = Double.parseDouble(mzppm);
					WrapperSpectrum spectrum = new WrapperSpectrum(peaks, Integer.parseInt(mode), exactMassThread);
					Map<Integer, ArrayList<String>> scoreMap = new HashMap<Integer, ArrayList<String>>();
					Map<Double, Vector<String>> realScoreMap = new HashMap<Double, Vector<String>>();
					
					
					hitsDatabase = candidates.size();
					
					percentDone = 0;
					count[0] = 0;
					
					Map<String, IAtomContainer> candidateToStructure = new HashMap<String, IAtomContainer>();
					
					for (int c = 0; c < candidates.size(); c++) {
						
						//initialize spectrum
						//XYSeries xyNotFound = new XYSeries("Not Found");
						//XYSeries xyFound = new XYSeries("Found");
						//XYSeries xyNotUsed = new XYSeries("Not Used");
						ResultPeaks peaksFound = new ResultPeaks();
						ResultPeaks peaksNotFound = new ResultPeaks();
						ResultPeaks peaksNotUsed = new ResultPeaks();
						Vector<Peak> listOfPeaks = new Vector<Peak>();
						
						
						if(stop)
							break;
						
						//stores the path to the pics from the explained peaks
						List<ResultPic> fragsPics = new ArrayList<ResultPic>();
						
				        //get mol file from kegg....remove "cpd:"
						String candidateID = getCandidateID(database, candidates.get(c));
						IAtomContainer molecule = getMolecule(database, candidateID);
						
						if(molecule == null)
							continue;
						
						
						//skip if molecule is not connected
						boolean isConnected = ConnectivityChecker.isConnected(molecule);
						if(!isConnected)
							continue;
				        						
				        try
				        {
					        //add hydrogens
					        CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(molecule.getBuilder());
			
					        for (IAtom atom : molecule.atoms()) {
					          IAtomType type = matcher.findMatchingAtomType(molecule, atom);
					          AtomTypeManipulator.configure(atom, type);
					        }
					        CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(molecule.getBuilder());
					        hAdder.addImplicitHydrogens(molecule);
					        AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);
				        }
				        //there is a bug in cdk?? error happens when there is a S or Ti in the molecule
				        catch(IllegalArgumentException e)
			            {
				        	count[0] = count[0] + 1;
			            	continue;
			            }
				        
				        //fill map with structures
				        candidateToStructure.put(candidateID, molecule);
									        
						String currentFolder = webRoot + sep + "FragmentPics" + sep + sessionString + sep + candidateID + sep;
						new File(currentFolder).mkdirs();
				        
				        //render original compound....thats the first picture in the list
						int countTemp = 0;
						//DisplayStructure dsOrig = new DisplayStructure(false, 200, 200, 0.9, true, "png", currentFolder, candidateID + "_" + countTemp);
						//dsOrig.drawStructure(molecule, countTemp);
						
			//			CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(molecule.getBuilder());
			//	        adder.addImplicitHydrogens(molecule);
			//	        AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule); 
						
						DisplayStructureVector dsvOrig = new DisplayStructureVector(200,200, currentFolder, false, true);
						dsvOrig.writeMOL2PNGFile(molecule, candidateID + "_" + countTemp + ".png");
						DisplayStructureVector dsvOrigLarge = new DisplayStructureVector(350,350, currentFolder, false, true);
						dsvOrigLarge.writeMOL2PNGFile(molecule, candidateID + "_" + countTemp + "_Large.png");
						
						IMolecularFormula molFormula = MolecularFormulaManipulator.getMolecularFormula(molecule);
						Double massDoubleOrig = MolecularFormulaTools.getMonoisotopicMass(molFormula);
						massDoubleOrig = (double)Math.round((massDoubleOrig)*10000)/10000;
						String massOrig = massDoubleOrig.toString();
						fragsPics.add(new ResultPic(sep + "FragmentPics" + sep + sessionString + sep + candidateID + sep + candidateID + "_" + countTemp, massOrig + "(Original Compound)", MolecularFormulaManipulator.getHTML(molFormula)));
						countTemp++;
						        
				        Fragmenter fragmenter = new Fragmenter((Vector<Peak>)spectrum.getPeakList().clone(), mzabsThread, mzppmThread, Integer.parseInt(mode), true, molFormulaRedundancyCheck, false, false);     
				        List<IAtomContainer> l = null;
				        List<File> vec = new ArrayList<File>();
				        try
				        {
				        	//TODO!
				        	vec = fragmenter.generateFragmentsEfficient(molecule, true, Integer.parseInt(treeDepth), candidateID);
				        	l = Molfile.ReadfolderTemp(vec);
				        	//l = fragmenter.generateFragments(molecule, true, 2);
				        }
				        catch(OutOfMemoryError e)
				        {
				        	System.out.println("OUT OF MEMORY ERROR! " + candidateID);
				        	continue;
				        }
			        
				        List<IAtomContainer> fragments = l; 
						
						//get the original peak list again
						Vector<Peak> peakListParsed = spectrum.getPeakList();
						
						
						//clean up peak list
						CleanUpPeakList cList = new CleanUpPeakList((Vector<Peak>) peakListParsed.clone());
						Vector<Peak> cleanedPeakList = cList.getCleanedPeakList(spectrum.getExactMass());
						
						
						//now find corresponding fragments to the mass
						AssignFragmentPeak afp = new AssignFragmentPeak();
						afp.setHydrogenTest(true);
						afp.AssignFragmentPeak(fragments, cleanedPeakList, mzabsThread, mzppmThread, spectrum.getMode(), true);
						Vector<PeakMolPair> hits = afp.getHits();
						
						
						//render all fragments which explain a peak
						Vector<PeakMolPair> allHits = afp.getAllHits();
						allHits = sortBackwards(allHits);
						for (PeakMolPair peakMolPair : allHits) {
							DisplayStructureVector dsv = null;
							DisplayStructureVector dsvLarge = null;
							if(databaseID.equals(""))	
								dsv = new DisplayStructureVector(200,200, currentFolder, false, false);
							else
								dsv = new DisplayStructureVector(200,200, currentFolder, false, false);
								
							dsvLarge = new DisplayStructureVector(350,350, currentFolder, false, false);
							
							dsv.writeMOL2PNGFile(peakMolPair.getFragment(), candidateID + "_" + countTemp + ".png");
							dsvLarge.writeMOL2PNGFile(peakMolPair.getFragment(), candidateID + "_" + countTemp + "_Large.png");
							
							IMolecularFormula fragFormula = MolecularFormulaManipulator.getMolecularFormula(peakMolPair.getFragment());
							Double massDouble = MolecularFormulaTools.getMonoisotopicMass(fragFormula);
							
							massDouble = (double)Math.round(((Double.parseDouble(mode) * MolecularFormulaTools.getMonoisotopicMass("H1")) + massDouble)*10000)/10000;
							
							String modeString = "+";
							if(mode.equals(-1))
								modeString = "-";
							
							fragsPics.add(new ResultPic(sep + "FragmentPics" + sep + sessionString + sep + candidateID + sep + candidateID + "_" + countTemp, peakMolPair.getMatchedMass() + " [" + peakMolPair.getMolecularFormula() + "] <br />(" + PPMTool.getPPMWeb(peakMolPair.getMatchedMass(), peakMolPair.getPeak().getMass()) + " ppm)", MolecularFormulaManipulator.getHTML(fragFormula)));
							//ds.drawStructure(peakMolPair.getFragment(), countTemp);
							countTemp++;
						}
						//end render hits
						
						
						
						
						//now "real" scoring --> depends on intensities
						Scoring score = new Scoring(spectrum.getPeakList());
						double currentScore = 0.0;
						if(bondEnergyScoring)
							currentScore = score.computeScoringWithBondEnergies(hits);
						else
							currentScore = score.computeScoringPeakMolPair(hits);
						
						
						
						//now "real" scoring --> depends on intensities
						//Scoring score = new Scoring(spectrum.getPeakList());
						//scoring with bond energies
						//double currentScore = score.computeScoringWithBondEnergies(afp.getHits());
						//scoring without bond energies
						//double currentScore = score.computeScoring(afp.getHitsMZ());
						
						//save score in hashmap...if there are several hits with the same score --> vector of strings
						if(realScoreMap.containsKey(currentScore))
				        {
				        	Vector<String> tempList = realScoreMap.get(currentScore);
				        	tempList.add(candidateID);
				        	realScoreMap.put(currentScore, tempList);
				        }
				        else
				        {
				        	Vector<String> temp = new Vector<String>();
				        	temp.add(candidateID);
				        	realScoreMap.put(currentScore, temp);
				        }
						
						
						//save score in hashmap...if there are several hits with the same
						//amount of identified peaks --> ArrayList
						if(scoreMap.containsKey(hits.size()))
				        {
				        	ArrayList<String> tempList = scoreMap.get(hits.size());
				        	tempList.add(candidateID);
				        	scoreMap.put(hits.size(), tempList);
				        }
				        else
				        {
				        	ArrayList<String> temp = new ArrayList<String>();
				        	temp.add(candidateID);
				        	scoreMap.put(hits.size(), temp);
				        }
						
						Vector<Double> peaks = new Vector<Double>();
						Vector<Double> intensities = new Vector<Double>();
						
						//get all the identified peaks
						for (int i = 0; i < hits.size(); i++) {
							listOfPeaks.add(hits.get(i).getPeak());
							peaks.add(hits.get(i).getPeak().getMass());
							intensities.add(hits.get(i).getPeak().getRelIntensity());
							//all found peaks are later on marked in the spectrum
							//xyFound.add(hits.get(i).getPeak().getMass(), hits.get(i).getPeak().getRelIntensity());
						}
						peaksFound.setIntensities(intensities);
						peaksFound.setPeaks(peaks);
						
						
						Vector<Double> peaks1 = new Vector<Double>();
						Vector<Double> intensities1 = new Vector<Double>();
						Vector<Double> peaks2 = new Vector<Double>();
						Vector<Double> intensities2 = new Vector<Double>();
						
						//write all peaks which are not explained in the other list
						for(int i = 0; i < peakListParsed.size(); i++)
						{
							if(!listOfPeaks.contains(peakListParsed.get(i)) && cleanedPeakList.contains(peakListParsed.get(i)))
							{
								peaks1.add(peakListParsed.get(i).getMass());
								intensities1.add(peakListParsed.get(i).getRelIntensity());
								//xyNotFound.add(peakListParsed.get(i).getMass(), peakListParsed.get(i).getRelIntensity());
								
							}
							else if(!listOfPeaks.contains(peakListParsed.get(i)))
							{
								peaks2.add(peakListParsed.get(i).getMass());
								intensities2.add(peakListParsed.get(i).getRelIntensity());
								//xyNotUsed.add(peakListParsed.get(i).getMass(), peakListParsed.get(i).getRelIntensity());
							}
						}			
						peaksNotFound.setIntensities(intensities1);
						peaksNotFound.setPeaks(peaks1);
						peaksNotUsed.setIntensities(intensities2);
						peaksNotUsed.setPeaks(peaks2);
						
						List<IAtomContainer> hitsList = new ArrayList<IAtomContainer>();
						for (int i = 0; i < hits.size(); i++) {
							hitsList.add(AtomContainerManipulator.removeHydrogens(hits.get(i).getFragment()));
							//Render.Highlight(AtomContainerManipulator.removeHydrogens(molecule), hitsList , Double.toString(hits.get(i).getPeak()));
						}
						
						
						// initiate the list
				        if (resultRowGroupedBeans != null) {
				            resultRowGroupedBeans.clear();
				        } else {
				            resultRowGroupedBeans = new ArrayList(10);
				        }
				        
				        //now generate the names and links according to the used database
				        String namesString = "";
				        String databaseLink = "";
				        
				        //local database
				        if(database.equals("sdf"))
				        {
				        	namesString = candidateID;
							databaseLink = "";
				        }
						//real result vector
						if(database.equals("kegg"))
						{
							String[] names = KeggWebservice.KEGGgetNameByCpdLocally(candidateID, "/vol/mirrors/kegg/compound");
							namesString = "<ul>";
							for (int i = 0; i < names.length; i++) {
								namesString += "<li>" + names[i] + "</li>";
							}
							namesString += "</ul>";
							databaseLink = "http://www.genome.jp/dbget-bin/www_bget?cpd:" + candidateID;
							
						}
						if(database.equals("chemspider"))
						{
							namesString = candidateID;
							databaseLink = "http://www.chemspider.com/Chemical-Structure." + candidateID + ".html";
						}
						if(database.equals("pubchem"))
						{
							List<String> names = pubchemLocal.getNames(candidateID);
							namesString += "<ul>";
							for (int i = 0; i < names.size(); i++) {
								namesString += "<li>" + names.get(i) + "</li>";
								if(i == 5)
									break;
							}
							namesString += "</ul>";
							databaseLink = "http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?cid=" + candidateID;
						}
						if(database.equals("beilstein"))
						{
							List<String> names = beilstein.getNames(candidateID);
							namesString = "<ul>";
							for (int i = 0; i < names.size(); i++) {
								namesString += "<li>" + names.get(i) + "</li>";
							}
							namesString += "</ul>";
						}
						

						//now add the candidate to the results
						if(!bondEnergyScoring)
							candidateToResult.put(candidateID, new ResultRow(candidateID, namesString, fragsPics.get(0).getPath(), afp.getHits().size(), currentScore, fragsPics, MolecularFormulaManipulator.getHTML(molFormula), massOrig, databaseLink, 
								peaksFound.getPeaksString(), peaksNotFound.getPeaksString(), peaksNotUsed.getPeaksString(), peaksFound.getIntensitiesString(), peaksNotFound.getIntensitiesString(), peaksNotUsed.getIntensitiesString()));
//							resultRows.add(new ResultRow(candidateID, namesString, fragsPics.get(0).getPath(), afp.getHits().size(), currentScore, fragsPics, massOrig, databaseLink, 
//								peaksFound.getPeaksString(), peaksNotFound.getPeaksString(), peaksNotUsed.getPeaksString(), peaksFound.getIntensitiesString(), peaksNotFound.getIntensitiesString(), peaksNotUsed.getIntensitiesString()));
						else
							candidateToResult.put(candidateID, new ResultRow(candidateID, namesString, fragsPics.get(0).getPath(), afp.getHits().size(), currentScore, score.getFragmentBondEnergy(), score.getPenalty(), fragsPics, MolecularFormulaManipulator.getHTML(molFormula), massOrig, databaseLink, 
									peaksFound.getPeaksString(), peaksNotFound.getPeaksString(), peaksNotUsed.getPeaksString(), peaksFound.getIntensitiesString(), peaksNotFound.getIntensitiesString(), peaksNotUsed.getIntensitiesString()));
//							resultRows.add(new ResultRow(candidateID, namesString, fragsPics.get(0).getPath(), afp.getHits().size(), currentScore, score.getFragmentBondEnergy(), score.getPenalty(), fragsPics, massOrig, databaseLink, 
//									peaksFound.getPeaksString(), peaksNotFound.getPeaksString(), peaksNotUsed.getPeaksString(), peaksFound.getIntensitiesString(), peaksNotFound.getIntensitiesString(), peaksNotUsed.getIntensitiesString()));

						
						
						keys.add(currentRow);

						
						count[0] = count[0] + 1;
						float test = (count[0] / (float)candidates.size()) * (float)100;
						percentDone = Math.round(test);
						
						state.render();
						
						//break is enough compounds were processed
						if(count[0] == Integer.parseInt(limit))
							break;
					}
					
					Application application = fc.getApplication();
			        StyleBean styleBean = ((StyleBean) application.createValueBinding("#{styleBean}").getValue(fc));
			        Similarity sim = new Similarity(candidateToStructure, 0.95f, true);
			        
					if(!bondEnergyScoring)
						normalize();
					else
						normalizeWithBondEnergy(realScoreMap);
			        
			        
			        for (Double score : realScoreMap.keySet()) {
						List<String> candidates = realScoreMap.get(score);
						List<SimilarityGroup> simGroups = sim.getTanimotoDistanceList(candidates);
						for (SimilarityGroup similarityGroup : simGroups) {
							if(similarityGroup.getSimilarCompounds().size() == 0)
							{
								ResultRowGroupedBean filesRecordGroup = new ResultRowGroupedBean("",
										"",
				                        styleBean,
				                        SPACER_IMAGE, SPACER_IMAGE,
				                        resultRowGroupedBeans, false);
								String candidate = similarityGroup.getSimilarCandidatesWithBase().get(0);
								addToResultsList(candidate, filesRecordGroup);								
							}
							else
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
									ResultRowGroupedBean childFilesGroup = new ResultRowGroupedBean(CHILD_INDENT_STYLE_CLASS, CHILD_ROW_STYLE_CLASS);
									addToResultsList(similarityGroup.getSimilarCompounds().get(i), childFilesGroup);
							        filesRecordGroup.addChildFilesGroupRecord(childFilesGroup);
								}
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
	private void addToResultsList(String candidate, ResultRowGroupedBean filesRecordGroup)
	{
		ResultRow resultRow = candidateToResult.get(candidate);
		filesRecordGroup.setBondEnergy(resultRow.getBondEnergy());
		filesRecordGroup.setDatabaseLink(resultRow.getDatabaseLink());
		filesRecordGroup.setExplainedPeaks(resultRow.getExplainedPeaks());
		filesRecordGroup.setFrags(resultRow.getFrags());
		filesRecordGroup.setHydrogenPenalty(resultRow.getHydrogenPenalty());
		filesRecordGroup.setID(candidate);
		filesRecordGroup.setImage(resultRow.getImage());
		filesRecordGroup.setMass(resultRow.getMass());
		filesRecordGroup.setMolName(resultRow.getMolName());
		filesRecordGroup.setPeaksFound(resultRow.getPeaksFound());
		filesRecordGroup.setPeaksFoundInt(resultRow.getPeaksFoundInt());
		filesRecordGroup.setPeaksNotFound(resultRow.getPeaksNotFound());
		filesRecordGroup.setPeaksNotFoundInt(resultRow.getPeaksNotFoundInt());
		filesRecordGroup.setPeaksNotUsed(resultRow.getPeaksNotUsed());
		filesRecordGroup.setPeaksNotUsedInt(resultRow.getPeaksNotUsedInt());
		filesRecordGroup.setScore(resultRow.getScore());
		filesRecordGroup.setMolecularFormula(resultRow.getMolecularFormula());
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
		exactMass = currentFeedback.getExactMass().toString();
		limit = currentFeedback.getLimit().toString();
		mode = currentFeedback.getMode().toString();
		molFormula = currentFeedback.getMolecularFormula();
		mzabs = currentFeedback.getMzAbs().toString();
		mzppm = currentFeedback.getMzPPM().toString();
		peaks = currentFeedback.getPeaklist();
		searchPPM = currentFeedback.getSearchPPM().toString();
		
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
		    

            String sql = "Update Feedback set Fixed = 1 where ID = " + id;
            PreparedStatement pst = null;
            pst = con.prepareStatement(sql);
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
		    

            String sql = "Update Feedback set Answered = 1 where ID = " + id;
            PreparedStatement pst = null;
            pst = con.prepareStatement(sql);
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


//	public void setListFragSearch(List<Result> listFragSearch) {
//		this.listFragSearch = listFragSearch;
//	}
//
//
//	public List<Result> getListFragSearch() {
//		return listFragSearch;
//	}
	
	
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


}
