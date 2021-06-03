

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
import java.util.*;
import java.util.List;

import static gwenael.TreeMapValueSort.entriesSortedByValues;

/**
 * @author riesen
 * modified Gwenael
 *
 */
public class GraphMatchingSegFreeGW {

//	static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }


	/**
	 * the sets of graphs to be matched
	 */
	private GraphSet source, target;

	/**
	 * the resulting distances, corresponding to the windows AL
	 */
	private TwoDimAL<Map<Integer, Double>> distanceList;

	private TwoDimAL<Map<Integer, Double>> normDistanceList;

	/**
	 * the source graph, target graph and target page actually to be matched (temp ist for temporarily swappings)
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

	/**
	 * Ground truth and trecEval
	 */
	private TreeMap<String, String> wordList;
	private TrecEval trecEval;

	/**
	 * size of windows relative to source char
	 */
	 private double[] windowSizes;

	/**
	 * stores accepted windows' window size & grid position
	 */
	private FourDimAL<Integer> candWindows;

	/**
	 * count of accepted windows
	 */
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

	private double IoU_Ratio;

	private FourDimAL<Integer> windowNodeCount;
	private double nodeRatio;

	private static final int numBestMatches = 10;

	/**
	 * @param args
	 * properties[0] is an url to a properties file, where all parameters are defined
	 * (e.g. /Users/riesen/Documents/GraphMatching/properties/testproperties.prop)
	 */
	public static void main(String[] args) {
		try {
			new GraphMatchingSegFreeGW(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//andreas

	/**
	 * the matching procedure: set of graphs, print distance matrix
	 * @throws Exception
	 */
	public GraphMatchingSegFreeGW(String prop) throws Exception {

		// initialize the matching
		long initTimeStart = System.currentTimeMillis();
		System.out.println("Initializing the matching according to the properties...");
		this.init(prop);
		long initTime = System.currentTimeMillis() - initTimeStart;
		long initTimeMin = initTime / 60000;
		long initTimeSec = (initTime / 1000) % 60;
		System.out.println("Time for init(): "+ initTimeMin+" m "+initTimeSec+" s.");
		int totalNodeCount = 0;
		for (int i = 0; i < source.size(); i++){
			totalNodeCount += source.get(i).size();
		}
		for (int i = 0; i < target.size(); i++) {
			totalNodeCount += target.get(i).size();
		}
		System.out.println("Total number of nodes: "+totalNodeCount);

		// the cost matrix used for bipartite matchings
		double[][] costMatrix;
		// counts the progress
		this.counter = 0;

		// iterate through all pairs of graphs g_i x g_j from (source, target)
		System.out.println("Starting the matching...");
		System.out.println("Progress...");

		int numOfMatchings = 0;
		for (BufferedImage img : targetImages){
			numOfMatchings +=  (img.getWidth() / stepX + 1) * (img.getHeight() / stepY + 1);
		}
		numOfMatchings *= this.source.size() * windowSizes.length;
		System.out.println("Maximum number of matchings: "+numOfMatchings);

		// distance value d
		double d = -1;
		double d_norm = -1;

		// swapped the graphs?
		boolean swapped = false;

		// init editPath (for one matching)
		EditPath editPath = null;

		// main matching loop
		long start = System.currentTimeMillis();
		for (int i = 0; i < source.size(); i++) {
			sourceGraph = this.source.get(i);
			int sourceNodeCount = sourceGraph.size();

			// get size of source graph
			// max taken before normalization
			double sourceWidth = sourceImages.get(i).getWidth();
			double sourceHeight = sourceImages.get(i).getHeight();

			for (int j = 0; j < target.size(); j++) {

				targetPage = this.target.get(j);

				//get values
				double targetWidth = targetImages.get(j).getWidth();
				double targetHeight = targetImages.get(j).getHeight();
				double xMean = targetPage.getDouble("x_mean");
				double yMean = targetPage.getDouble("y_mean");
				double xStDev = targetPage.getDouble("x_std");
				double yStDev = targetPage.getDouble("y_std");

				// compute and store grid sizes
				int numOfStepsX = (int) Math.ceil(targetWidth / stepX);
				int numOfStepsY = (int) Math.ceil(targetHeight / stepY);
				int numOfGridPoints = numOfStepsX * numOfStepsY;
				gridSizes.set(j, 0, numOfGridPoints);
				gridSizes.set(j, 1, numOfStepsX);
				gridSizes.set(j, 2, numOfStepsY);

				// window dimensions
				double[][] windowMaxSides = new double[windowSizes.length][2];
				for (int k = 0; k < windowSizes.length; k++){
					windowMaxSides[k] = new double[] {windowSizes[k] * (sourceWidth) / (xStDev)  ,
							windowSizes[k] * (sourceHeight) / (yStDev)};
				}

				GroundtruthPage targetPageGroundtruth = groundtruthPages.get(j);

				// create windows, starting with top-left corner
				for (int k = 0; k < numOfGridPoints; k++){
					// calculate corner position
					int gridRow = k / numOfStepsX;
					int gridColumn = k % numOfStepsX;

					double columnCoord = gridColumn * stepX;
					double rowCoord = gridRow * stepY;

					double[] windowCornerCoords = {(columnCoord - xMean) / xStDev, (rowCoord - yMean) / yStDev};

					// windows extraction
					ArrayList<Graph> targetWindows = targetPage.extractWindowsCornerCoords(windowCornerCoords, windowMaxSides);

					for (int l = 0; l < windowSizes.length; l++) {

						swapped = false;

						targetGraph = targetWindows.get(l);

						int targetNodeCount = targetGraph.size();

						// check node count, close enough to source graph?
						windowNodeCount.set(i,j,k,l,targetNodeCount);
						double matchingNodeRatio = (double) sourceNodeCount / (double) targetNodeCount;
						if ((matchingNodeRatio > (1 / nodeRatio)) && (matchingNodeRatio < nodeRatio)) {
							// store accepted windows indices
							int winNum = windowsCount.get(i,j);
							candWindows.set(i,j,winNum,0,k);
							candWindows.set(i,j,winNum,1,l);
							windowsCount.set(i,j, winNum +1);

							if (counter % 1000 == 0) {
								System.out.println("Matching " + counter + " of possibly " + numOfMatchings);
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
									editPath = null;

									double[] distances = this.editDistance.getNormalisedEditDistance(sourceGraph, targetGraph, d, normalisationFunction);

									d = distances[0];
									// normalization is NOT to N(0,1) !
									// d_norm = distances[1];
								}
							}

							// whether distances or similarities are computed
							if (this.simKernel < 1) {
								this.distanceList.get(i,j).put(winNum, d);
							} else {
								switch (this.simKernel) {
									case 1:
										this.distanceList.get(i,j).put(winNum, -Math.pow(d, 2.0));
										break;
									case 2:
										this.distanceList.get(i,j).put(winNum, -d);
										break;
									case 3:
										this.distanceList.get(i,j).put(winNum, Math.tanh(-d));
										break;
									case 4:
										this.distanceList.get(i,j).put(winNum, Math.exp(-d));
										break;
								}
							}

							if (swapped) {
								this.swapGraphs();
							}

							this.counter++;
						}
					}
				}
			}
		}

		System.out.println("Final number of matchings: "+counter);

		long matchingTime = System.currentTimeMillis() - start;

		// computing means and standard deviation for source-target pairs

		for (int i = 0; i < source.size(); i++) {
			for (int j = 0; j < target.size(); j++) {
				for (int n = 0; n < candWindows.get(i).get(j).size(); n++) {
					double dist = distanceList.get(i,j).get(n);

					windowsDistMeanStd.set(i, j, 0, windowsDistMeanStd.get(i, j, 0) + dist);
				}
			}
		}

		for (int i = 0; i < source.size(); i++) {
			for (int j = 0; j < target.size(); j++) {
				windowsDistMeanStd.set(i,j,0,windowsDistMeanStd.get(i,j,0)/windowsCount.get(i,j));
			}
		}

		for (int i = 0; i < source.size(); i++) {
			for (int j = 0; j < target.size(); j++) {
				for (int n = 0; n < candWindows.get(i).get(j).size(); n++) {
					double dist = distanceList.get(i, j).get(n);
					double distMean = windowsDistMeanStd.get(i, j, 0);
					windowsDistMeanStd.set(i, j, 1, windowsDistMeanStd.get(i, j, 1) + Math.pow(dist - distMean, 2));
				}
			}
		}

		for (int i = 0; i < source.size(); i++) {
			for (int j = 0; j < target.size(); j++) {
				double distStDev = Math.sqrt(windowsDistMeanStd.get(i,j,1) / windowsCount.get(i,j));
				windowsDistMeanStd.set(i,j,1, distStDev);
				if (distStDev == 0) {
					System.out.println(" ------- /!\\ StDevDist = 0, no good!! -------");
				}
			}
		}

		// store spotting results
		ArrayList<SpottingResult> spottingResults = new ArrayList<>();

		for (int i = 0; i < source.size(); i++) {

			Graph sourceGraph = source.get(i);
			String sourceID = sourceGraph.getGraphID();
			String sourceClass = this.wordList.get(sourceID);

			for (int j = 0; j < target.size(); j++) {

				targetPage = target.get(j);
				String targetPageID = targetPage.getGraphID();


				GroundtruthPage targetPageGroundtruth = groundtruthPages.get(j);
				for (int n = 0; n < candWindows.get(i).get(j).size(); n++) {
					ArrayList<Integer> windowRefs = candWindows.get(i).get(j).get(n);
					int k = windowRefs.get(0);
					int l = windowRefs.get(1);

					String targetID = targetPageID+"_pt"+k+"_2"+windowSizes[l];
					String targetClass = "noCharLine";

					double dist = distanceList.get(i,j).get(n);
					double distMean = windowsDistMeanStd.get(i, j, 0);
					double distStDev = windowsDistMeanStd.get(i, j, 1);
					double normDist = (dist - distMean) / distStDev;
					normDistanceList.get(i).get(j).put(n, normDist);

					int numOfStepsX = gridSizes.get(j,1);

					int nodeX = (k % numOfStepsX) * stepX;
					int nodeY = (k / numOfStepsX) * stepY;

					int windowWidth = (int) (windowSizes[l] * sourceImages.get(i).getWidth());
					int windowHeight = (int) (windowSizes[l] * sourceImages.get(i).getHeight());

					Rectangle sourceRect = new Rectangle(nodeX, nodeY, windowWidth, windowHeight);

					// check every line of GT
					for (int m = 0; m < targetPageGroundtruth.getLines().size(); m++) {
						GroundtruthLine groundtruthLine = targetPageGroundtruth.getLines().get(m);
						//replace line Polygon with bounding box Rectangle for easy area computation
						Rectangle lineBoundingBox = groundtruthLine.getPolygon().getBounds();
						if (lineBoundingBox.intersects(sourceRect)) {
							double intersectArea = getArea(lineBoundingBox.intersection(sourceRect));
							// cant use IoU here, since line area is huge
							// --> use intersection/Area
							double IoA = intersectArea / getArea(sourceRect);
							if (IoA >= IoU_Ratio){
								if (groundtruthLine.getGroundtruth().contains(sourceClass)){
									targetClass = sourceClass;
								}
								break; // no double intersection
							}
						}
					}

					SpottingResult spottingResult = new SpottingResult(sourceID, sourceClass, targetID, targetClass, normDist);
					spottingResults.add(spottingResult);

				}
			}
		}

		SpottingPostProcessing spottingPostProcessing = new SpottingPostProcessing();
		ArrayList<SpottingResult> reducedSpottingResults = spottingPostProcessing.postProcess(spottingResults);

		trecEval.exportSpottingResults(reducedSpottingResults);

		// images creation
		for (int i = 0; i < source.size(); i++) {
			BufferedImage charImg = sourceImages.get(i);
			for (int j = 0; j < target.size(); j++) {
				BufferedImage greyImg = targetImages.get(j);
				int targetWidth = greyImg.getWidth();
				int targetHeight = greyImg.getHeight();
				int numOfStepsX = gridSizes.get(j,1);

				BufferedImage img = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
				Graphics g = (Graphics2D) img.getGraphics();
				g.drawImage(greyImg, 0, 0, null);
				SortedSet<Map.Entry<Integer, Double>> sortedEntries = entriesSortedByValues(distanceList.get(i,j));
				Iterator<Map.Entry<Integer, Double>> it = sortedEntries.iterator();
				ArrayList<Integer> bestMatches = new ArrayList<>();

				boolean newZone;
				// GroundtruthPage targetPageGroundtruth = groundtruthPages.get(j);
				for (int m = 0; m < numBestMatches; m++) {
					newZone = false;
					boolean overlap;
					while(!newZone) {
						Map.Entry<Integer, Double> nextBestMatch = it.next();
						int nextBestKey = nextBestMatch.getKey();
						ArrayList<Integer> winRefNext = candWindows.get(i).get(j).get(nextBestKey);
						int cornerXNext = (winRefNext.get(0) % numOfStepsX) * stepX;
						int cornerYNext = (winRefNext.get(0) / numOfStepsX) * stepY;
						int winWidthNext = (int) windowSizes[winRefNext.get(1)] * charImg.getWidth();
						int winHeightNext = (int) windowSizes[winRefNext.get(1)] * charImg.getHeight();
						Rectangle rectNext = new Rectangle(cornerXNext, cornerYNext, winWidthNext, winHeightNext);
						overlap = false;
						for(int n = 0; n < bestMatches.size(); n++) {
							ArrayList<Integer> winRefComp = candWindows.get(i).get(j).get(bestMatches.get(n));
							int cornerXComp = (winRefComp.get(0) % numOfStepsX) * stepX;
							int cornerYComp = (winRefComp.get(0) / numOfStepsX) * stepY;
							int winWidthComp = (int) windowSizes[winRefComp.get(1)] * charImg.getWidth();
							int winHeightComp = (int) windowSizes[winRefComp.get(1)] * charImg.getHeight();
							Rectangle rectComp = new Rectangle(cornerXComp, cornerYComp, winWidthComp, winHeightComp);
							if (rectComp.intersects(rectNext)){
								overlap = true;
								break;
							}
						}
						if (!overlap) {
							newZone = true;
							bestMatches.add(nextBestKey);
						}
					}
				}
				g.setColor(Color.BLACK);
				for (int m = 0; m < bestMatches.size(); m++) {
					ArrayList<Integer> winRef = candWindows.get(i).get(j).get(bestMatches.get(m));
					int cornerX = (winRef.get(0) % numOfStepsX) * stepX;
					int cornerY = (winRef.get(0) / numOfStepsX) * stepY;
					int winWidth = (int) windowSizes[winRef.get(1)] * charImg.getWidth();
					int winHeight = (int) windowSizes[winRef.get(1)] * charImg.getHeight();
					g.drawRect(cornerX, cornerY, winWidth, winHeight);
					g.drawImage(charImg, cornerX, cornerY, cornerX + winWidth, cornerY + winHeight,
							0, 0, charImg.getWidth(), charImg.getHeight(), null);
				}


				String imgFolder = charVisFolder.toString() + "/best/";
				Files.createDirectories(Paths.get(imgFolder));
				String propFile = prop.split("[/\\\\]")[(prop.split("[/\\\\]").length)-1].split("\\.")[0];
				String imgName = imgFolder+propFile+"_s"+i+"_t"+j+"_"+(int)costFunctionManager.getNodeCost()+"_"+(int)costFunctionManager.getEdgeCost()
						+"_"+costFunctionManager.getAlpha()+"_"+costFunctionManager.getNodeAttrImportance()[0]+".png";
				ImageIO.write(img, "png", new File(imgName));
			}

		}



		this.resultPrinter.printInfos(source, target, prop, initTime, matchingTime);


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

		this.distanceList = new TwoDimAL<>();
		for (int i = 0; i < source.size(); i++) {
			for (int j = 0; j < target.size(); j++) {
				distanceList.set(i,j,new TreeMap<>());
			}
		}
		
		this.normDistanceList = new TwoDimAL<>();
		for (int i = 0; i < source.size(); i++) {
			for (int j = 0; j < target.size(); j++) {
				normDistanceList.set(i,j,new TreeMap<>());
			}
		}

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

		this.candWindows = new FourDimAL<>();

		this.windowsCount = new TwoDimAL<>();
		for (int i = 0; i < source.size(); i++) {
			for (int j = 0; j < target.size(); j++) {
				windowsCount.set(i,j,0);
			}
		}

		this.windowsDistMeanStd = new ThreeDimAL<>();
		for (int i = 0; i < source.size(); i++) {
			for (int j = 0; j < target.size(); j++) {
				windowsDistMeanStd.set(i,j,0, 0.);
				windowsDistMeanStd.set(i,j,1, 0.);
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

		this.hotmapVisFolder = Paths.get(properties.getProperty("editDistVis"));
		this.charVisFolder = Paths.get(properties.getProperty("charVisFolder"));

		// open original binarized images as matrices
		Imgcodecs imgcodecs = new Imgcodecs();
		Path targetImagesPath = Paths.get(properties.getProperty("targetImagesPath"));
		this.targetImages = new ArrayList<>();
		for (int j = 0; j < target.size(); j++) {
			String imagePath = targetImagesPath + "\\" + target.get(j).getFileName().substring(0, target.get(j).getFileName().length() - 4) + "_r.png";
			System.out.println(imagePath);
			BufferedImage oldImg = ImageIO.read(new File(imagePath));
			targetImages.add(oldImg);
		}
		Path sourceImagesPath = Paths.get(properties.getProperty("sourceImagesPath"));
		this.sourceImages = new ArrayList<>();
		for (int j = 0; j < source.size(); j++) {
			String imagePath = sourceImagesPath + "\\" + source.get(j).getFileName().substring(0, source.get(j).getFileName().length() - 4) + "_tt.png";
			System.out.println(imagePath);
			BufferedImage oldImg = ImageIO.read(new File(imagePath));
			sourceImages.add(oldImg);
		}


		// TODO clean thresh

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
