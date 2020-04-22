/**
 * 
 */
package algorithms;

import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import andreas.*;
import costs.CostFunctionManager;
import costs.functions.CostFunction;
import costs.functions.EuclideanDistance;
import costs.functions.EuclideanDistanceScaling;
import costs.normalisation.GEDNormalisation1;
import costs.normalisation.GEDNormalisation2;
import costs.normalisation.GEDNormalisation3;
import costs.normalisation.NormalisationFunction;
import graph.Graph;
import graph.GraphSet;
import kaspar.GreedyMatching;
import kaspar.GreedyMatrixGenerator;
import util.*;
import util.treceval.SpottingPostProcessing;
import util.treceval.SpottingResult;
import util.treceval.TrecEval;
import xml.GraphParser;

/**
 * @author riesen
 * 
 */
public class GraphMatching {

	/**
	 * the sets of graphs to be matched 
	 */
	private GraphSet source, target;

	/**
	 * the resulting distance matrix D = (d_i,j), where d_i,j = d(g_i,g_j) 
	 * (distances between all graphs g_i from source and all graphs g_j from target) 
	 */
	private double[][] distanceMatrix;
	private double[][] normalisedDistanceMatrix;

	/**
	 * the source and target graph actually to be matched (temp ist for temporarily swappings)
	 */
	private Graph sourceGraph, targetGraph, temp;

	/**
	 * whether the edges of the graphs are undirected (=1) or directed (=0)
	 */
	private int undirected;

	/**
	 * progess-counter
	 */
	private int counter;

	/**
	 * log options:
	 * output the individual graphs
	 * output the cost matrix for bipartite graph matching
	 * output the matching between the nodes based on the cost matrix (considering the local substructures only)
	 * output the edit path between the graphs
	 */
	private int outputGraphs;
	private int outputCostMatrix;
	private int outputMatching;
	private int outputEditpath;

	/**
	 * the cost function to be applied
	 */
	private CostFunctionManager costFunctionManager;

	/**
	 * the normalisation function to be applied
	 */
	private NormalisationFunction normalisationFunction;

	/**
	 * number of rows and columns in the distance matrix
	 * (i.e. number of source and target graphs)
	 */
	private int r;
	private int c;

	/**
	 * computes an optimal bipartite matching of local graph structures
	 */
	private BipartiteMatching bipartiteMatching;
	private GreedyMatching greedyMatching;
	
	/**
	 * computes the approximated or exact graph edit distance 
	 */
	private EditDistance editDistance;
	
	// andreas
	
	private boolean oneMatch;
	private int oneMatchIdx1;
	private int oneMatchIdx2;
	private boolean oneMatchDisplay;
	private double oneMatchSize;
	
	// astar
	
	private String futureCostFunction;
	
	// ced
	
	private int contextRadius = 0;

	/**
	 * the matching procedure defined via GUI or properties file
	 * possible choices are 'Hungarian', 'VJ' (VolgenantJonker)
	 * 'AStar' (exact tree search) or 'Beam' (approximation based on tree-search)
	 */
	private String matching;
	
	/**
	 * the maximum number of open paths (used for beam-search)
	 */
	private int s;
	
	/**
	 * generates the cost matrix whereon the optimal bipartite matching can
	 * be computed
	 */
	private MatrixGenerator matrixGenerator;
	
	private GreedyMatrixGenerator greedyMatrixGenerator;
	
	private HEDMatrixGenerator hedMatrixGenerator;

	/**
	 * whether or not a similarity kernel is built upon the distance values:
	 * 0 = distance matrix is generated: D = (d_i,j), where d_i,j = d(g_i,g_j) 
	 * 1 = -(d_i,j)^2
	 * 2 = -d_i,j
	 * 3 = tanh(-d)
	 * 4 = exp(-d)
	 */
	private int simKernel;

	/**
	 * prints the results
	 */
	private ResultPrinter resultPrinter;

	// Ground truth and trecEval
	private TreeMap<String, String> wordList;
	private TrecEval trecEval;

