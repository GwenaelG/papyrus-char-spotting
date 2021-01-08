

package algorithms;

import andreas.EditPath;
import andreas.HED;
import andreas.HEDMatrixGenerator;
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
import gwenael.*;
import kaspar.GreedyMatching;
import kaspar.GreedyMatrixGenerator;
import org.opencv.imgcodecs.Imgcodecs;
import util.EditDistance;
import util.MatrixGenerator;
import util.ResultPrinter;
import util.treceval.SpottingPostProcessing;
import util.treceval.SpottingResult;
import util.treceval.TrecEval;
import xml.GraphParser;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;

/**
 * @author riesen
 * modified Gwenael
 *
 */
public class GraphMatchingSegFreeDummy {

//	static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }


	/**
	 * the sets of graphs to be matched
	 */
	private GraphSet source, target;

	/**
	 * the resulting distances, corresponding to the windows AL
	 */
	private ThreeDimAL<Double> minLineDistances;

	private FourDimAL<Integer> lineBestWindow;

	/**
	 * the source and target graph actually to be matched (temp ist for temporarily swappings)
	 */
	private Graph sourceGraph, targetGraph, targetPage, temp;

	/**
	 * whether the edges of the graphs are undirected (=1) or directed (=0)
	 */
	private int undirected;

	/**
	 * progress-counter
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

	// size of windows relative to source char
	private double[] windowSizes;

	//
	private TwoDimAL<Integer> candWindows;

	private	TwoDimAL<Integer> windowsCount;

	//
	private ThreeDimAL<Double> windowsDistMeanStd;

	// groundtruth
	private ArrayList<GroundtruthPage> groundtruthPages;

	// images for dist display
	private ArrayList<BufferedImage> targetImages;
	private ArrayList<BufferedImage> sourceImages;

	// size of circles
	private static final int RADIUS = 5;

	// folder for saving images
	private Path hotmapVisFolder;
	private Path charVisFolder;

	// normalized threshold (for normalized distances)
	private double[] thresholds;

	private TwoDimAL<Boolean> underThresholdMat;

	private FourDimAL<Integer> truePositives;
	private FourDimAL<Integer> trueNegatives;
	private FourDimAL<Integer> falsePositives;
	private FourDimAL<Integer> falseNegatives;

	private int stepX;
	private int stepY;
	private TwoDimAL<Integer> gridSizes;

	private ThreeDimAL<Double> minDists;

	private double IoU_Ratio;

	private FourDimAL<Integer> windowNodeCount;
	private double nodeRatio;


	/**
	 * @param args
	 * properties[0] is an url to a properties file, where all parameters are defined
	 * (e.g. /Users/riesen/Documents/GraphMatching/properties/testproperties.prop)
	 */
	public static void main(String[] args) {
		try {
			new GraphMatchingSegFreeDummy(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//andreas

	/**
	 * the matching procedure: set of graphs, print distance matrix
	 * @throws Exception
	 */
	public GraphMatchingSegFreeDummy(String prop) throws Exception {

		// initialize the matching
		long initTimeStart = System.currentTimeMillis();
		System.out.println("Initializing the matching according to the properties...");
		this.init(prop);
		long initTime = System.currentTimeMillis() - initTimeStart;
		long initTimeMin = initTime / 60000;
		long initTimeSec = (initTime / 1000) % 60;
		System.out.println("Time for init(): "+ initTimeMin+" m "+initTimeSec+" s.");


		this.resultPrinter.printInfos(source, target, prop, initTime, 0);


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

	private double getArea(Rectangle rect) {
		return rect.getWidth()*rect.getHeight();
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
//			nodeCostTypes[i] = properties.getProperty("nodeCostType" + i);
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
			if (edgeCostTypes[i].equals("discrete")){
				edgeCostMu[i]=Double.parseDouble(properties.getProperty("edgeCostMu" + i));
				edgeCostNu[i]=Double.parseDouble(properties.getProperty("edgeCostNu" + i));
			}
		}

		// whether or not the costs are "p-rooted"
		double squareRootNodeCosts = Double.parseDouble(properties
				.getProperty("pNode"));
		double squareRootEdgeCosts = Double.parseDouble(properties
				.getProperty("pEdge"));

		// whether costs are multiplied or summed
		int multiplyNodeCosts = Integer.parseInt(properties
				.getProperty("multiplyNodeCosts"));
		int multiplyEdgeCosts = Integer.parseInt(properties
				.getProperty("multiplyEdgeCosts"));

//		//changed Gw
//		// what is logged on the console (graphs, cost-matrix, matching, edit path)
		this.outputGraphs = Integer.parseInt(properties
				.getProperty("outputGraphs"));
		this.outputCostMatrix = Integer.parseInt(properties
				.getProperty("outputCostMatrix"));
		this.outputMatching = Integer.parseInt(properties
				.getProperty("outputMatching"));
		this.outputEditpath = Integer.parseInt(properties
				.getProperty("outputEditPath"));

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

		// Create and initialise new cost functions
		CostFunction costFunction;

		String costType = properties.getProperty("costType");

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
		Path gxlSourcePath = Paths.get(properties.getProperty("sourcePath"));
		Path gxlTargetPath = Paths.get(properties.getProperty("targetPath"));

		Path cxlSourcePath = Paths.get(properties.getProperty("sourceFile"));
		this.source = graphParser.parseCXL(cxlSourcePath, gxlSourcePath);

		Path cxlTargetPath = Paths.get(properties.getProperty("targetFile"));
		this.target = graphParser.parseCXL(cxlTargetPath, gxlTargetPath);


//		// check if only one match is required
		this.oneMatch = Boolean.parseBoolean(properties.getProperty("oneMatch"));
		if (this.oneMatch) {
			this.oneMatchIdx1 = Integer.parseInt(properties.getProperty("oneMatchIdx1"));
			this.oneMatchIdx2 = Integer.parseInt(properties.getProperty("oneMatchIdx2"));
			this.oneMatchDisplay = Boolean.parseBoolean(properties.getProperty("oneMatchDisplay"));
			this.oneMatchSize = 8;
			if (properties.getProperty("oneMatchSize") != null) {
				this.oneMatchSize = Double.parseDouble(properties.getProperty("oneMatchSize"));
			}
		}

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

		// window sizes for subgraph matching
		int numOfWindowSizes = Integer.parseInt(properties.getProperty("numOfWindowSizes"));
		this.windowSizes = new double[numOfWindowSizes];
		for (int i = 0; i < numOfWindowSizes; i++){
			windowSizes[i] = Double.parseDouble(properties.getProperty("windowSize" + i));
		}

		this.candWindows = new TwoDimAL<>();

		this.windowsDistMeanStd = new ThreeDimAL<>();
		for (int i = 0; i < source.size(); i++) {
			for (int j = 0; j < target.size(); j++) {
				windowsDistMeanStd.set(i,j,0, 0.);
				windowsDistMeanStd.set(i,j,1, 0.);
			}
		}

		this.windowsCount = new TwoDimAL<>();
		for (int i = 0; i < source.size(); i++) {
			for (int j = 0; j < target.size(); j++) {
				windowsCount.set(i,j,0);
			}
		}

		//extract line groundtruth from files
		this.groundtruthPages = new ArrayList<>();
		String groundtruthFilesFolder = properties.getProperty("groundtruthPagesFolder");
		for (int j = 0; j < target.size(); j ++) {
			String pageName = target.get(j).getGraphID();
			Path groundtruthFilePath = Paths.get(groundtruthFilesFolder, pageName+".txt");
			GroundtruthPage GTPage = new GroundtruthPage(pageName);
			GTPage.extractGroundtruthLines(groundtruthFilePath);
			groundtruthPages.add(GTPage);
		}

		this.minLineDistances = new ThreeDimAL<>();
		for (int j = 0; j < target.size(); j++) {
			GroundtruthPage gtPage = groundtruthPages.get(j);
			for (int m = 0; m < gtPage.getLines().size(); m++) {
				for (int i = 0; i < source.size(); i++) {
					minLineDistances.set(i,j,m,Double.POSITIVE_INFINITY);
				}
			}
		}

		this.lineBestWindow = new FourDimAL<>();

		this.hotmapVisFolder = Paths.get(properties.getProperty("editDistVis"));
		this.charVisFolder = Paths.get(properties.getProperty("charVisFolder"));

		// open original binarized images as matrices
		Imgcodecs imgcodecs = new Imgcodecs();
		Path targetImagesPath = Paths.get(properties.getProperty("targetImagesPath"));
		this.targetImages = new ArrayList<>();
		for (int j = 0; j < target.size(); j++) {
			String imagePath = targetImagesPath + "\\" + target.get(j).getFileName().substring(0, target.get(j).getFileName().length() - 4) + "_r.png";
			BufferedImage oldImg = ImageIO.read(new File(imagePath));
			targetImages.add(oldImg);
		}
		Path sourceImagesPath = Paths.get(properties.getProperty("sourceImagesPath"));
		this.sourceImages = new ArrayList<>();
		for (int j = 0; j < source.size(); j++) {
			String imagePath = sourceImagesPath + "\\" + source.get(j).getFileName().substring(0, source.get(j).getFileName().length() - 4) + "_tt.png";
			BufferedImage oldImg = ImageIO.read(new File(imagePath));
			sourceImages.add(oldImg);
		}

		int numOfThresholdVal = Integer.parseInt(properties.getProperty("numOfThresholdVal"));
		thresholds = new double[numOfThresholdVal];
		for (int i = 0; i < numOfThresholdVal; i++) {
			thresholds[i] = Double.parseDouble(properties.getProperty("threshold"+i));
		}
		this.underThresholdMat = new TwoDimAL<>();
		this.truePositives = new FourDimAL<>();
		this.trueNegatives = new FourDimAL<>();
		this.falsePositives = new FourDimAL<>();
		this.falseNegatives = new FourDimAL<>();

		for (int i = 0; i < source.size(); i++) {
			for (int j = 0; j < target.size(); j++) {
				//for (int k = 0; k < target.get(j).size(); k++){
				for (int k = 0; k < numOfWindowSizes; k++) {
					for (int l = 0; l < thresholds.length; l++) {
						truePositives.set(i, j, k, l,0);
						falseNegatives.set(i, j, k, l, 0);
						falsePositives.set(i, j, k, l, 0);
						trueNegatives.set(i, j, k, l,0);
					}
				}
			}
		}

		this.stepX = Integer.parseInt(properties.getProperty("stepX"));
		this.stepY = Integer.parseInt(properties.getProperty("stepY"));
		this.gridSizes = new TwoDimAL<>();

		this.minDists = new ThreeDimAL<>();

		this.nodeRatio = Double.parseDouble(properties.getProperty("nodeRatio"));
		this.windowNodeCount = new FourDimAL<>();

		this.IoU_Ratio = Double.parseDouble(properties.getProperty("iouRatio"));

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
