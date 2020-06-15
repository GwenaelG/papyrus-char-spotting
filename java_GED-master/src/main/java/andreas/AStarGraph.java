package andreas;

import java.util.Iterator;

import graph.Edge;
import graph.Graph;
import graph.Node;

public class AStarGraph {

	private Graph graph;
	private Node[] nodes;
	private Edge[][] edges;
	private AdjacentNodes[] adjacentNodes;
	
	public AStarGraph(Graph theGraph) {
		graph = theGraph;
		nodes = new Node[graph.size()];
		int i = 0;
		Iterator<Node> iter = graph.iterator();
		while (iter.hasNext()) {
			nodes[i] = iter.next();
			i++;
		}
		edges = graph.getAdjacencyMatrix();
		adjacentNodes = new AdjacentNodes[nodes.length];
		for (int k=0; k < nodes.length; k++) {
			adjacentNodes[k] = new AdjacentNodes(graph, k);
		}
	}
	
	public Graph getGraph() {
		return graph;
	}
	
	public int size() {
		return nodes.length;
	}
	
	public Node getNode(int i) {
		return nodes[i];
	}
	
	public int sizeAdjacent(int i) {
		return adjacentNodes[i].size();
	}
	
	public AdjacentNodes getAdjacentNodes(int i) {
		return adjacentNodes[i];
	}
	
	public boolean hasEdge(int i, int j, boolean isOutgoing) {
		if (isOutgoing) {
			return (edges[i][j] != null);
		} else {
			return (edges[j][i] != null);
		}
	}
	
	public Edge getEdge(int i, int j, boolean isOutgoing) {
		if (isOutgoing) {
			return edges[i][j];
		} else {
			return edges[j][i];
		}
	}
	
}
