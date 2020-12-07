/**
 * 
 */
package graph;

import com.sun.security.auth.NTDomainPrincipal;
import gwenael.ImageDisp;
import net.sourceforge.gxl.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author riesen
 *
 */
@SuppressWarnings("serial")
public class Graph extends LinkedList<Node> {

	/**
	 * the class of this graph
	 */
	private String className;

	/**
	 * the identifier of the graph
	 */
	private String graphID;
	private String fileName;

	/**
	 * the attributes of the node
	 */
	private Hashtable<String, GXLValue> attributes;

	/**
	 * directed or undirected edges
	 */
	private boolean directed;

	/**
	 * the adjacency-matrix of the graph
	 */
	private Edge[][] adjacencyMatrix;

	/**
	 * Constructors
	 */
	public Graph(int n) {
		super();
		this.adjacencyMatrix = new Edge[n][n];
		this.attributes = new Hashtable<>();
	}

	public Graph() {
		super();
		this.attributes = new Hashtable<>();
	}

	/**
	 * puts a new attribute
	 * in the attribute-table
	 */
	public void put(String key, GXLValue value) {
		this.attributes.put(key, value);
	}

	/**
	 * @param key
	 * @return the attribute-value of
	 */
	public GXLValue getValue(String key) {
		return this.attributes.get(key);
	}

	public void setBoolean(String key, Boolean value) {
		GXLBool gxlBool = new GXLBool(value);
		this.attributes.put(key, gxlBool);
	}

	public boolean getBoolean(String key) {
		GXLBool gxlBool = (GXLBool) this.attributes.get(key);
		return gxlBool.getBooleanValue();
	}

	public void setFloat(String key, Float value) {
		GXLFloat gxlFloat = new GXLFloat(value);
		this.attributes.put(key, gxlFloat);
	}

	public float getFloat(String key) {
		GXLFloat gxlFloat = (GXLFloat) this.attributes.get(key);
		return gxlFloat.getFloatValue();
	}

	public void setDouble(String key, Double value) {
		GXLFloat gxlFloat = new GXLFloat(value.floatValue());
		this.attributes.put(key, gxlFloat);
	}

	public double getDouble(String key) {
		return this.getFloat(key);
	}

	public void setInt(String key, Integer value) {
		GXLInt gxlInt = new GXLInt(value);
		this.attributes.put(key, gxlInt);
	}

	public int getInt(String key) {
		GXLInt gxlFloat = (GXLInt) this.attributes.get(key);
		return gxlFloat.getIntValue();
	}

	public void setString(String key, String value) {
		GXLString gxlString = new GXLString(value);
		this.attributes.put(key, gxlString);
	}

	public String getString(String key) {
		GXLString gxlString = (GXLString) this.attributes.get(key);
		return gxlString.getValue();
	}

	/**
	 * generates a printable string of the graph
	 */
	public String toString() {
		String graph = "*** Graph: " + this.graphID + " ***\n";
		graph += "Class: " + this.className + "\n";
		graph += "Nodes:\n";
		Iterator<Node> iter = this.iterator();
		while (iter.hasNext()) {
			Node node = iter.next();
			graph += node.toString();
			graph += "\n";
		}
		graph += "\n";
		graph += "Edges of...\n";
		iter = this.iterator();
		while (iter.hasNext()) {
			Node node = iter.next();
			graph += "... Node: " + node.getNodeID() + ": ";
			Iterator<Edge> edgeIter = node.getEdges().iterator();
			while (edgeIter.hasNext()) {
				Edge edge = edgeIter.next();
				graph += edge.getEdgeID() + "\t";
			}
			graph += "\n";
		}
		graph += "\n";
		graph += "Adjacency Matrix:\n";
		for (int i = 0; i < this.adjacencyMatrix.length; i++) {
			for (int j = 0; j < this.adjacencyMatrix.length; j++) {
				if (this.adjacencyMatrix[i][j] != null) {
					graph += "1";
				} else {
					graph += "0";
				}

				graph += "\t";
			}
			graph += "\n";
		}
		graph += "\n*** *** *** *** *** *** *** *** *** *** *** *** *** ***\n";
		return graph;
	}

