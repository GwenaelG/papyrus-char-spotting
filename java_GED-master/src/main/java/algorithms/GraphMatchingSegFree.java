

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import util.EditDistance;
import util.MatrixGenerator;
import util.ResultPrinter;
import util.treceval.SpottingPostProcessing;
import util.treceval.SpottingResult;
import util.treceval.TrecEval;
import xml.GraphParser;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

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
	private TwoDimAL<BoundingBox> boundingBoxesGT;

	// images for dist display
	private ArrayList<BufferedImage> targetImages;
	private ArrayList<BufferedImage> sourceImages;

	// size of circles
	private static final int RADIUS = 5;

	// folder for saving images
	private Path hotmapVisFolder;
	private Path charVisFolder;

	// use sigmoid (or linear distance-to-color conversion )
	private boolean sigmoid;

	// normalized threshold (for normalized distances)
	private double[] thresholds;

	private FiveDimAL<Boolean> underThresholdMat;

	private FourDimAL<Integer> truePositives;
	private FourDimAL<Integer> trueNegatives;
	private FourDimAL<Integer> falsePositives;
	private FourDimAL<Integer> falseNegatives;

	private int stepX;
	private int stepY;
	private TwoDimAL<Integer> gridSizes;

	private double IoU_Ratio;

	private FourDimAL<Boolean> nodeRatioOK;
	private FourDimAL<Integer> windowNodeCount;
	private double nodeRatio;

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

		int numOfMatchings = 0;
		for (BufferedImage img : targetImages){
			numOfMatchings +=  (img.getWidth() / stepX + 1) * (img.getHeight() / stepY + 1);
		}
		numOfMatchings *= this.source.size() * windowSizes.length;

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
			int sourceNodeCount = sourceGraph.size();

			// get size of source graph
			// max taken before normalization
			double sourceWidth = sourceImages.get(i).getWidth();
			double sourceHeight = sourceImages.get(i).getHeight();

			for (int j0 = 0; j0 < idxs2.length; j0++) {
				int j = idxs2[j0];

				targetPage = this.target.get(j);

				double targetWidth = targetImages.get(j).getWidth();
				double targetHeight = targetImages.get(j).getHeight();
				double xMean = targetPage.getDouble("x_mean");
				double yMean = targetPage.getDouble("y_mean");
				double xStDev = targetPage.getDouble("x_std");
				double yStDev = targetPage.getDouble("y_std");

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


				// create windows, starting with top-left corner
				for (int k = 0; k < numOfGridPoints; k++){
					int gridRow = k / numOfStepsX;
					int gridColumn = k % numOfStepsX;

					double columnCoord = gridColumn * stepX;
					double rowCoord = gridRow * stepY;

					double[] windowCornerCoords = {(columnCoord - xMean) / xStDev, (rowCoord - yMean) / yStDev};

					ArrayList<Graph> windows = targetPage.extractWindowsCornerCoords(windowCornerCoords, windowMaxSides);

					for (int l = 0; l < windowSizes.length; l++) {

						swapped = false;

						targetGraph = windows.get(l);

						int targetNodeCount = targetGraph.size();

						windowNodeCount.set(i,j,k,l,targetNodeCount);
						boolean ratioOK = false;
						ratioOK = false;
						double matchingNodeRatio = (double) sourceNodeCount / (double) targetNodeCount;
						if ((matchingNodeRatio > (1 / nodeRatio)) && (matchingNodeRatio < nodeRatio)) {
							ratioOK = true;
						}
						nodeRatioOK.set(i,j,k,l,ratioOK);

						if(k % 61 == 0) {
							BufferedImage img = targetGraph.displayGraph((int) targetWidth, (int) targetHeight);
							Graphics g = (Graphics2D) img.getGraphics();
							g.setColor(Color.GRAY);
							g.drawRect((int) columnCoord,(int) rowCoord,(int) sourceWidth,(int) sourceHeight);
							ImageIO.write(img, "png", new File("C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/files/vis/test/"+String.format("%05d", k)+".png"));
						}

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
								editPath = null;

								double[] distances = this.editDistance.getNormalisedEditDistance(sourceGraph, targetGraph, d, normalisationFunction);

								d = distances[0];
								d_norm = distances[1];
							}
						}

						
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

		ArrayList<SpottingResult> spottingResults = new ArrayList<>();

		// display edit distance between target node window and source char
		for (int i = 0; i < source.size(); i++) {

		 	Graph sourceGraph = source.get(i);
			String charID = sourceGraph.getGraphID();
		 	String charClass = this.wordList.get(sourceGraph.getGraphID());

			for (int j = 0; j < target.size(); j++) {

				int numOfGridPoints = gridSizes.get(j,0);
				int numOfStepsX = gridSizes.get(j,1);

				targetPage = target.get(j);
				String targetPageID = targetPage.getGraphID();

				double xMean = targetPage.getDouble("x_mean");
				double yMean = targetPage.getDouble("y_mean");
				double xStDev = targetPage.getDouble("x_std");
				double yStDev = targetPage.getDouble("y_std");

				TwoDimAL<Double> minDists = new TwoDimAL<>();
				//store overall min dist, min dist in a BB, min dist with no BB overlap
				for (int z = 0; z < 2; z++) {
					minDists.set(z,0, Double.POSITIVE_INFINITY);
					minDists.set(z,1,(double) 0);
					minDists.set(z,2,(double) 0);
				}

				// normalize distances for threshold
				double distMean = 0;
				double distSumSqDiff = 0;

				for (int k = 0; k < numOfGridPoints; k++) {
					for (int l = 0; l < windowSizes.length; l++) {
						double dist = distanceMatrix.get(i, j, k, l);
						//overall min distance, even if no nodes
						if (dist < minDists.get(0, 0)) {
								minDists.set(0, 0, dist);
								minDists.set(0, 1, (double) k);
								minDists.set(0, 2, (double) l);
						}
						distMean += dist;
					}
				}
				distMean /= (numOfGridPoints*windowSizes.length);

				for (int k = 0; k < numOfGridPoints; k++) {
					for (int l = 0; l < windowSizes.length; l++) {
						double dist = distanceMatrix.get(i, j, k, l);
						distSumSqDiff += Math.pow(dist - distMean,2);
					}
				}

				double distStDev = Math.sqrt(distSumSqDiff / (numOfGridPoints*windowSizes.length));
				if (distStDev == 0) {
					System.out.println(" ------- /!\\ StDevDist = 0, no good!! -------");
				}

				for (int k = 0; k < numOfGridPoints; k++) {
					for (int l = 0; l < windowSizes.length; l++) {
						double dist = distanceMatrix.get(i, j, k, l);
						double normDist = (dist - distMean) / distStDev ;
						normColorDistMatrix.set(i,j,k,l, normDist);
					}
				}

				for (int l = 0; l < windowSizes.length; l++) {

					int windowWidth = (int) (windowSizes[l] * sourceImages.get(i).getWidth());
					int windowHeight = (int) (windowSizes[l] * sourceImages.get(i).getHeight());

					for (int k = 0; k < numOfGridPoints; k++) {

						String targetWindowID = targetPageID+"_pt"+k+"_w"+windowSizes[l];

						int nodeX = (k % numOfStepsX) * stepX;
						int nodeY = (k / numOfStepsX) * stepY;

						// careful with access order
						double normDist = normColorDistMatrix.get(i,j,k,l);

						for(int t = 0; t < thresholds.length; t++){
							boolean underThresh = false;
							double thresh = thresholds[t];
							if (normDist <= thresh) {
								underThresh = true;
							}
							underThresholdMat.set(i,j,k,l,t,underThresh);
						}

						String targetWindowClass = "";

						boolean inBB = false;
						boolean touchBB = false;
						for (int n = 0; n < boundingBoxesGT.get(j).size(); n++) {
							int[] coords = boundingBoxesGT.get(j, n).getCoords();
							int bbX1 = coords[0];
							int bbY1 = coords[1];
							int bbX2 = coords[2];
							int bbY2 = coords[3];
							// max(...) get coords of topleft corner of intersection rectangle
							// min(...) get coords of bottomright corner
							int topLeftCornerX = Math.max(nodeX, bbX1);
							int topLeftCornerY = Math.max(nodeY, bbY1);
							int bottomRightCornerX = Math.min(nodeX + windowWidth, bbX2);
							int bottomRightCornerY = Math.min(nodeY + windowHeight, bbY2);
							// compare them to be sure there is actually an intersection
							if ((topLeftCornerX < bottomRightCornerX) && (topLeftCornerY < bottomRightCornerY)){
								touchBB = true;
								int bbArea = (bbX2 - bbX1) * (bbY2 - bbY1);
								int windowArea = windowWidth * windowHeight;
								int intersectionArea = (bottomRightCornerX - topLeftCornerX) * (bottomRightCornerY - topLeftCornerY);
								double IoU = intersectionArea / (double)(bbArea + windowArea - intersectionArea);
								//check if rectangles overlap enough
								if (IoU >= IoU_Ratio) {
									inBB = true;
									for(int t = 0; t < thresholds.length; t++) {
										if (underThresholdMat.get(i,j,k,l,t)) {
											this.truePositives.set(i, j, l, t, this.truePositives.get(i, j, l, t) + 1);
										} else {
											this.falseNegatives.set(i, j, l, t, this.falseNegatives.get(i, j, l, t) + 1);
										}
									}
									double dist = distanceMatrix.get(i,j,k,l);
//									System.out.println(k+": "+topLeftCornerX+" "+topLeftCornerY+" "+bottomRightCornerX+" "+bottomRightCornerY+", "
//											+String.format("%.3f",IoU)+" "+String.format("%.3f",dist)
//											+" "+windowNodeCount.get(i,j,k,l)+" "+nodeRatioOK.get(i,j,k,l));
//									if (dist < minDists.get(1, 0)) {
//										minDists.set(1, 0, dist);
//										minDists.set(1, 1, (double) k);
//										minDists.set(1, 2, (double) l);
//									}

									targetWindowClass = charClass;
									break;
								}
							}
						}
						if (!inBB) {
							for(int t = 0; t < thresholds.length; t++) {
								if (underThresholdMat.get(i,j,k,l,t)) {
									this.falsePositives.set(i, j, l, t, this.falsePositives.get(i, j, l,t) + 1);
								} else {
									this.trueNegatives.set(i, j, l, t,this.trueNegatives.get(i, j, l, t) + 1);
								}
							}
							if (!touchBB) {
								double dist = distanceMatrix.get(i,j,k,l);
								if (dist < minDists.get(1, 0)) {
									minDists.set(1, 0, dist);
									minDists.set(1, 1, (double) k);
									minDists.set(1, 2, (double) l);
								}
							}

							targetWindowClass = "notChar";
						}
						SpottingResult spottingResult = new SpottingResult(charID, charClass, targetWindowID, targetWindowClass, normDist);
						spottingResults.add(spottingResult);
					}
				}

				SpottingPostProcessing spottingPostProcessing = new SpottingPostProcessing();
				ArrayList<SpottingResult> reducedSpottingResults = spottingPostProcessing.postProcess(spottingResults);

				trecEval.exportSpottingResults(reducedSpottingResults);

				//  display best match
				BufferedImage greyImg = targetImages.get(j);
				int targetWidth = greyImg.getWidth();
				int targetHeight = greyImg.getHeight();
				BufferedImage img = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
				Graphics g = (Graphics2D) img.getGraphics();
				g.drawImage(greyImg, 0, 0, null);
				//blue overall, green BB, red no touch
				Color[] c = {Color.GREEN, Color.GRAY};
				String[] msg = {"overall", "no BB"};
				for (int n = 0; n < c.length; n++) {
					int cornerX = (minDists.get(n,1).intValue() % numOfStepsX) * stepX;
					int cornerY = (minDists.get(n,1).intValue() / numOfStepsX) * stepY;
					BufferedImage charImg = sourceImages.get(i);
					int windowWidth = (int) (windowSizes[minDists.get(n, 2).intValue()] * charImg.getWidth());
					int windowHeight = (int) (windowSizes[minDists.get(n, 2).intValue()] * charImg.getHeight());
					g.setColor(c[n]);
					g.drawRect(cornerX, cornerY, windowWidth, windowHeight);
					g.drawImage(charImg, cornerX, cornerY, cornerX + windowWidth, cornerY + windowHeight,
							0, 0, charImg.getWidth(), charImg.getHeight(), null);
//					System.out.println(msg[n]+" "+minDists.get(n,0)+" "+windowNodeCount.get(i,j,minDists.get(n,1).intValue(),minDists.get(n,2).intValue()));
				}

//				for (int k = 0; k < numOfGridPoints; k++) {
//					int l = 0;
//					int nodeX = (k % numOfStepsX) * stepX;
//					int nodeY = (k / numOfStepsX) * stepY;
//					int nc = windowNodeCount.get(i,j,k,l);
//					g.setColor(new Color(nc, nc, nc));
//					g.fillRect(nodeX, nodeY, 1, 1);
//
//				}
				String imgFolder = charVisFolder.toString() + "/";
				Files.createDirectories(Paths.get(imgFolder));
				String propFile = prop.split("[/\\\\]")[(prop.split("[/\\\\]").length)-1].split("\\.")[0];
				String imgName = imgFolder+propFile+"_"+(int)costFunctionManager.getNodeCost()+"_"+(int)costFunctionManager.getEdgeCost()
						+"_"+costFunctionManager.getAlpha()+"_"+costFunctionManager.getNodeAttrImportance()[0]+".png";
				ImageIO.write(img, "png", new File(imgName));
			}
		}

		String propName = prop.split("[/\\\\]")[(prop.split("[/\\\\]").length)-1].split("\\.")[0];
		this.resultPrinter.printResultGw(propName, source, target, windowSizes, thresholds, truePositives, falseNegatives,
				falsePositives, trueNegatives);


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
		this.boundingBoxesGT = new TwoDimAL<BoundingBox>();
		String boundingBoxesFolder = properties.getProperty("boundingBoxesFolder");
		for (int i = 0; i < c; i ++) {
			Path boundingBoxesFilePath = Paths.get(boundingBoxesFolder, target.get(i).getFileName().split("\\.")[0]+".xml");
			boundingBoxesGT.add(new ArrayList<>());
			if (Files.isRegularFile(boundingBoxesFilePath)) {
				File boundingBoxesFile = new File(String.valueOf(boundingBoxesFilePath));
				DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document boundingBoxesDoc = dBuilder.parse(boundingBoxesFile);
				boundingBoxesDoc.getDocumentElement().normalize();
				NodeList boundingBoxes = boundingBoxesDoc.getElementsByTagName("box");
				for (int j = 0; j < boundingBoxes.getLength(); j++){
					Element boundingBox = (Element) boundingBoxes.item(j);
					String boundingBoxId = boundingBox.getAttribute("id");
					String boundingBoxChar = boundingBox.getAttribute("char");
					Integer boundingBoxX1 = Integer.valueOf(boundingBox.getElementsByTagName("x1").item(0).getTextContent());
					Integer boundingBoxY1 = Integer.valueOf(boundingBox.getElementsByTagName("y1").item(0).getTextContent());
					Integer boundingBoxX2 = Integer.valueOf(boundingBox.getElementsByTagName("x2").item(0).getTextContent());
					Integer boundingBoxY2 = Integer.valueOf(boundingBox.getElementsByTagName("y2").item(0).getTextContent());
					int[] coords = new int[] {boundingBoxX1, boundingBoxY1, boundingBoxX2, boundingBoxY2};
					BoundingBox tempBB = new BoundingBox(boundingBoxId, boundingBoxChar, coords);
					boundingBoxesGT.set(i,j,tempBB);
				}
			}
		}

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
		this.underThresholdMat = new FiveDimAL<>();
		this.truePositives = new FourDimAL<>();
		this.trueNegatives = new FourDimAL<>();
		this.falsePositives = new FourDimAL<>();
		this.falseNegatives = new FourDimAL<>();

		for (int i = 0; i < r; i++) {
			for (int j = 0; j < c; j++) {
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
		this.nodeRatioOK = new FourDimAL<>();
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
