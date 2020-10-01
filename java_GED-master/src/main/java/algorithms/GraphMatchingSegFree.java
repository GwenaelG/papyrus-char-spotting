/**
 * 
 */
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
import graph.Node;
import gwenael.FourDimAL;
import gwenael.ThreeDimAL;
import kaspar.GreedyMatching;
import kaspar.GreedyMatrixGenerator;
import org.opencv.core.Core;
import org.opencv.imgcodecs.Imgcodecs;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import util.EditDistance;
import util.MatrixGenerator;
import util.ResultPrinter;
import util.treceval.TrecEval;
import gwenael.BoundingBox;
import xml.GraphParser;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.net.PortUnreachableException;
import java.nio.Buffer;
import java.nio.DoubleBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/**
 * @author riesen
 * modified Gwenael
 * 
 */
public class GraphMatchingSegFree {

//	static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }


	/**
	 * the sets of graphs to be matched
	 */
	private GraphSet source, target;

	/**
	 * the resulting distance matrix D = (d_i,j,k,l), where d_i,j,k,l = d(g_i,g_j,k,l)
	 * (distances between all graphs g_i from source and all window graphs from target graph g_j, centered on node k
	 * with window size l)
	 */
	private FourDimAL<Double> distanceMatrix;

	// used for resultPrinter only (?)
	// private FourDimAL<Double> normalisedDistanceMatrix;

	// used for distance-to-color conversion
	private FourDimAL<Double> normColorDistMatrix;

	/**
	 * the source and target graph actually to be matched (temp ist for temporarily swappings)
	 */
	private Graph sourceGraph, targetGraph, targetPage, temp;

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

	// groundtruth
	private ArrayList<BoundingBox> boundingBoxesGT;

	// images for dist display
	private ArrayList<BufferedImage> targetImages;

	// size of circles
	private static final int RADIUS = 5;

	// folder for saving images
	private Path visualizationFolder;

	// use sigmoid (or linear distance-to-color conversion )
	private boolean sigmoid;

	// steepness values for sigmoid function
	private double[] steepness;