	public Edge adjEdge(int i, int j) {
		if (adjacencyMatrix[i][j] != null) {
			return adjacencyMatrix[i][j];
		} else {
			return adjacencyMatrix[j][i];
		}
	}

	public int adjEdgeDegree(int idx) {
		int deg = 0;
		for (int i = 0; i < adjacencyMatrix.length; i++) {
			if (adjacencyMatrix[idx][i] != null) {
				deg++;
			}
			if (directed && adjacencyMatrix[i][idx] != null) {
				deg++;
			}
		}
		return deg;
	}

	public LinkedList<Edge> adjEdges(int idx) {
		LinkedList<Edge> edges = new LinkedList<Edge>();
		for (int i = 0; i < adjacencyMatrix.length; i++) {
			if (adjacencyMatrix[idx][i] != null) {
				edges.add(adjacencyMatrix[idx][i]);
			}
			if (directed && adjacencyMatrix[i][idx] != null) {
				edges.add(adjacencyMatrix[i][idx]);
			}
		}
		return edges;
	}

	public LinkedList<Integer> adjNeighborIdxs(int idx) {
		LinkedList<Integer> neighbors = new LinkedList<Integer>();
		for (int i = 0; i < adjacencyMatrix.length; i++) {
			if (adjacencyMatrix[idx][i] != null) {
				neighbors.add(i);
			}
			if (directed && adjacencyMatrix[i][idx] != null) {
				neighbors.add(i);
			}
		}
		return neighbors;
	}

	public LinkedList<Edge> getOutgoingEdges(int idx) {
		LinkedList<Edge> edges = new LinkedList<Edge>();
		for (int i = 0; i < adjacencyMatrix.length; i++) {
			if (adjacencyMatrix[idx][i] != null) {
				edges.add(adjacencyMatrix[idx][i]);
			}
		}
		return edges;
	}

	public LinkedList<Edge> getIncomingEdges(int idx) {
		LinkedList<Edge> edges = new LinkedList<Edge>();
		for (int i = 0; i < adjacencyMatrix.length; i++) {
			if (adjacencyMatrix[i][idx] != null) {
				edges.add(adjacencyMatrix[i][idx]);
			}
		}
		return edges;
	}

	public Edge getEdge(int idx1, int idx2) {
		return adjacencyMatrix[idx1][idx2];
	}

	public int[] getOutgoingEdgeEndings(int idx1) {
		return getEdgeEndings(idx1, true);
	}

	public int[] getIncomingEdgeEndings(int idx1) {
		return getEdgeEndings(idx1, false);
	}

