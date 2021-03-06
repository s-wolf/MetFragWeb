package de.ipbhalle.metfrag.web.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import de.ipbhalle.metfrag.chemspiderClient.ChemSpider;
import de.ipbhalle.metfrag.fragmenter.Fragmenter;
import de.ipbhalle.metfrag.keggWebservice.KeggWebservice;
import de.ipbhalle.metfrag.spectrum.AssignFragmentPeak;
import de.ipbhalle.metfrag.main.PeakMolPair;

import de.ipbhalle.metfrag.massbankParser.Peak;
import de.ipbhalle.metfrag.molDatabase.BeilsteinLocal;
import de.ipbhalle.metfrag.molDatabase.PubChemLocal;
import de.ipbhalle.metfrag.pubchem.PubChemWebService;
import de.ipbhalle.metfrag.read.Molfile;
import de.ipbhalle.metfrag.scoring.Scoring;
import de.ipbhalle.metfrag.spectrum.CleanUpPeakList;
import de.ipbhalle.metfrag.spectrum.WrapperSpectrum;
import de.ipbhalle.metfrag.tools.DisplayStructureVector;
import de.ipbhalle.metfrag.tools.MolecularFormulaTools;
import de.ipbhalle.metfrag.tools.PPMTool;
import de.ipbhalle.metfrag.web.model.MetFragObject;
import de.ipbhalle.metfrag.web.model.ResultPeaks;
import de.ipbhalle.metfrag.web.model.ResultPic;
import de.ipbhalle.metfrag.web.model.ResultRow;

public class ParallelFragmentation implements Runnable {
	
	private String sep = System.getProperty("file.separator");
	private MetFragObject metFragData;
	//get folder names and session ID
	private String sessionString;
	private String webRoot;	
	private PubChemLocal pubchemLocal;
	private BeilsteinLocal beilstein;
	private Map<String, ResultRow> candidateToResult;
	private Map<Double, Vector<String>> realScoreMap;
	private int[] count;
	
	/**
	 * Instantiates a new parallel fragmentation. (This is one thread)
	 * 
	 * @param metFragData the met frag data
	 * @param pubchemLocal the pubchem local
	 * @param beilstein the beilstein
	 * @param candidateToResult the candidate to result
	 */
	public ParallelFragmentation(MetFragObject metFragData, PubChemLocal pubchemLocal, BeilsteinLocal beilstein, Map<String, ResultRow> candidateToResult, Map<Double, Vector<String>> realScoreMap,
			String sessionString, String webRoot, int[] count) {
		this.metFragData = metFragData;
		this.pubchemLocal = pubchemLocal;
		this.beilstein = beilstein;
		this.candidateToResult = candidateToResult;
		this.realScoreMap = realScoreMap;
		this.sessionString = sessionString;
		this.webRoot = webRoot;
		this.count = count;
	}
	