	/**
	 * @param args
	 * properties[0] is an url to a properties file, where all parameters are defined
	 * (e.g. /Users/riesen/Documents/GraphMatching/properties/testproperties.prop)
	 */
	public static void main(String[] args) {
		try {
			new GraphMatchingSegFree(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//andreas

	/**
	 * the matching procedure: set of graphs, print distance matrix
	 * @throws Exception
	 */
	public GraphMatchingSegFree(String prop) throws Exception {

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
		int numOfMatchings = this.source.size() * windowSizes.length;
		for (Graph graph : target){
			numOfMatchings *= graph.size();
		}
		// distance value d
		double d = -1;
		double d_norm = -1;
		ArrayList<Double> dMax = new ArrayList<>();
		Double dMaxGlobal = 0.;

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

			// get size of source graph
			// max taken before normalization
			double xMaxSource = sourceGraph.getDouble("x_max");
			double yMaxSource = sourceGraph.getDouble("y_max");

			for (int j0 = 0; j0 < idxs2.length; j0++) {
				int j = idxs2[j0];

				targetPage = this.target.get(j);

				double xMean = targetPage.getDouble("x_mean");
				double yMean = targetPage.getDouble("y_mean");
				double xStDev = targetPage.getDouble("x_std");
				double yStDev = targetPage.getDouble("y_std");

				// window dimensions
				double[][] windowMaxDistances = new double[windowSizes.length][2];
				for (int k = 0; k < windowSizes.length; k++){
					windowMaxDistances[k] = new double[] {windowSizes[k] * (xMaxSource) / xStDev,
							windowSizes[k] * (yMaxSource) / yStDev};
					dMax.add((double) 0);
				}

				// center windows on each node of the page graph
				for (int k = 0; k < targetPage.size(); k++){

					Node centerNode = targetPage.get(k);

					ArrayList<Graph> windows = targetPage.extractWindows(centerNode, windowMaxDistances);

					for (int l = 0; l < windowSizes.length; l++) {

						swapped = false;

						targetGraph = windows.get(l);

						this.counter++;
						if (counter % 100 == 0) {
							System.out.println("Matching " + counter + " of " + numOfMatchings);
						}

						// log the current graphs on the console
						if (this.outputGraphs == 1) {
							System.out.println("The Source Graph:");
							System.out.println(sourceGraph);
							System.out.println("\n\nThe Target Graph:");
							System.out.println(targetGraph);
						}
						// if both graphs are empty the distance is zero and no computations have to be carried out!
						if (this.sourceGraph.size() < 1 && this.targetGraph.size() < 1) {
							d = 0;
						} else {

							if (this.matching.equals("HED")) {
								if (this.sourceGraph.size() < this.targetGraph.size()) {
									this.swapGraphs();
									swapped = true;
								}

								HED hed = new HED();
								d = hed.getHausdorffEditDistance(sourceGraph, targetGraph, costFunctionManager);
//								System.out.println(sourceGraph.getGraphID()+" "+targetGraph.getGraphID()+" "+d);
								editPath = null;

								double[] distances = this.editDistance.getNormalisedEditDistance(sourceGraph, targetGraph, d, normalisationFunction);

								d = distances[0];
								d_norm = distances[1];
							}
						}

						dMaxGlobal = Math.max(dMaxGlobal, d);


						// whether distances or similarities are computed
						if (this.simKernel < 1) {
							this.distanceMatrix.set(i, j, k, l, d);
							this.normColorDistMatrix.set(i, j, k, l, d_norm);

						} else {
							switch (this.simKernel) {
								case 1:
									this.distanceMatrix.set(i, j, k, l, -Math.pow(d, 2.0));
									break;
								case 2:
									this.distanceMatrix.set(i, j, k, l, -d);
									break;
								case 3:
									this.distanceMatrix.set(i, j, k, l, Math.tanh(-d));
									break;
								case 4:
									this.distanceMatrix.set(i, j, k, l, Math.exp(-d));
									break;
							}
						}
						if (swapped) {
							this.swapGraphs();
						}
					}
				}
			}
		}



//		TODO: update resultPrinter
//		long time = System.currentTimeMillis() - start;
//		this.resultPrinter.printResult(this.distanceMatrix, this.source, this.target, prop, time);

		String convMethod = new String();

		// display edit distance between target node window and source char
		for (int i = 0; i < source.size(); i++) {

			for (int j = 0; j < target.size(); j++) {

				targetPage = target.get(j);
				double xMean = targetPage.getDouble("x_mean");
				double yMean = targetPage.getDouble("y_mean");
				double xStDev = targetPage.getDouble("x_std");
				double yStDev = targetPage.getDouble("y_std");

				// normalize distances for color conversion
				double distMean = 0;
				double distSumSqDiff = 0;

				for (int k = 0; k < targetPage.size(); k++) {
					for (int l = 0; l < windowSizes.length; l++) {
						double dist = distanceMatrix.get(i, j, k, l);
						distMean += dist;
					}
				}
				distMean /= targetPage.size();

				for (int k = 0; k < targetPage.size(); k++) {
					for (int l = 0; l < windowSizes.length; l++) {
						double dist = distanceMatrix.get(i, j, k, l);
						distSumSqDiff += Math.pow(dist - distMean,2);
					}
				}

				double distStDev = Math.sqrt(distSumSqDiff / (targetPage.size()*windowSizes.length));
				if (distStDev == 0) {
					System.out.println(" ------- /!\\ StDevDist = 0, no good!! -------");
				}

				for (int k = 0; k < targetPage.size(); k++) {
					for (int l = 0; l < windowSizes.length; l++) {
						double dist = distanceMatrix.get(i, j, k, l);
						double normDist = (dist - distMean) / distStDev ;
						normColorDistMatrix.set(i,j,k,l, normDist);
					}
				}

				for (int k = 0; k < windowSizes.length; k++) {

					int steepnessLen = steepness.length;

					for (int l = 0; l < steepnessLen; l++) {

						BufferedImage oldImg = targetImages.get(j);
						BufferedImage img = new BufferedImage(oldImg.getWidth(), oldImg.getHeight(), BufferedImage.TYPE_INT_RGB);
						Graphics2D g = (Graphics2D) img.getGraphics();
						g.drawImage(oldImg, 0,0, null);
						g.setColor(Color.white);
						g.fillRect(0, 0, img.getWidth(), img.getHeight());

						for (int m = 0; m < targetPage.size(); m++) {

							//un-normalize to get image coords
							Node node = targetPage.get(m);
							double nodeX = (node.getDouble("x") * xStDev) + xMean;
							double nodeY = (node.getDouble("y") * yStDev) + yMean;
							// careful with access order
//							double dist = distanceMatrix.get(i, j, m, k);
							double dist = normColorDistMatrix.get(i,j,m,k);

							// convert edit distance to color
							// dist 0 goes to black
							// dist max goes to white

							float distSig = (float) (1 - (1 / (1 + Math.exp(-dist * steepness[l]))));
							Color colorSig = new Color(distSig, distSig, distSig);
							g.setColor(colorSig);
							convMethod = "sig" + steepness[l];

							g.fillOval((int) Math.floor(nodeX) - RADIUS, (int) Math.floor(nodeY) - RADIUS, 2 * RADIUS, 2 * RADIUS);
						}

						// display bounding boxes
						g.setColor(Color.red);
						g.setStroke(new BasicStroke(2));
						for (int m = 0; m < boundingBoxesGT.size(); m++) {
							// only keep char
							if (boundingBoxesGT.get(m).getCharacter().equals(source.get(i).getClassName())) {
								int[] coords = boundingBoxesGT.get(m).getCoords();
								g.drawRect(coords[0], coords[1], coords[2] - coords[0], coords[3] - coords[1]);
							}
						}

						String targetName = targetPage.getFileName().substring(0, targetPage.getFileName().length() - 4);
						String targetFile = visualizationFolder.toString() + "/" ;
						String propFile = prop.split("/")[(prop.split("/").length)-1];
						String maxD = "";
						maxD = "_globD";
						targetFile = targetFile + targetName + "/globmax/" + propFile + "/" + convMethod;
						Files.createDirectories(Paths.get(targetFile));
						String imgName = "hm_" + targetName + "_" + convMethod + maxD + "_w" + windowSizes[k] + "_" + source.get(i).getClassName() + ".png";
						ImageIO.write(img, "png", new File(targetFile +  "/" + imgName));
					}
				}
			}
		}
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

		// create a distance matrix to store the resulting dissimilarities
		this.r = this.source.size();
		this.c = this.target.size();
		this.distanceMatrix             = new FourDimAL<>();
// 		this.normalisedDistanceMatrix   = new FourDimAL<>();
		this.normColorDistMatrix = new FourDimAL<>();
				
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

		// extract groundtruth bounding boxes from XML file
		this.boundingBoxesGT = new ArrayList<>();
		Path boundingBoxesGTPath = Paths.get(properties.getProperty("boundingBoxesGT"));
		if (Files.isRegularFile(boundingBoxesGTPath)) {
			File boundingBoxesFile = new File(String.valueOf(boundingBoxesGTPath));
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document boundingBoxesDoc = dBuilder.parse(boundingBoxesFile);
			boundingBoxesDoc.getDocumentElement().normalize();
			NodeList boundingBoxes = boundingBoxesDoc.getElementsByTagName("box");
			for (int i = 0; i < boundingBoxes.getLength(); i++){
				Element boundingBox = (Element) boundingBoxes.item(i);
				String boundingBoxId = boundingBox.getAttribute("id");
				String boundingBoxChar = boundingBox.getAttribute("char");
				Integer boundingBoxX1 = Integer.valueOf(boundingBox.getElementsByTagName("x1").item(0).getTextContent());
				Integer boundingBoxY1 = Integer.valueOf(boundingBox.getElementsByTagName("y1").item(0).getTextContent());
				Integer boundingBoxX2 = Integer.valueOf(boundingBox.getElementsByTagName("x2").item(0).getTextContent());
				Integer boundingBoxY2 = Integer.valueOf(boundingBox.getElementsByTagName("y2").item(0).getTextContent());
				int[] coords = new int[] {boundingBoxX1, boundingBoxY1, boundingBoxX2, boundingBoxY2};
				BoundingBox tempBB = new BoundingBox(boundingBoxId, boundingBoxChar, coords);
				boundingBoxesGT.add(tempBB);
			}

		}

		this.visualizationFolder = Paths.get(properties.getProperty("editDistVis"));

		int numOfSteepnessVal = Integer.parseInt(properties.getProperty("numOfSteepnessVal"));
		steepness = new double[numOfSteepnessVal];
		for (int i = 0; i < numOfSteepnessVal; i++) {
			steepness[i] = Double.parseDouble(properties.getProperty("steepnessVal"+i));
		}

		// open original binarized images as matrices
		Imgcodecs imgcodecs = new Imgcodecs();
		Path originalImagesPath = Paths.get(properties.getProperty("imagesPath"));
		this.targetImages = new ArrayList<>();
		for (int j = 0; j < target.size(); j++) {
			String imagePath = originalImagesPath + "\\" + target.get(j).getFileName().substring(0, target.get(j).getFileName().length() - 4) + ".png";
			BufferedImage oldImg = ImageIO.read(new File(imagePath));
			targetImages.add(oldImg);
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
