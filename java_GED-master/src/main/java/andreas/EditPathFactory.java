package andreas;

import java.util.Iterator;
import java.util.LinkedList;

import costs.CostFunctionManager;
import graph.Edge;
import graph.Graph;
import graph.Node;
import graph.TreeNode;

public class EditPathFactory {

	private Graph g1;
	private Graph g2;
	private Edge[][] a1;
	private Edge[][] a2;
	private int[] map12;
	private int[] map21;
	private CostFunctionManager cf;
	private double factor;
	
	public EditPath fromAStar(TreeNode tn) {
		this.g1 = tn.getG1();
		this.g2 = tn.getG2();
		this.map12 = tn.getMap12();
		this.map21 = tn.getMap21();
		this.cf = tn.getCf();
		return createEditPath();
	}
	
	public EditPath fromAStarGED(Graph g1, Graph g2, CostFunctionManager cf, AStarMap map) {
		this.g1 = g1;
		this.g2 = g2;
		this.map12 = map.getMap12();
		this.map21 = map.getMap21();
		this.cf = cf;
		return createEditPath();
	}
	
	public EditPath fromBipartite(Graph g1, Graph g2, int[][] matching, CostFunctionManager cf) {
		this.g1 = g1;
		this.g2 = g2;
		this.cf = cf;
		map12 = new int[g1.size()]; for(int i=0;i<g1.size();i++){map12[i] = -1;}
		map21 = new int[g2.size()]; for(int i=0;i<g2.size();i++){map21[i] = -1;}
		for (int i=0; i < matching.length; i++) {
			if (matching[i][0] < g1.size() && matching[i][1] < g2.size()) {
				map12[matching[i][0]] = matching[i][1];
				map21[matching[i][1]] = matching[i][0];
			}
		}
		return createEditPath();
	}
	
	public EditPath fromHausdorffMultipleAssignments(Graph g1, Graph g2, int[] map12, int[] map21, CostFunctionManager cf) {
		this.g1 = g1;
		this.g2 = g2;
		this.map12 = map12;
		this.map21 = map21;
		this.cf = cf;
		return createDirectedEditPath();
	}
	
	public EditPath fromHausdorffUniqueAssignments(Graph g1, Graph g2, int[] map12, int[] map21, CostFunctionManager cf) {
		this.g1 = g1;
		this.g2 = g2;
		this.map12 = map12;
		this.map21 = map21;
		this.cf = cf;
		return createEditPath();
	}
	
	private EditPath createEditPath() {
		EditPath editPath = new EditPath(g1, g2, false);
		factor = 1.0;
		if (!g1.isDirected()) {
			factor = 0.5;
		}
		a1 = g1.getAdjacenyMatrix();
		a2 = g2.getAdjacenyMatrix();
		for (int i=0; i < map12.length; i++) {
			if (map12[i] >= 0) { // substitutions
				int j = map12[i];
				Node n1 = g1.get(i);
				Node n2 = g2.get(j);
				// TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
				double nodeCost = this.cf.getCost(n1, n2);
				double edgeCost = getEdgeCost1(i, j);
				EditOperation eo = new EditOperation(0, n1, n2, nodeCost, edgeCost);
				editPath.addEditOperation(eo);
			} else { // deletions
				Node n1 = g1.get(i);
				double nodeCost = this.cf.getNodeCosts();
				double edgeCost = getNodeEdges(a1, i).size() * this.cf.getEdgeCosts() * factor * 0.5;
				EditOperation eo = new EditOperation(1, n1, n1, nodeCost, edgeCost);
				editPath.addEditOperation(eo);
			}
		}
		for (int j=0; j < map21.length; j++) {
			if (map21[j] < 0) { // insertions
				Node n2 = g2.get(j);
				double nodeCost = this.cf.getNodeCosts();
				double edgeCost = getNodeEdges(a2, j).size() * this.cf.getEdgeCosts() * factor * 0.5;
				EditOperation eo = new EditOperation(2, n2, n2, nodeCost, edgeCost);
				editPath.addEditOperation(eo);
			}
		}
		return editPath;
	}
	