	public int[] getEdgeEndings(int idx1, boolean isOutgoing) {
		LinkedList<Integer> list = new LinkedList<Integer>();
		for (int i = 0; i < adjacencyMatrix.length; i++) {
			if (isOutgoing && adjacencyMatrix[idx1][i] != null) {
				list.add(i);
			} else if (!isOutgoing && adjacencyMatrix[i][idx1] != null) {
				list.add(i);
			}
		}
		int[] idxs = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			idxs[i] = list.get(i);
		}
		return idxs;
	}

	public Edge getEdge(int idx1, int idx2, boolean isOutgoing) {
		if (isOutgoing) {
			return adjacencyMatrix[idx1][idx2];
		}
		return adjacencyMatrix[idx2][idx1];
	}

	/**
	 * getters and setters
	 */

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getGraphID() {
		return graphID;
	}

	public void setGraphID(String graphID) {
		this.graphID = graphID;
	}

	public boolean isDirected() {
		return directed;
	}

	public void setDirected(boolean directed) {
		this.directed = directed;
	}

	public Edge[][] getAdjacencyMatrix() {
		return adjacencyMatrix;
	}

	public void setAdjacencyMatrix(Edge[][] edges) {
		this.adjacencyMatrix = edges;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public Hashtable<String, GXLValue> getAttributes() {
		return attributes;
	}

	public void setAttributes(Hashtable<String, GXLValue> attributes) {
		this.attributes = attributes;
	}

	public Graph deepCopy() {

		Graph copy = new Graph();
		copy.setGraphID(new String(this.graphID));

		for (Map.Entry<String, GXLValue> entry : this.attributes.entrySet()) {
			copy.put(new String(entry.getKey()), copyValue(entry.getValue()));
		}

		// Copy nodes
		HashMap<Node, Node> nodeMap = new HashMap<>();
		for (Node node : this) {

			Node copyNode = new Node(new String(node.getNodeID()));
			nodeMap.put(node, copyNode);

			copyNode.setGraph(copy);

			copy.add(copyNode);

			// Add attributes
			for (Map.Entry<String, GXLValue> entry : node.getAttributes().entrySet()) {
				copyNode.put(entry.getKey(), copyValue(entry.getValue()));
			}
		}

		// Copy edges
		Edge[][] copyEdges = new Edge[copy.size()][copy.size()];
		copy.setAdjacencyMatrix(copyEdges);

		HashMap<String, Edge> edges = new HashMap<>();

		for (Node node : this) {

			for (Edge edge : node.getEdges()) {

				Node startNode = nodeMap.get(edge.getStartNode());
				Node endNode = nodeMap.get(edge.getEndNode());

				String edgeID = startNode.getNodeID() + "_" + endNode.getNodeID();

				if (!edges.containsKey(edgeID)) {

					Edge copyEdge = new Edge();
					copyEdge.setStartNode(startNode);
					copyEdge.setEndNode(endNode);

					startNode.getEdges().add(copyEdge);
					endNode.getEdges().add(copyEdge);

					// Add attributes
					for (Map.Entry<String, GXLValue> entry : edge.getAttributes().entrySet()) {
						copyEdge.put(entry.getKey(), copyValue(entry.getValue()));
					}

					edges.put(edgeID, copyEdge);
				}
			}
		}

		for (int s = 0; s < copy.size(); s++) {

			Node startNode = copy.get(s);

			for (int t = 0; t < copy.size(); t++) {

				Node endNode = copy.get(t);

				if (this.adjacencyMatrix[s][t] != null) {

					String edgeID = startNode.getNodeID() + "_" + endNode.getNodeID();

					if (!this.isDirected() && !edges.containsKey(edgeID)) {
						edgeID = endNode.getNodeID() + "_" + startNode.getNodeID();
					}

					Edge edge = edges.get(edgeID);

					copyEdges[s][t] = edge;
				}
			}
		}

		return copy;
	}

	public ArrayList<Graph> extractWindowsNodeCenter(Node centerNode, double[][] windowMaxDistances) {

		double centerNodeX = centerNode.getDouble("x");
		double centerNodeY = centerNode.getDouble("y");

		// accessing windows
		ArrayList<Graph> windows = new ArrayList<>();

		// copy nodes

		// correspondences of nodes in page graph & in window
		ArrayList<HashMap<Node, Node>> nodeMaps = new ArrayList<>();

		// create windows and maps
		for (int k = 0; k < windowMaxDistances.length; k++) {
			Graph window = new Graph();
			window.setGraphID(new String(centerNode.getNodeID() + "_w" + k));
			windows.add(window);

			HashMap<Node, Node> nodeMap = new HashMap<>();
			nodeMaps.add(nodeMap);
		}

		// iterate through nodes only once to fill all windows
		for (Node pageNode : this) {
			for (int k = 0; k < windowMaxDistances.length; k++) {
				Graph window = windows.get(k);

				double pageNodeX = pageNode.getDouble("x");
				double pageNodeY = pageNode.getDouble("y");
				double distX = Math.abs(centerNodeX - pageNodeX);
				double distY = Math.abs(centerNodeY - pageNodeY);

				// check if node in window
				if (distX <= windowMaxDistances[k][0] && distY <= windowMaxDistances[k][1]) {

					Node copyNode = new Node(new String(pageNode.getNodeID()+"_w"+k));
					nodeMaps.get(k).put(pageNode, copyNode);

					copyNode.setGraph(window);

					window.add(copyNode);


					// Add attributes
					for (Map.Entry<String, GXLValue> entry : pageNode.getAttributes().entrySet()) {
						copyNode.put(entry.getKey(), copyValue(entry.getValue()));
					}

				}
			}
		}



		//copy edges

		// access to edges via id
		ArrayList<HashMap<String, Edge>> edgesList = new ArrayList<>();
		// adjacency matrix reference
		ArrayList<Edge[][]> copyEdgesList = new ArrayList<>();

		for (int k = 0; k < windowMaxDistances.length; k++) {
			HashMap<String, Edge> edges = new HashMap<>();
			edgesList.add(edges);

			Graph window = windows.get(k);
			Edge[][] copyEdges = new Edge[window.size()][window.size()];
			copyEdgesList.add(copyEdges);
			window.setAdjacencyMatrix(copyEdges);

			HashMap<Node, Node> nodeMap = nodeMaps.get(k);

			// only search nodes in window
			for (Map.Entry<Node, Node> entry : nodeMap.entrySet()) {
				Node pageNode = entry.getKey();
				Node windowNode = entry.getValue();

				// all edges from node
				for (Edge edge : pageNode.getEdges()) {
					Node startPageNode = edge.getStartNode();
					Node endPageNode = edge.getEndNode();

					// check that edge has both nodes in window
					if (nodeMap.containsKey(startPageNode) && nodeMap.containsKey(endPageNode)) {
						Node startWindowNode = nodeMap.get(startPageNode);
						Node endWindowNode = nodeMap.get(endPageNode);
						String edgeID = startWindowNode.getNodeID() + "_" + endWindowNode.getNodeID();

						if (!edges.containsKey(edgeID)) {

							Edge copyEdge = new Edge();
							copyEdge.setStartNode(startWindowNode);
							copyEdge.setEndNode(endWindowNode);

							startWindowNode.getEdges().add(copyEdge);
							endWindowNode.getEdges().add(copyEdge);

//							// Add edge attributes
//							for (Map.Entry<String, GXLValue> entry2 : edge.getAttributes().entrySet()) {
//								copyEdge.put(entry2.getKey(), copyValue(entry2.getValue()));
//							}

							edges.put(edgeID, copyEdge);
						}
					}
				}
			}

			// fill adjacency matrix
			for (int s = 0; s < window.size(); s++) {
				Node startNode = window.get(s);

				for (int t = 0; t < window.size(); t++) {
					Node endNode = window.get(t);

					if (this.adjacencyMatrix[s][t] != null) {
						String edgeID = startNode.getNodeID()+"_"+endNode.getNodeID();

						if (!this.isDirected() && !edges.containsKey(edgeID)){
							edgeID = endNode.getNodeID()+"_"+startNode.getNodeID();
						}

						Edge edge = edges.get(edgeID);

						copyEdges[s][t] = edge;
					}
				}
			}

			//compute means and standard deviation
			double xMean = 0;
			double yMean = 0;
			for (int i = 0; i < window.size(); i++) {
				double xNode = window.get(i).getDouble("x");
				xMean += xNode;
				double yNode = window.get(i).getDouble("y");
				yMean += yNode;
			}
			xMean /= window.size();
			yMean /= window.size();

			// standard dev: square root of mean of squared distances to mean val
			double xSumSqDist = 0;
			double ySumSqDist = 0;
			for (int i = 0; i < window.size(); i++) {
				double xNode = window.get(i).getDouble("x");
				xSumSqDist += Math.pow(xNode - xMean, 2);
				double yNode = window.get(i).getDouble("y");
				ySumSqDist += Math.pow(yNode - yMean, 2);
			}

			double xStDev = Math.sqrt(xSumSqDist / window.size());
			double yStDev = Math.sqrt(ySumSqDist / window.size());

			// normalize coords
			for (int i = 0; i < window.size(); i++) {
				double xNode = window.get(i).getDouble("x");
				if (xStDev != 0) {
					window.get(i).setDouble("x", (xNode - xMean) / xStDev);
				}
				double yNode = window.get(i).getDouble("y");
				if (yStDev != 0) {
					window.get(i).setDouble("y", (yNode - yMean) / yStDev);
				}
			}

			//set graph attributes
			window.setDouble("x_mean", xMean);
			window.setDouble("y_mean", yMean);
			window.setDouble("x_std", xStDev);
			window.setDouble("y_std", yStDev);


		}

		return windows;
	}

	public ArrayList<Graph> extractWindowsCenterCoords(double[] coords, double[][] windowMaxDistances) {

		double centerX = coords[0];
		double centerY = coords[1];

		// accessing windows
		ArrayList<Graph> windows = new ArrayList<>();

		// copy nodes

		// correspondences of nodes in page graph & in window
		ArrayList<HashMap<Node, Node>> nodeMaps = new ArrayList<>();

		// create windows and maps
		for (int k = 0; k < windowMaxDistances.length; k++) {
			Graph window = new Graph();
			window.setGraphID(new String(this.graphID + "_" + centerX + "_" + centerY + "_w" + k));
			windows.add(window);

			HashMap<Node, Node> nodeMap = new HashMap<>();
			nodeMaps.add(nodeMap);
		}

		// iterate through nodes only once to fill all windows
		for (Node pageNode : this) {
			for (int k = 0; k < windowMaxDistances.length; k++) {
				Graph window = windows.get(k);

				double pageNodeX = pageNode.getDouble("x");
				double pageNodeY = pageNode.getDouble("y");
				double distX = Math.abs(centerX - pageNodeX);
				double distY = Math.abs(centerY - pageNodeY);

				// check if node in window
				if (distX <= windowMaxDistances[k][0] && distY <= windowMaxDistances[k][1]) {

					Node copyNode = new Node(new String(pageNode.getNodeID()+"_w"+k));
					nodeMaps.get(k).put(pageNode, copyNode);

					copyNode.setGraph(window);

					window.add(copyNode);


					// Add attributes
					for (Map.Entry<String, GXLValue> entry : pageNode.getAttributes().entrySet()) {
						copyNode.put(entry.getKey(), copyValue(entry.getValue()));
					}

				}
			}
		}



		//copy edges

		// access to edges via id
		ArrayList<HashMap<String, Edge>> edgesList = new ArrayList<>();
		// adjacency matrix reference
		ArrayList<Edge[][]> copyEdgesList = new ArrayList<>();

		for (int k = 0; k < windowMaxDistances.length; k++) {
			HashMap<String, Edge> edges = new HashMap<>();
			edgesList.add(edges);

			Graph window = windows.get(k);
			Edge[][] copyEdges = new Edge[window.size()][window.size()];
			copyEdgesList.add(copyEdges);
			window.setAdjacencyMatrix(copyEdges);

			HashMap<Node, Node> nodeMap = nodeMaps.get(k);

			// only search nodes in window
			for (Map.Entry<Node, Node> entry : nodeMap.entrySet()) {
				Node pageNode = entry.getKey();
				Node windowNode = entry.getValue();

				// all edges from node
				for (Edge edge : pageNode.getEdges()) {
					Node startPageNode = edge.getStartNode();
					Node endPageNode = edge.getEndNode();

					// check that edge has both nodes in window
					if (nodeMap.containsKey(startPageNode) && nodeMap.containsKey(endPageNode)) {
						Node startWindowNode = nodeMap.get(startPageNode);
						Node endWindowNode = nodeMap.get(endPageNode);
						String edgeID = startWindowNode.getNodeID() + "_" + endWindowNode.getNodeID();

						if (!edges.containsKey(edgeID)) {

							Edge copyEdge = new Edge();
							copyEdge.setStartNode(startWindowNode);
							copyEdge.setEndNode(endWindowNode);

							startWindowNode.getEdges().add(copyEdge);
							endWindowNode.getEdges().add(copyEdge);

//							// Add edge attributes
//							for (Map.Entry<String, GXLValue> entry2 : edge.getAttributes().entrySet()) {
//								copyEdge.put(entry2.getKey(), copyValue(entry2.getValue()));
//							}

							edges.put(edgeID, copyEdge);
						}
					}
				}
			}

			// fill adjacency matrix
			for (int s = 0; s < window.size(); s++) {
				Node startNode = window.get(s);

				for (int t = 0; t < window.size(); t++) {
					Node endNode = window.get(t);

					if (this.adjacencyMatrix[s][t] != null) {
						String edgeID = startNode.getNodeID()+"_"+endNode.getNodeID();

						if (!this.isDirected() && !edges.containsKey(edgeID)){
							edgeID = endNode.getNodeID()+"_"+startNode.getNodeID();
						}

						Edge edge = edges.get(edgeID);

						copyEdges[s][t] = edge;
					}
				}
			}

			//compute means and standard deviation
			double xMean = 0;
			double yMean = 0;
			for (int i = 0; i < window.size(); i++) {
				double xNode = window.get(i).getDouble("x");
				xMean += xNode;
				double yNode = window.get(i).getDouble("y");
				yMean += yNode;
			}
			xMean /= window.size();
			yMean /= window.size();

			// standard dev: square root of mean of squared distances to mean val
			double xSumSqDist = 0;
			double ySumSqDist = 0;
			for (int i = 0; i < window.size(); i++) {
				double xNode = window.get(i).getDouble("x");
				xSumSqDist += Math.pow(xNode - xMean, 2);
				double yNode = window.get(i).getDouble("y");
				ySumSqDist += Math.pow(yNode - yMean, 2);
			}

			double xStDev = Math.sqrt(xSumSqDist / window.size());
			double yStDev = Math.sqrt(ySumSqDist / window.size());

			// normalize coords
			for (int i = 0; i < window.size(); i++) {
				double xNode = window.get(i).getDouble("x");
				if (xStDev != 0) {
					window.get(i).setDouble("x", (xNode - xMean) / xStDev);
				}
				double yNode = window.get(i).getDouble("y");
				if (yStDev != 0) {
					window.get(i).setDouble("y", (yNode - yMean) / yStDev);
				}
			}
			double xMeanNorm = 0;
			double yMeanNorm = 0;
			for (int i = 0; i < window.size(); i++) {
				double xNode = window.get(i).getDouble("x");
				xMeanNorm += xNode;
				double yNode = window.get(i).getDouble("y");
				yMeanNorm += yNode;
			}


			//set graph attributes
			window.setDouble("x_mean", xMean);
			window.setDouble("y_mean", yMean);
			window.setDouble("x_std", xStDev);
			window.setDouble("y_std", yStDev);


		}

		return windows;
	}

	public ArrayList<Graph> extractWindowsCornerCoords(double[] coords, double[][] windowMaxSides) {

		double corner0X = Math.round(coords[0] * 1000.0) / 1000.0;
		double corner0Y = Math.round(coords[1] * 1000.0) / 1000.0;

		double xMeanTarget = this.getDouble("x_mean");
		double yMeanTarget = this.getDouble("y_mean");
		double xStdTarget = this.getDouble("x_std");
		double yStdTarget = this.getDouble("y_std");


		// accessing windows
		ArrayList<Graph> windows = new ArrayList<>();

		// copy nodes

		// correspondences of nodes in page graph & in window
		ArrayList<HashMap<Node, Node>> nodeMaps = new ArrayList<>();

		// create windows and maps
		for (int k = 0; k < windowMaxSides.length; k++) {
			Graph window = new Graph();
			window.setGraphID(new String(this.graphID + "_" + corner0X + "_" + corner0Y + "_w" + k));
			windows.add(window);

			HashMap<Node, Node> nodeMap = new HashMap<>();
			nodeMaps.add(nodeMap);
		}

		// iterate through nodes only once to fill all windows
		for (Node pageNode : this) {
			double pageNodeX = Math.round(pageNode.getDouble("x") * 1000.0) / 1000.0;
			double pageNodeY = Math.round(pageNode.getDouble("y") * 1000.0) / 1000.0;

			for (int k = 0; k < windowMaxSides.length; k++) {
				Graph window = windows.get(k);

				double sideX = Math.round(windowMaxSides[k][0] * 1000.0) / 1000.0;
				double sideY = Math.round(windowMaxSides[k][1] * 1000.0) / 1000.0;
				double corner1X = corner0X + sideX;
				double corner1Y = corner0Y + sideY;
				// check if node in window
				if (pageNodeX >= corner0X && pageNodeX <= corner1X && pageNodeY >= corner0Y && pageNodeY <= corner1Y) {
					Node copyNode = new Node(new String(pageNode.getNodeID()+"_w"+k));
					nodeMaps.get(k).put(pageNode, copyNode);

					copyNode.setGraph(window);

					window.add(copyNode);

					// Add attributes
					for (Map.Entry<String, GXLValue> entry : pageNode.getAttributes().entrySet()) {
						copyNode.put(entry.getKey(), copyValue(entry.getValue()));
					}

				}

			}
		}

		//copy edges

		// access to edges via id
		ArrayList<HashMap<String, Edge>> edgesList = new ArrayList<>();
		// adjacency matrix reference
		ArrayList<Edge[][]> copyEdgesList = new ArrayList<>();

		for (int k = 0; k < windowMaxSides.length; k++) {
			HashMap<String, Edge> edges = new HashMap<>();
			edgesList.add(edges);

			Graph window = windows.get(k);
			Edge[][] copyEdges = new Edge[window.size()][window.size()];
			copyEdgesList.add(copyEdges);
			window.setAdjacencyMatrix(copyEdges);

			HashMap<Node, Node> nodeMap = nodeMaps.get(k);

			// only search nodes in window
			for (Map.Entry<Node, Node> entry : nodeMap.entrySet()) {
				Node pageNode = entry.getKey();
				Node windowNode = entry.getValue();

				// all edges from node
				for (Edge edge : pageNode.getEdges()) {
					Node startPageNode = edge.getStartNode();
					Node endPageNode = edge.getEndNode();

					// check that edge has both nodes in window
					if (nodeMap.containsKey(startPageNode) && nodeMap.containsKey(endPageNode)) {
						Node startWindowNode = nodeMap.get(startPageNode);
						Node endWindowNode = nodeMap.get(endPageNode);
						String edgeID = startWindowNode.getNodeID() + "_" + endWindowNode.getNodeID();

						if (!edges.containsKey(edgeID)) {

							Edge copyEdge = new Edge();
							copyEdge.setStartNode(startWindowNode);
							copyEdge.setEndNode(endWindowNode);

							startWindowNode.getEdges().add(copyEdge);
							endWindowNode.getEdges().add(copyEdge);

//							// Add edge attributes
//							for (Map.Entry<String, GXLValue> entry2 : edge.getAttributes().entrySet()) {
//								copyEdge.put(entry2.getKey(), copyValue(entry2.getValue()));
//							}

							edges.put(edgeID, copyEdge);
						}
					}
				}
			}

			// fill adjacency matrix
			for (int s = 0; s < window.size(); s++) {
				Node startNode = window.get(s);

				for (int t = 0; t < window.size(); t++) {
					Node endNode = window.get(t);

					if (this.adjacencyMatrix[s][t] != null) {
						String edgeID = startNode.getNodeID()+"_"+endNode.getNodeID();

						if (!this.isDirected() && !edges.containsKey(edgeID)){
							edgeID = endNode.getNodeID()+"_"+startNode.getNodeID();
						}

						Edge edge = edges.get(edgeID);

						copyEdges[s][t] = edge;
					}
				}
			}

			// compute means and standard deviation
			// un-normalize coords to get "true" mean/std, so we can go back to 1:1 image
			// else we'd go back to targetGraph values
			double xMean = 0;
			double yMean = 0;
			for (int i = 0; i < window.size(); i++) {
				double xNode = window.get(i).getDouble("x") * xStdTarget + xMeanTarget ;
				xMean += xNode;
				double yNode = window.get(i).getDouble("y") * yStdTarget + yMeanTarget;
				yMean += yNode;
			}
			xMean /= window.size();
			yMean /= window.size();

			// standard dev: square root of mean of squared distances to mean val
			double xSumSqDist = 0;
			double ySumSqDist = 0;
			for (int i = 0; i < window.size(); i++) {
				double xNode = window.get(i).getDouble("x") * xStdTarget + xMeanTarget;
				xSumSqDist += Math.pow(xNode - xMean, 2);
				double yNode = window.get(i).getDouble("y") * yStdTarget + yMeanTarget;
				ySumSqDist += Math.pow(yNode - yMean, 2);
			}

			double xStDev = Math.sqrt(xSumSqDist / window.size());
			double yStDev = Math.sqrt(ySumSqDist / window.size());

			// normalize coords to get mean/std (0,1)
			for (int i = 0; i < window.size(); i++) {
				double xNode = window.get(i).getDouble("x") * xStdTarget + xMeanTarget;
				if (xStDev != 0) {
					window.get(i).setDouble("x", (xNode - xMean) / xStDev);
				}
				double yNode = window.get(i).getDouble("y") * yStdTarget + yMeanTarget;
				if (yStDev != 0) {
					window.get(i).setDouble("y", (yNode - yMean) / yStDev);
				}
			}

			//set graph attributes
			window.setDouble("x_mean", xMean);
			window.setDouble("y_mean", yMean);
			window.setDouble("x_std", xStDev);
			window.setDouble("y_std", yStDev);


		}

		return windows;
	}

	private GXLValue copyValue(GXLValue gxlValue){
		if(gxlValue.getClass() == GXLBool.class){
			GXLBool gxlBool 	= (GXLBool) gxlValue;
			boolean value 		= new Boolean(gxlBool.getBooleanValue());
			return new GXLBool(value);
		} else if(gxlValue.getClass() == GXLFloat.class){
			GXLFloat gxlFloat 	= (GXLFloat) gxlValue;
			float value 		= new Float(gxlFloat.getFloatValue());
			return new GXLFloat(value);
		} else if(gxlValue.getClass() == GXLInt.class){
			GXLInt gxlInt 		= (GXLInt) gxlValue;
			int value 			= new Integer(gxlInt.getIntValue());
			return new GXLInt(value);
		} else if(gxlValue.getClass() == GXLString.class){
			GXLString gxlString	= (GXLString) gxlValue;
			String value 		= new String(gxlString.getValue());
			return new GXLString(value);
		} else {
			return null;
		}
	}

	public BufferedImage displayGraph(int W, int H) {
		double xMean = this.getDouble("x_mean");
		double yMean = this.getDouble("y_mean");
		double xStd = this.getDouble("x_std");
		double yStd = this.getDouble("y_std");
		int xMax = 0;
		int yMax = 0;
		for (Node node: this){
			int nodeX = (int) Math.round(node.getDouble("x") * xStd + xMean);
			xMax = Math.max(xMax, nodeX);
			int nodeY = (int) Math.round(node.getDouble("y") * yStd + yMean);
			yMax = Math.max(yMax, nodeY);
		}
		BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
		Graphics g = (Graphics2D) img.getGraphics();
		g.setColor(Color.CYAN);
		g.fillRect(0,0,img.getWidth(), img.getHeight());
		g.setColor(Color.BLACK);
		for (Node node : this) {
			int nodeX = (int) Math.round(node.getDouble("x") * xStd + xMean);
			int nodeY = (int) Math.round(node.getDouble("y") * yStd + yMean);
			g.fillRect(nodeX + 1, nodeY + 1, 1, 1);
		}

		return img;

	}
}