	/**
	 * Gets the row data for the compound just computed.
	 * 
	 * @return the row data
	 */
	public Map<String, ResultRow> getRowData()
	{
		return candidateToResult;
	}
	
	
	public void run()
	{
		String database = this.metFragData.getDatabase();
		String databaseID = this.metFragData.getDatabaseID();
		String candidateID = this.metFragData.getCandidate();
		IAtomContainer molecule = this.metFragData.getMolecule();
		Double mzabs = this.metFragData.getMzabs();
		Double mzppm = this.metFragData.getMzppm();
		WrapperSpectrum spectrum = this.metFragData.getSpectrum();
		boolean bondEnergyScoring = this.metFragData.isBondEnergyScoring();
		Integer mode = this.metFragData.getMode();
		
		
		try
		{
			//initialize spectrum
			ResultPeaks peaksFound = new ResultPeaks();
			ResultPeaks peaksNotFound = new ResultPeaks();
			ResultPeaks peaksNotUsed = new ResultPeaks();
			Vector<Peak> listOfPeaks = new Vector<Peak>();
			
			
			if(MetFragBean.isStop())
				return;
			
			//stores the path to the pics from the explained peaks
			List<ResultPic> fragsPics = new ArrayList<ResultPic>();		
			
	        						
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
	        	return;
	        }
	        						        
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
			        
	        Fragmenter fragmenter = new Fragmenter((Vector<Peak>)spectrum.getPeakList().clone(), mzabs, mzppm, mode, true, this.metFragData.isMolFormulaRedundancyCheck(), false, false);     
	        List<IAtomContainer> l = null;
	        List<File> vec = new ArrayList<File>();
	        try
	        {
	        	//TODO!
	        	vec = fragmenter.generateFragmentsEfficient(molecule, true, Integer.parseInt(this.metFragData.getTreeDepth()), candidateID);
	        	l = Molfile.ReadfolderTemp(vec);
	        	//l = fragmenter.generateFragments(molecule, true, 2);
	        }
	        catch(OutOfMemoryError e)
	        {
	        	System.out.println("OUT OF MEMORY ERROR! " + candidateID);
	        	return;
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
			afp.assignFragmentPeak(fragments, cleanedPeakList, mzabs, mzppm, spectrum.getMode(), true);
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
				
				IMolecularFormula fragMolFormula = MolecularFormulaManipulator.getMolecularFormula(peakMolPair.getFragment());
				Double massDouble = MolecularFormulaTools.getMonoisotopicMass(fragMolFormula);
				
				massDouble = (double)Math.round(((mode * MolecularFormulaTools.getMonoisotopicMass("H1")) + massDouble)*10000)/10000;
				
				String modeString = "+";
				if(mode.equals(-1))
					modeString = "-";
				
				fragsPics.add(new ResultPic(sep + "FragmentPics" + sep + sessionString + sep + candidateID + sep + candidateID + "_" + countTemp, peakMolPair.getMatchedMass() + " [" + peakMolPair.getMolecularFormula() + "] <br />(" + PPMTool.getPPMWeb(peakMolPair.getMatchedMass(), peakMolPair.getPeak().getMass()) + " ppm)", MolecularFormulaManipulator.getHTML(fragMolFormula)));
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
	//		if(scoreMap.containsKey(hits.size()))
	//        {
	//        	ArrayList<String> tempList = scoreMap.get(hits.size());
	//        	tempList.add(candidateID);
	//        	scoreMap.put(hits.size(), tempList);
	//        }
	//        else
	//        {
	//        	ArrayList<String> temp = new ArrayList<String>();
	//        	temp.add(candidateID);
	//        	scoreMap.put(hits.size(), temp);
	//        }
			
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
//	        if (resultRowGroupedBeans != null) {
//	            resultRowGroupedBeans.clear();
//	        } else {
//	            resultRowGroupedBeans = new ArrayList(10);
//	        }
	        
	        //now generate the names and links according to the used database
	        String namesString = "";
	        String databaseLink = "";
	        
	        //local sdf file
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
	//				resultRows.add(new ResultRow(candidateID, namesString, fragsPics.get(0).getPath(), afp.getHits().size(), currentScore, fragsPics, massOrig, databaseLink, 
	//					peaksFound.getPeaksString(), peaksNotFound.getPeaksString(), peaksNotUsed.getPeaksString(), peaksFound.getIntensitiesString(), peaksNotFound.getIntensitiesString(), peaksNotUsed.getIntensitiesString()));
			else
				candidateToResult.put(candidateID, new ResultRow(candidateID, namesString, fragsPics.get(0).getPath(), afp.getHits().size(), currentScore, score.getFragmentBondEnergy(), score.getPenalty(), fragsPics, MolecularFormulaManipulator.getHTML(molFormula), massOrig, databaseLink, 
						peaksFound.getPeaksString(), peaksNotFound.getPeaksString(), peaksNotUsed.getPeaksString(), peaksFound.getIntensitiesString(), peaksNotFound.getIntensitiesString(), peaksNotUsed.getIntensitiesString()));
	//				resultRows.add(new ResultRow(candidateID, namesString, fragsPics.get(0).getPath(), afp.getHits().size(), currentScore, score.getFragmentBondEnergy(), score.getPenalty(), fragsPics, massOrig, databaseLink, 
	//						peaksFound.getPeaksString(), peaksNotFound.getPeaksString(), peaksNotUsed.getPeaksString(), peaksFound.getIntensitiesString(), peaksNotFound.getIntensitiesString(), peaksNotUsed.getIntensitiesString()));
	
			count[0] = count[0] + 1;
			
			}
		catch(CDKException e)
		{
			System.out.println("Error!" + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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

}