	private EditPath createDirectedEditPath() {
		EditPath editPath = new EditPath(g1, g2, true);
		factor = 1.0;
		if (!g1.isDirected()) {
			factor = 0.5;
		}
		a1 = g1.getAdjacenyMatrix();
		a2 = g2.getAdjacenyMatrix();
		for (int i=0; i < map12.length; i++) {
			if (map12[i] >= 0) { // substitutions 1 => 2
				int j = map12[i];
				Node n1 = g1.get(i);
				Node n2 = g2.get(j);
				// TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
				double nodeCost = this.cf.getCost(n1, n2);
				double edgeCost = getEdgeCost1(i, j);
				EditOperation eo = new EditOperation(EditOperation.SAB, n1, n2, nodeCost/2, edgeCost/2);
				editPath.addEditOperation(eo);
			} else { // deletions
				Node n1 = g1.get(i);
				double nodeCost = this.cf.getNodeCosts();
				double edgeCost = getNodeEdges(a1, i).size() * this.cf.getEdgeCosts() * factor;
				EditOperation eo = new EditOperation(EditOperation.DEL, n1, n1, nodeCost, edgeCost);
				editPath.addEditOperation(eo);
			}
		}
		for (int j=0; j < map21.length; j++) {
			if (map21[j] >= 0) { // substitutions 2 => 1
				int i = map21[j];
				Node n2 = g2.get(j);
				Node n1 = g1.get(i);
				// TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
				double nodeCost = this.cf.getCost(n2, n1);
				double edgeCost = getEdgeCost2(i, j);
				EditOperation eo = new EditOperation(EditOperation.SBA, n2, n1, nodeCost/2, edgeCost/2);
				editPath.addEditOperation(eo);
			} else { // insertions
				Node n2 = g2.get(j);
				double nodeCost = this.cf.getNodeCosts();
				double edgeCost = getNodeEdges(a2, j).size() * this.cf.getEdgeCosts() * factor;
				EditOperation eo = new EditOperation(EditOperation.INS, n2, n2, nodeCost, edgeCost);
				editPath.addEditOperation(eo);
			}
		}
		return editPath;
	}
	
	private double getEdgeCost1(int i, int j) {
		double cost = 0;
		Iterator<Edge> iter = getNodeEdges(a1,i).iterator();
		while (iter.hasNext()) {
			Edge edge1 = iter.next();
			if (hasMatchedNodes(edge1, g1, map12)) {
				Edge edge2 = getMatchedEdge(edge1, g1, map12, a2);
				if (edge2 != null) { // substitution
					cost += this.cf.getCost(edge1, edge2) * factor * 0.5;
				} else { // deletion
					cost += this.cf.getEdgeCosts() * factor * 0.5;
				}
			} else {
				cost += this.cf.getEdgeCosts() * factor * 0.5;
			}
		}
		iter = getNodeEdges(a2,j).iterator();
		while (iter.hasNext()) {
			Edge edge2 = iter.next();
			if (hasMatchedNodes(edge2, g2, map21)) {
				Edge edge1 = getMatchedEdge(edge2, g2, map21, a1);
				if (edge1 == null) { // insertion
					cost += this.cf.getEdgeCosts() * factor * 0.5;
				}
			} else {
				cost += this.cf.getEdgeCosts() * factor * 0.5;
			}
		}
		return cost;
	}
	
	private double getEdgeCost2(int i, int j) {
		double cost = 0;
		Iterator<Edge> iter = getNodeEdges(a2,j).iterator();
		while (iter.hasNext()) {
			Edge edge2 = iter.next();
			if (hasMatchedNodes(edge2, g2, map21)) {
				Edge edge1 = getMatchedEdge(edge2, g2, map21, a1);
				if (edge1 != null) { // substitution
					cost += this.cf.getCost(edge2, edge1) * factor * 0.5;
				} else { // deletion
					cost += this.cf.getEdgeCosts() * factor * 0.5;
				}
			} else {
				cost += this.cf.getEdgeCosts() * factor * 0.5;
			}
		}
		iter = getNodeEdges(a1,i).iterator();
		while (iter.hasNext()) {
			Edge edge1 = iter.next();
			if (hasMatchedNodes(edge1, g1, map12)) {
				Edge edge2 = getMatchedEdge(edge1, g1, map12, a2);
				if (edge2 == null) { // insertion
					cost += this.cf.getEdgeCosts() * factor * 0.5;
				}
			} else {
				cost += this.cf.getEdgeCosts() * factor * 0.5;
			}
		}
		return cost;
	}
	
	private LinkedList<Edge> getNodeEdges(Edge[][] a, int idx) {
		LinkedList<Edge> edges = new LinkedList<Edge>();
		for (int i=0; i < a.length; i++) {
			if (a[idx][i] != null) {
				edges.add(a[idx][i]);
			}
			if (a[i][idx] != null) {
				edges.add(a[i][idx]);
			}
		}
		return edges;
	}
	
	private boolean hasMatchedNodes(Edge e, Graph g, int[] m) {
		int idx1 = g.indexOf(e.getStartNode());
		int idx2 = g.indexOf(e.getEndNode());
		return m[idx1] >= 0 && m[idx2] >= 0;
	}
	
	private Edge getMatchedEdge(Edge e, Graph g, int[] m, Edge[][] a) {
		int idx1 = g.indexOf(e.getStartNode());
		int idx2 = g.indexOf(e.getEndNode());
		return a[m[idx1]][m[idx2]];
	}
	
}
