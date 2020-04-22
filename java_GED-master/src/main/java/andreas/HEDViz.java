package andreas;

import java.util.Iterator;
import java.util.LinkedList;

import costs.CostFunctionManager;
import graph.Edge;
import graph.Graph;
import graph.Node;

/**
 * @author Andreas Fischer
 */
public class HEDViz {

	private int idxU;
	private int idxV;
	private int[] map12;
	private int[] map21;
	private double[] cost12;
	private double[] cost21;

	public double getHausdorffEditDistance(Graph g1, Graph g2, CostFunctionManager cf) {
		this.idxU = -1;
		this.idxV = -1;
		return getEditDistance(g1, g2, cf);
	}
	
	public double getHausdorffEditDistanceUV(Graph g1, Graph g2, CostFunctionManager cf, int idxU, int idxV) {
		this.idxU = idxU;
		this.idxV = idxV;
		return getEditDistance(g1, g2, cf);
	}
	
	private double getEditDistance(Graph g1, Graph g2, CostFunctionManager cf) {
		// init map
		map12 = new int[g1.size()];
		map21 = new int[g2.size()];
		cost12 = new double[g1.size()];
		cost21 = new double[g2.size()];
		
		// node cost function
		double[][] nf = getNodeCosts(g1, g2, cf);
		
		// node costs
		double min = Math.abs(g1.size() - g2.size()) * cf.getNodeCosts();
		double hed = hdNodes12(g1, g2, nf) + hdNodes21(g2, g1, nf);
		return Math.max(min, hed);
	}
	
	public double[][] getNodeCosts(Graph g1, Graph g2, CostFunctionManager cf) {
		// init node cost function
		double[][] nf = new double[g1.size()+1][g2.size()+1];
		
		// node deletion
		for (int i=0; i < g1.size(); i++) {
			nf[i][g2.size()] = cf.getNodeCosts();
			nf[i][g2.size()] += 0.5 * g1.adjEdgeDegree(i) * cf.getEdgeCosts();
		}
		
		// node insertion
		for (int j=0; j < g2.size(); j++) {
			nf[g1.size()][j] = cf.getNodeCosts();
			nf[g1.size()][j] += 0.5 * g2.adjEdgeDegree(j) * cf.getEdgeCosts();
		}
		
		// node substitution
		for (int i=0; i < g1.size(); i++) { 
			Node n1 = g1.get(i);
			LinkedList<Edge> edges1a = g1.getOutgoingEdges(i);
			for (int j=0; j < g2.size(); j++) {
				Node n2 = g2.get(j);
				LinkedList<Edge> edges2a = g2.getOutgoingEdges(j);
				// TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
				nf[i][j] = cf.getCost(n1, n2);
				
				// adjacent edge costs: outgoing edges
				double min = Math.abs(edges1a.size() - edges2a.size()) * cf.getEdgeCosts();
				double hed = hdEdges(edges1a, edges2a, cf) + hdEdges(edges2a, edges1a, cf);
				nf[i][j] += 0.5 * Math.max(min, hed);
				
				// adjacent edge costs: incoming edges
				if (g1.isDirected()) {
					LinkedList<Edge> edges1b = g1.getIncomingEdges(i);
					LinkedList<Edge> edges2b = g2.getIncomingEdges(j);
					min = Math.abs(edges1b.size() - edges2b.size()) * cf.getEdgeCosts();
					hed = hdEdges(edges1b, edges2b, cf) + hdEdges(edges2b, edges1b, cf);
					nf[i][j] += 0.5 * Math.max(min, hed);
				}
			}
		}
		
		return nf;
	}
	
	private double hdEdges(LinkedList<Edge> edges1, LinkedList<Edge> edges2, CostFunctionManager cf) {
		double hd = 0;
		Iterator<Edge> iter1 = edges1.iterator();
		while (iter1.hasNext()) {
			double best = cf.getEdgeCosts();
			Edge e1 = iter1.next();
			Iterator<Edge> iter2 = edges2.iterator();
			while (iter2.hasNext()) {
				Edge e2 = iter2.next();
				double curr = cf.getCost(e1, e2) / 2;
				if (curr < best) {
					best = curr;
				}
			}
			hd += best;
		}
		return hd;
	}
	
	private double hdNodes12(Graph g1, Graph g2, double[][] nf) {
		double hd = 0;
		for (int i=0; i < g1.size(); i++) {
			double best = nf[i][g2.size()];
			int map = -1;
			for (int j=0; j < g2.size(); j++) {
				double curr = nf[i][j] / 2;
				if (curr < best) {
					best = curr;
					map = j;
				}
			}
			if (i == idxU) {
				best = nf[idxU][idxV] / 2;
				map = idxV;
			}
			hd += best;
			map12[i] = map;
			cost12[i] = best;
		}
		return hd;
	}
	
	private double hdNodes21(Graph g2, Graph g1, double[][] nf) {
		double hd = 0;
		for (int j=0; j < g2.size(); j++) {
			double best = nf[g1.size()][j];
			int map = -1;
			for (int i=0; i < g1.size(); i++) {
				double curr = nf[i][j] / 2;
				if (curr < best) {
					best = curr;
					map = i;
				}
			}
			if (j == idxV) {
				best = nf[idxU][idxV] / 2;
				map = idxU;
			}
			hd += best;
			map21[j] = map;
			cost21[j] = best;
		}
		return hd;
	}
	
	public EditPath getEditPath(Graph g1, Graph g2) {
		EditPath editPath = new EditPath(g1, g2, true);
		for (int i=0; i < g1.size(); i++) {
			Node n1 = g1.get(i);
			double nodeCost = cost12[i];
			double edgeCost = 0;
			EditOperation eo = null;
			if (map12[i] >= 0) {
				Node n2 = g2.get(map12[i]);
				eo = new EditOperation(EditOperation.SAB, n1, n2, nodeCost, edgeCost);
			} else {
				eo = new EditOperation(EditOperation.DEL, n1, n1, nodeCost, edgeCost);
			}
			editPath.addEditOperation(eo);
		}
		for (int j=0; j < g2.size(); j++) {
			Node n2 = g2.get(j);
			double nodeCost = cost21[j];
			double edgeCost = 0;
			EditOperation eo = null;
			if (map21[j] >= 0) {
				Node n1 = g1.get(map21[j]);
				eo = new EditOperation(EditOperation.SBA, n2, n1, nodeCost, edgeCost);
			} else { // deletion
				eo = new EditOperation(EditOperation.INS, n2, n2, nodeCost, edgeCost);
			}
			editPath.addEditOperation(eo);
		}
		return editPath;
	}
	
}