	/**
	 * @param args
	 * properties[0] is an url to a properties file, where all parameters are defined
	 * (e.g. /Users/riesen/Documents/GraphMatching/properties/testproperties.prop)
	 */
	public static void main(String[] args) {
		try {
			new GraphMatching(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//andreas
	
	/**
	 * the matching procedure: set of graphs, print distance matrix
	 * @throws Exception
	 */
	public GraphMatching(String prop) throws Exception {
		// initialize the matching
		System.out.println("Initializing the matching according to the properties...");
		this.init(prop);
		// the cost matrix used for bipartite matchings
		double[][] costMatrix;
		// counts the progress
		this.counter = 0;
		
		// iterate through all pairs of graphs g_i x g_j from (source, target)
		System.out.println("Starting the matching...");
		System.out.println("Progress...");
		int numOfMatchings = this.source.size() * this.target.size();
		// distance value d
		double d = -1;
		double d_norm = -1;
		// swapped the graphs?
		boolean swapped = false;
		
		// prepare indexes for one or several matchings
		int[] idxs1 = new int[r]; for (int i=0; i < r; i++) { idxs1[i] = i; }
		int[] idxs2 = new int[c]; for (int j=0; j < c; j++) { idxs2[j] = j; }
		if (this.oneMatch) {
			idxs1 = new int[1]; idxs1[0] = this.oneMatchIdx1;
			idxs2 = new int[1]; idxs2[0] = this.oneMatchIdx2;
			numOfMatchings = 1;
		}
		
		// init editPath (for one matching)
		EditPath editPath = null;
		
		// main matching loop
		long start = System.currentTimeMillis();
		for (int i0 = 0; i0 < idxs1.length; i0++) {
			int i = idxs1[i0];
			sourceGraph = this.source.get(i);
			for (int j0 = 0; j0 < idxs2.length; j0++) {
				int j = idxs2[j0];
				swapped = false;
				targetGraph = this.target.get(j);
				this.counter++;
				if (counter % 100 == 0) {
					System.out.println("Matching "+counter+" of "+numOfMatchings);
				}
				
				// log the current graphs on the console
				if (this.outputGraphs == 1) {
					System.out.println("The Source Graph:");
					System.out.println(sourceGraph);
					System.out.println("\n\nThe Target Graph:");
					System.out.println(targetGraph);
				}
				// if both graphs are empty the distance is zero and no computations have to be carried out!
				if (this.sourceGraph.size()<1 && this.targetGraph.size()<1){
					d = 0;
				} else {
				// calculate the approximated or exact edit-distance using tree search algorithms 
				// AStar: number of open paths during search is unlimited (s=infty)
				// Beam: number of open paths during search is limited to s
//				if (this.matching.equals("AStar") || this.matching.equals("Beam")){
//					d = this.editDistance.getEditDistance(sourceGraph, targetGraph, costFunction, this.s);
//					if (this.oneMatch) {
//						editPath = this.editDistance.getEditPath();
//					}
//				}
//
//				/**
//				 * GED
//				 */
//
//				else if (this.matching.equals("GED")) {
//					if (this.sourceGraph.size()<this.targetGraph.size()){
//						this.swapGraphs();
//						swapped= true;
//					}
//					AStarGED aStarGED = new AStarGED(sourceGraph, targetGraph, costFunction, futureCostFunction);
//					d = aStarGED.getGraphEditDistance();
//					if (this.oneMatch) {
//						EditPathFactory epf = new EditPathFactory();
//						editPath = epf.fromAStarGED(sourceGraph, targetGraph, costFunction, aStarGED.getGraphEditDistanceMap());
//					}
//				}
//
//				/**
//				 * AED
//				 */
//
//				else if (this.matching.equals("AED")) {
//					if (this.sourceGraph.size()<this.targetGraph.size()){
//						this.swapGraphs();
//						swapped= true;
//					}
//					costMatrix = this.matrixGenerator.getMatrix(sourceGraph, targetGraph);
//					int[][] matching = this.bipartiteMatching.getMatching(costMatrix);
//					d = this.editDistance.getEditDistance(sourceGraph, targetGraph, matching, costFunction);
//					if (this.oneMatch) {
//						editPath = this.editDistance.getEditPath();
//					}
//				}
				
				/**
				 * HED
				 */				
				
				if (this.matching.equals("HED")) {
					if (this.sourceGraph.size()<this.targetGraph.size()){
						this.swapGraphs();
						swapped= true;
					}

					HED hed = new HED();
					d = hed.getHausdorffEditDistance(sourceGraph, targetGraph, costFunctionManager);
					editPath = null;

					double[] distances = this.editDistance.getNormalisedEditDistance(sourceGraph,targetGraph,d,normalisationFunction);

					d = distances[0];
					d_norm = distances[1];

//					if (this.oneMatch) {
//						HEDViz hedviz = new HEDViz();
//						d = hedviz.getHausdorffEditDistance(sourceGraph, targetGraph, costFunctionManager);
//						editPath = hedviz.getEditPath(sourceGraph, targetGraph);
//					} else {
//						HED hed = new HED();
//						d = hed.getHausdorffEditDistance(sourceGraph, targetGraph, costFunctionManager);
//						editPath = null;
//					}
				}

//				/**
//				 * HEDOneMatch
//				 * In contrast to multiple node assignment in HED
//				 * Each node allowed only to have
//				 */
//				else if (this.matching.equals("HEDOneMatch"))
//				{
//					if (this.sourceGraph.size() < this.targetGraph.size()){
//						this.swapGraphs();
//						swapped = true;
//					}
//					HEDBaseOneMatch hedScore = new HEDBaseOneMatch();
//					d = hedScore.getHausdorffEditDistance(sourceGraph, targetGraph, costFunction);
//					editPath = null;
//				}
//
//				/**
//				 * HEDOneMatchRatioScore
//				 * In contrast to multiple node assignment in HED
//				 * Each node allowed only to have
//				 */
//				else if (this.matching.equals("HEDOneMatchRatioScore"))
//				{
//					if (this.sourceGraph.size() < this.targetGraph.size()){
//						this.swapGraphs();
//						swapped = true;
//					}
//					HEDBaseOneMatch hedScore = new HEDBaseOneMatch(HEDBaseOneMatch.SCORE_TYPE.RATIO_SUB_ALL);
//					d = hedScore.getHausdorffEditDistance(sourceGraph, targetGraph, costFunction);
//					editPath = null;
//				}
//
//				/**
//				 * HEDOneMatchVarianceScore
//				 * In contrast to multiple node assignment in HED
//				 * Each node allowed only to have
//				 */
//				else if (this.matching.equals("HEDOneMatchVarianceScore"))
//				{
//					if (this.sourceGraph.size() < this.targetGraph.size()){
//						this.swapGraphs();
//						swapped = true;
//					}
//					HEDBaseOneMatch hedScore = new HEDBaseOneMatch(HEDBaseOneMatch.SCORE_TYPE.VARIANCE);
//					d = hedScore.getHausdorffEditDistance(sourceGraph, targetGraph, costFunction);
//					editPath = null;
//				}
//
//				/**
//				 * HEDOneMatchMeanVarianceScore
//				 * In contrast to multiple node assignment in HED
//				 * Each node allowed only to have
//				 */
//				else if (this.matching.equals("HEDOneMatchMeanVarianceScore"))
//				{
//					if (this.sourceGraph.size() < this.targetGraph.size()){
//						this.swapGraphs();
//						swapped = true;
//					}
//					HEDBase hedScore = new HEDBase(HEDBase.SCORE_TYPE.MEAN_VARIANCE);
//					d = hedScore.getHausdorffEditDistance(sourceGraph, targetGraph, costFunction);
//					editPath = null;
//				}
//
//				/**
//				 * HEDSelfCost
//				 */
//				else if (this.matching.equals("HEDSelfCost"))
//				{
//					if (this.sourceGraph.size() < this.targetGraph.size()){
//						this.swapGraphs();
//						swapped = true;
//					}
//
//					CostFunction cfEvaluated = new CostFunction(costFunction);
//                    EvaluateNodeEdgeCosts evaluateNodeEdgeCosts  =
//                            new EvaluateNodeEdgeCosts(
//                                    cfEvaluated,
//                                    sourceGraph,
//                                    targetGraph);
//                    evaluateNodeEdgeCosts.estimateCosts();
//
//
//
//					HED hedScore = new HED();
//					d = hedScore.getHausdorffEditDistance(sourceGraph, targetGraph, cfEvaluated);
//
//                    d = d /(    (evaluateNodeEdgeCosts.getG1EdgeNum() + evaluateNodeEdgeCosts.getG2EdgeNum()) *
//                                cfEvaluated.getEdgeCosts()
//                                +
//                                (evaluateNodeEdgeCosts.getG1NodeNum() + evaluateNodeEdgeCosts.getG2NodeNum()) *
//                                cfEvaluated.getNodeCosts() );
//
//					editPath = null;
//				}
//
//                /**
//                 * HEDSelfCostOneMatch
//                 */
//                else if (this.matching.equals("HEDSelfCostOneMatch"))
//                {
//                    if (this.sourceGraph.size() < this.targetGraph.size()){
//                        this.swapGraphs();
//                        swapped = true;
//                    }
//
//                    CostFunction cfEvaluated = new CostFunction(costFunction);
//                    EvaluateNodeEdgeCosts evaluateNodeEdgeCosts  =
//                            new EvaluateNodeEdgeCosts(
//                                    cfEvaluated,
//                                    sourceGraph,
//                                    targetGraph);
//                    evaluateNodeEdgeCosts.estimateCosts();
//
//                    HEDBaseOneMatch hedScore = new HEDBaseOneMatch();
//
//                    d = hedScore.getHausdorffEditDistance(sourceGraph, targetGraph, cfEvaluated);
//
//                    d = d /(    (evaluateNodeEdgeCosts.getG1EdgeNum() + evaluateNodeEdgeCosts.getG2EdgeNum()) *
//                            cfEvaluated.getEdgeCosts()
//                            +
//                            (evaluateNodeEdgeCosts.getG1NodeNum() + evaluateNodeEdgeCosts.getG2NodeNum()) *
//                                    cfEvaluated.getNodeCosts() );
//
//                    editPath = null;
//                }
//
//				/**
//				 * HEDSelfCostOneMatchRatioScore
//				 */
//				else if (this.matching.equals("HEDSelfCostOneMatchRatioScore"))
//				{
//					if (this.sourceGraph.size() < this.targetGraph.size()){
//						this.swapGraphs();
//						swapped = true;
//					}
//
//					CostFunction cfEvaluated = new CostFunction(costFunction);
//					EvaluateNodeEdgeCosts evaluateNodeEdgeCosts  =
//							new EvaluateNodeEdgeCosts(
//									cfEvaluated,
//									sourceGraph,
//									targetGraph);
//					evaluateNodeEdgeCosts.estimateCosts();
//
//					HEDBaseOneMatch hedScore = new HEDBaseOneMatch(HEDBaseOneMatch.SCORE_TYPE.RATIO_SUB_ALL);
//
//					d = hedScore.getHausdorffEditDistance(sourceGraph, targetGraph, cfEvaluated);
//
//					d = d /(    (evaluateNodeEdgeCosts.getG1EdgeNum() + evaluateNodeEdgeCosts.getG2EdgeNum()) *
//							cfEvaluated.getEdgeCosts()
//							+
//							(evaluateNodeEdgeCosts.getG1NodeNum() + evaluateNodeEdgeCosts.getG2NodeNum()) *
//									cfEvaluated.getNodeCosts() );
//
//					editPath = null;
//				}
//
//				/**
//				 * HEDSelfCostOneMatchVarianceScore
//				 */
//				else if (this.matching.equals("HEDSelfCostOneMatchVarianceScore"))
//				{
//					if (this.sourceGraph.size() < this.targetGraph.size()){
//						this.swapGraphs();
//						swapped = true;
//					}
//
//					CostFunction cfEvaluated = new CostFunction(costFunction);
//					EvaluateNodeEdgeCosts evaluateNodeEdgeCosts  =
//							new EvaluateNodeEdgeCosts(
//									cfEvaluated,
//									sourceGraph,
//									targetGraph);
//					evaluateNodeEdgeCosts.estimateCosts();
//
////                    System.out.println("Node Cost:" + cfEvaluated.getNodeCosts() + " Edge Cost:" + cfEvaluated.getEdgeCosts());
//
//					HEDBaseOneMatch hedScore = new HEDBaseOneMatch(HEDBaseOneMatch.SCORE_TYPE.VARIANCE);
//
//					d = hedScore.getHausdorffEditDistance(sourceGraph, targetGraph, cfEvaluated);
//
////					d = d /(    (evaluateNodeEdgeCosts.getG1EdgeNum() + evaluateNodeEdgeCosts.getG2EdgeNum()) *
////							cfEvaluated.getEdgeCosts()
////							+
////							(evaluateNodeEdgeCosts.getG1NodeNum() + evaluateNodeEdgeCosts.getG2NodeNum()) *
////									cfEvaluated.getNodeCosts() );
//
//					editPath = null;
//				}
//
//
//				/**
//				 * HEDContext
//				 */
//
//				else if (this.matching.equals("HEDContext")) {
//					if (this.sourceGraph.size()<this.targetGraph.size()){
//						this.swapGraphs();
//						swapped= true;
//					}
//					HEDContext ced = new HEDContext(sourceGraph, targetGraph, costFunction, contextRadius);
//					d = ced.getEditDistance();
//					if (this.oneMatch) {
//						editPath = ced.getEditPath();
//					}
//				}
//
//				/**
//				 * BP2
//				 */
//
//				else if (this.matching.equals("BP2")) {
//					if (this.sourceGraph.size()<this.targetGraph.size()){
//						this.swapGraphs();
//						swapped= true;
//					}
//					BP2 hed = new BP2();
//					d = hed.getEditDistance(sourceGraph, targetGraph, costFunction);
//					if (this.oneMatch) {
//						editPath = hed.getEditPath(sourceGraph, targetGraph);
//					}
//				}
				
				/**
				 * Other
				 */
				
//				else { // approximation of graph edit distances via bipartite matching
//					// in order to get determinant edit costs between two graphs
//					if (this.sourceGraph.size()<this.targetGraph.size()){
//						this.swapGraphs();
//						swapped= true;
//					}
//					// generate the cost-matrix between the local substructures of the source and target graphs
//					costMatrix = this.matrixGenerator.getMatrix(sourceGraph, targetGraph);
//					// compute the matching using Hungarian or VolgenantJonker (defined in String matching)
//					int[][] matching = this.bipartiteMatching.getMatching(costMatrix);
//					// calculate the approximated edit-distance according to the bipartite matching 
//					d = this.editDistance.getEditDistance(sourceGraph, targetGraph, matching, costFunction);
//					if (this.oneMatch) {
//						editPath = this.editDistance.getEditPath();
//					}
//				}
				}
				// whether distances or similarities are computed
				if (this.simKernel < 1){
					this.distanceMatrix[i][j] = d;
					this.normalisedDistanceMatrix[i][j] = d_norm;

				} else {
					switch (this.simKernel){
					case 1:
						this.distanceMatrix[i][j] = -Math.pow(d,2.0);break;
					case 2:
						this.distanceMatrix[i][j] = -d;break;
					case 3:
						this.distanceMatrix[i][j] = Math.tanh(-d);break;
					case 4:
						this.distanceMatrix[i][j] = Math.exp(-d);break;
                    }			
				}
				if (swapped){
					this.swapGraphs();
				}
			}
		}
		long time = System.currentTimeMillis() - start;
		
//		System.out.println("time: " + AStarGED.timeNano);
//		System.out.println("open: " + AStarGED.numOpen);
//		System.out.println("matchings: " + AStarGED.numMatchings);
//		System.out.println("failures: " + AStarGED.numFailures);
//
//		// printing the distances or similarities
//		System.out.println("Printing the results...");
//		if (this.oneMatch) {
//			if (editPath != null) {
//				this.resultPrinter.printEditPath(prop, this.sourceGraph, this.targetGraph, editPath, 600, 600, this.oneMatchDisplay, this.oneMatchSize);
//			}
//			System.out.println("=> distance: " + this.distanceMatrix[this.oneMatchIdx1][this.oneMatchIdx2]);
//		} else {
//			this.resultPrinter.printResult(this.distanceMatrix, this.source, this.target, prop, time);
//		}

		// Create KWS results
		ArrayList<SpottingResult> spottingResults = new ArrayList<>();

		for (int i = 0; i < source.size(); i++) {

			Graph sourceGraph = source.get(i);

			String keywordID = sourceGraph.getGraphID();
			String keywordClass = this.wordList.get(sourceGraph.getGraphID());

			for (int j = 0; j < target.size(); j++) {

				Graph targetGraph = target.get(j);

				String wordID = targetGraph.getGraphID();
				String wordClass = this.wordList.get(targetGraph.getGraphID());

				double normGED = this.normalisedDistanceMatrix[i][j];

				SpottingResult spottingResult = new SpottingResult(keywordID, keywordClass, wordID, wordClass, normGED);
				spottingResults.add(spottingResult);
			}
		}

		// Reduce number of spotting results (i.e. find minimal distance per query)
		SpottingPostProcessing spottingPostProcessing = new SpottingPostProcessing();
		ArrayList<SpottingResult> reducedSpottingResults = spottingPostProcessing.postProcess(spottingResults);

		// Evaluate and export spotting results
		trecEval.exportSpottingResults(reducedSpottingResults);
	}

	/**
	 * swap the source and target graph
	 */
	private void swapGraphs() {
		this.temp = this.sourceGraph;
		this.sourceGraph = this.targetGraph;
		this.targetGraph = this.temp;
	}

	/**
	 * @return the progress of the matching procedure
	 */
	public int getCounter() {
		return counter;
	}

	/**
	 * initializes the whole graph edit distance framework according to the properties files 
	 * @param prop
	 * @throws Exception
	 */
	private void init(String prop) throws Exception {
		// load the properties file
		Properties properties = new Properties();
		FileInputStream fis = new FileInputStream(prop);
		properties.load(fis);
		fis.close();
		
		// define result folder
		String resultFolder = properties.getProperty("result");
		System.out.println("result dir:" + resultFolder);
		
		// the node and edge costs, the relative weighting factor alpha
		double nodeCost = Double.parseDouble(properties.getProperty("node"));
		double edgeCost = Double.parseDouble(properties.getProperty("edge"));
		double alpha = Double.parseDouble(properties.getProperty("alpha"));
		
		// the node and edge attributes (the names, the individual cost functions, the weighting factors)
		int numOfNodeAttr = Integer.parseInt(properties
				.getProperty("numOfNodeAttr"));
		int numOfEdgeAttr = Integer.parseInt(properties
				.getProperty("numOfEdgeAttr"));
		String[] nodeAttributes = new String[numOfNodeAttr];
		String[] edgeAttributes = new String[numOfEdgeAttr];
		String[] nodeCostTypes = new String[numOfNodeAttr];
		String[] edgeCostTypes = new String[numOfEdgeAttr];
		double[] edgeCostMu = new double[numOfEdgeAttr];
		double[] edgeCostNu = new double[numOfEdgeAttr];
		double[] nodeAttrImportance = new double[numOfNodeAttr];
		double[] edgeAttrImportance = new double[numOfEdgeAttr];
		double[] nodeCostMu = new double[numOfNodeAttr];
		double[] nodeCostNu = new double[numOfNodeAttr];
		for (int i = 0; i < numOfNodeAttr; i++) {
			nodeAttributes[i] = properties.getProperty("nodeAttr" + i);
			nodeCostTypes[i] = properties.getProperty("nodeCostType" + i);
//			if (nodeCostTypes[i].equals("discrete")){
//				nodeCostMu[i]=Double.parseDouble(properties.getProperty("nodeCostMu" + i));
//				nodeCostNu[i]=Double.parseDouble(properties.getProperty("nodeCostNu" + i));
//			}
			nodeAttrImportance[i] = Double.parseDouble(properties
					.getProperty("nodeAttr" + i + "Importance"));
		}
		for (int i = 0; i < numOfEdgeAttr; i++) {
			edgeAttributes[i] = properties.getProperty("edgeAttr" + i);
			edgeCostTypes[i] = properties.getProperty("edgeCostType" + i);
			edgeAttrImportance[i] = Double.parseDouble(properties
					.getProperty("edgeAttr" + i + "Importance"));
//			if (edgeCostTypes[i].equals("discrete")){
//				edgeCostMu[i]=Double.parseDouble(properties.getProperty("edgeCostMu" + i));
//				edgeCostNu[i]=Double.parseDouble(properties.getProperty("edgeCostNu" + i));
//			}
		}
		
		
//		// whether or not the costs are "p-rooted"
//		double squareRootNodeCosts = Double.parseDouble(properties
//				.getProperty("pNode"));
//		double squareRootEdgeCosts = Double.parseDouble(properties
//				.getProperty("pEdge"));
//
//		// whether costs are multiplied or summed
//		int multiplyNodeCosts = Integer.parseInt(properties
//				.getProperty("multiplyNodeCosts"));
//		int multiplyEdgeCosts = Integer.parseInt(properties
//				.getProperty("multiplyEdgeCosts"));
//
//		// what is logged on the console (graphs, cost-matrix, matching, edit path)
//		this.outputGraphs = Integer.parseInt(properties
//				.getProperty("outputGraphs"));
//		this.outputCostMatrix = Integer.parseInt(properties
//				.getProperty("outputCostMatrix"));
//		this.outputMatching = Integer.parseInt(properties
//				.getProperty("outputMatching"));
//		this.outputEditpath = Integer.parseInt(properties
//				.getProperty("outputEditpath"));
		
		// whether the edges of the graphs are directed or undirected
		this.undirected = Integer
				.parseInt(properties.getProperty("undirected"));
		
		// the graph matching paradigm actually employed 		
		this.matching =  properties.getProperty("matching");
		
		// maximum number of open paths is limited to s in beam-search
		if (this.matching.equals("Beam")){
			this.s =  Integer.parseInt(properties.getProperty("s"));
		} else {
			this.s = Integer.MAX_VALUE; // AStar
		}
		
		// initialize the cost function according to properties
//		this.costFunction = new CostFunction(node, edge, alpha, nodeAttributes,
//				nodeCostTypes, nodeAttrImportance, edgeAttributes,
//				edgeCostTypes, edgeAttrImportance, squareRootNodeCosts,
//				multiplyNodeCosts, squareRootEdgeCosts, multiplyEdgeCosts, nodeCostMu, nodeCostNu);


		// Create and initialise new cost functions
		CostFunction costFunction;
		String costType = "Euclidean";
		//String costType = properties.getProperty("costType");

		if (costType.equals("Euclidean")) {
			costFunction = new EuclideanDistance();
		} else if (costType.equals("Euclidean Scaling")) {
			costFunction = new EuclideanDistanceScaling();
		} else {
			costFunction = new EuclideanDistance();
		}

		this.costFunctionManager = new CostFunctionManager(
				costFunction,
				alpha,
				nodeCost,
				edgeCost,
				nodeAttributes,
				edgeAttributes,
				nodeAttrImportance,
				edgeAttrImportance
		);

		// Create and initialise GED normalisation functions
		String normalisationFunction = properties.getProperty("normalisationFunction");

		if (normalisationFunction != null) {
			if (normalisationFunction.equals("N1")) {
				this.normalisationFunction = new GEDNormalisation1(nodeCost, edgeCost);
			} else if (normalisationFunction.equals("N2")) {
				this.normalisationFunction = new GEDNormalisation2(nodeCost, edgeCost);
			} else if (normalisationFunction.equals("N3")) {
				this.normalisationFunction = new GEDNormalisation3();
			}
		} else {
			this.normalisationFunction = new GEDNormalisation1(nodeCost, edgeCost);
		}
		
		// the matrixGenerator generates the cost-matrices according to the costfunction
		this.matrixGenerator = new MatrixGenerator(this.costFunctionManager, this.outputCostMatrix);
		
		this.greedyMatrixGenerator = new GreedyMatrixGenerator(this.costFunctionManager, this.outputCostMatrix, 0);
		this.greedyMatrixGenerator.setAdj("best");
		
		this.hedMatrixGenerator = new HEDMatrixGenerator();
		
		// bipartite matching procedure (Hungarian or VolgenantJonker)
		this.bipartiteMatching = new BipartiteMatching(this.matching, this.outputMatching);
		
		this.greedyMatching = new GreedyMatching();
		
		// editDistance computes either the approximated edit-distance according to the bipartite  
		// or computes the exact edit distance
		this.editDistance = new EditDistance(this.undirected, this.outputEditpath);
		
		// the resultPrinter prints the properties and the distances found		
		this.resultPrinter = new ResultPrinter(resultFolder, properties);
		
		// whether or not a similarity is derived from the distances 
		this.simKernel=Integer.parseInt(properties.getProperty("simKernel"));
		
		// load the source and target set of graphs
		System.out.println("Load the source and target graph sets...");

		GraphParser graphParser = new GraphParser();
		Path gxlPath = Paths.get(properties.getProperty("path"));

		Path cxlSourcePath = Paths.get(properties.getProperty("source"));
		this.source = graphParser.parseCXL(cxlSourcePath, gxlPath);

		Path cxlTargetPath = Paths.get(properties.getProperty("target"));
		this.target = graphParser.parseCXL(cxlTargetPath, gxlPath);

		// create a distance matrix to store the resulting dissimilarities
		this.r = this.source.size();
		this.c = this.target.size();
		this.distanceMatrix             = new double[this.r][this.c];
		this.normalisedDistanceMatrix   = new double[this.r][this.c];
		//this.assignmentCostMatrix       = new double[this.r][this.c];
				
//		// check if only one match is required
//		this.oneMatch = Boolean.parseBoolean(properties.getProperty("oneMatch"));
//		if (this.oneMatch) {
//			this.oneMatchIdx1 = Integer.parseInt(properties.getProperty("oneMatchIdx1"));
//			this.oneMatchIdx2 = Integer.parseInt(properties.getProperty("oneMatchIdx2"));
//			this.oneMatchDisplay = Boolean.parseBoolean(properties.getProperty("oneMatchDisplay"));
//			this.oneMatchSize = 8;
//			if (properties.getProperty("oneMatchSize") != null) {
//				this.oneMatchSize = Double.parseDouble(properties.getProperty("oneMatchSize"));
//			}
//		}
//
//		// astar
//		this.futureCostFunction = properties.getProperty("futureCostFunction");
//
//		// ced
//		if (properties.getProperty("contextRadius") != null) {
//			this.contextRadius = Integer.parseInt(properties.getProperty("contextRadius"));
//		}

		// load the ground truth and keywords
		Path groundTruthPath = Paths.get(properties.getProperty("groundtruth"));

		if(Files.isRegularFile(groundTruthPath)) {

			List<String> wordIDList = Files.readAllLines(groundTruthPath, Charset.defaultCharset());

			this.wordList = new TreeMap<>();
			this.wordList.putAll(createWordList(wordIDList));
		} else {
			this.wordList = null;
		}

		// Create trecEval export possibility
		Path trecEvalFile = Paths.get(properties.getProperty("treceval"));
		Path resultPath = Paths.get(properties.getProperty("result"));

		resultPath.toFile().mkdirs();

		if(Files.isRegularFile(trecEvalFile) && Files.isDirectory(resultPath)){
			this.trecEval = new TrecEval(resultPath,trecEvalFile,-1);
		} else {
			this.trecEval = null;
		}
		
	}

	private TreeMap<String, String> createWordList(List<String> wordIDList){

		TreeMap<String, String> wordList = new TreeMap<>();

		for(String wordID : wordIDList){

			// Split by Regex
			String[]split   = wordID.split(("\\ "));

			if(split.length == 2){

				String id       = split[0];
				String word     = split[1];

				wordList.put(id, word);
			}
		}

		return wordList;
	}
}
