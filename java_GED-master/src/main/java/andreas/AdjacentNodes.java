package andreas;

import java.util.ArrayList;

import graph.Edge;
import graph.Graph;

public class AdjacentNodes {

	private int[] adjacentNodes;
	private boolean[] isOutgoing;
	
	public AdjacentNodes(Graph g, int i) {
		Edge[][] a = g.getAdjacencyMatrix();
		ArrayList<Integer> nodes = new ArrayList<Integer>();
		ArrayList<Boolean> outgoing = new ArrayList<Boolean>();
		for (int j=0; j < a.length; j++) {
			if (a[i][j] != null) {
				nodes.add(j);
				outgoing.add(true);
			}
		}
		if (g.isDirected()) {
			for (int j=0; j < a.length; j++) {
				if (i != j && a[j][i] != null) {
					nodes.add(j);
					outgoing.add(false);
				}
			}
		}
		adjacentNodes = new int[nodes.size()];
		isOutgoing = new boolean[nodes.size()];
		for (int j=0; j < nodes.size(); j++) {
			adjacentNodes[j] = nodes.get(j);
			isOutgoing[j] = outgoing.get(j);
		}
	}
	
	public int size() {
		return adjacentNodes.length;
	}
	
	public int getAdjacentNode(int i) {
		return adjacentNodes[i];
	}
	
	public boolean isOutgoing(int i) {
		return isOutgoing[i];
	}
	
	public boolean contains(int i) {
		for (int j=0; j < adjacentNodes.length; j++) {
			if (adjacentNodes[j] == i) {
				return true;
			}
		}
		return false;
	}
	
}
