package andreas;
// old version of HEDContext
// deprecated
// computes only an (n) x (m) context cost matrix
// disregarding deletion and insertion of the whole context

import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import costs.CostFunctionManager;
import graph.Edge;
import graph.Graph;
import graph.Node;

/**
 * @author Andreas Fischer
 */
public class HEDc2 {

	private Graph g1;
	private Graph g2;
	private CostFunctionManager cf;
	private int radius;
	private EditPath editPath;
	private EditPath contextEditPath;
	private boolean verbose;
	
	public HEDc2(Graph g1, Graph g2, CostFunctionManager cf, int radius) {
		this.g1 = g1;
		this.g2 = g2;
		this.cf = cf;
		this.radius = radius;
		this.verbose = false;
	}

	public double getEditDistance() {
		double[][] localCosts = getLocalCostMatrix();
		double[][] contextCosts = getContextCostMatrix(localCosts);
		double hed = getContextEditDistance(localCosts, contextCosts);
		double min = Math.abs(g1.size() - g2.size()) * cf.getNodeCosts();
		if (verbose) {
			plotEditPath();
		}
		return Math.max(min, hed);
	}
	
	public EditPath getEditPath() {
		return editPath;
	}
	
	public double[][] generateLocalCostMatrix() {
		return getLocalCostMatrix();
	}
	
	public double[][] generateContextCostMatrix(double[][] localCosts) {
		return getContextCostMatrix(localCosts);
	}
	
	// local cost matrix
	
	private double[][] getLocalCostMatrix() {
		double[][] localCostMatrix = new double[g1.size()+1][g2.size()+1];
		for (int i=0; i < g1.size(); i++) {
			localCostMatrix[i][g2.size()] = cf.getNodeCosts();
			localCostMatrix[i][g2.size()] += 0.5 * g1.adjEdgeDegree(i) * cf.getEdgeCosts();
		}
		for (int j=0; j < g2.size(); j++) {
			localCostMatrix[g1.size()][j] = cf.getNodeCosts();
			localCostMatrix[g1.size()][j] += 0.5 * g2.adjEdgeDegree(j) * cf.getEdgeCosts();
		}
		for (int i=0; i < g1.size(); i++) { 
			Node n1 = g1.get(i);
			LinkedList<Edge> edges1a = g1.getOutgoingEdges(i);
			for (int j=0; j < g2.size(); j++) {
				Node n2 = g2.get(j);
				LinkedList<Edge> edges2a = g2.getOutgoingEdges(j);
				// TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
				localCostMatrix[i][j] = cf.getCost(n1, n2);
				double min = Math.abs(edges1a.size() - edges2a.size()) * cf.getEdgeCosts();
				double hed = getLocalEdgeCost(edges1a, edges2a, cf) + getLocalEdgeCost(edges2a, edges1a, cf);
				localCostMatrix[i][j] += 0.5 * Math.max(min, hed);
				if (g1.isDirected()) {
					LinkedList<Edge> edges1b = g1.getIncomingEdges(i);
					LinkedList<Edge> edges2b = g2.getIncomingEdges(j);
					min = Math.abs(edges1b.size() - edges2b.size()) * cf.getEdgeCosts();
					hed = getLocalEdgeCost(edges1b, edges2b, cf) + getLocalEdgeCost(edges2b, edges1b, cf);
					localCostMatrix[i][j] += 0.5 * Math.max(min, hed);
				}
			}
		}
		return localCostMatrix;
	}
	
	private double getLocalEdgeCost(LinkedList<Edge> edges1, LinkedList<Edge> edges2, CostFunctionManager cf) {
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
	
	// get context

	private TreeContext[] getContext(Graph g) {
		TreeContext[] context = new TreeContext[g.size()];
		for (int i_g=0; i_g < g.size(); i_g++) {
			TreeContext tree = new TreeContext();
			TreeSet<Integer> level = new TreeSet<Integer>();
			level.add(i_g);
			tree.add(level);
			int depth = 0;
			while (depth < radius && !level.isEmpty()) {
				TreeSet<Integer> nextLevel = new TreeSet<Integer>();
				Iterator<Integer> iter_l = level.iterator();
				while (iter_l.hasNext()) {
					int idx_l = iter_l.next();
					Iterator<Integer> iter_n = g.adjNeighborIdxs(idx_l).iterator();
					while (iter_n.hasNext()) {
						int idx_n = iter_n.next();
						if (!tree.contains(idx_n)) {
							nextLevel.add(idx_n);
						}
					}
				}
				if (nextLevel.size() > 0) {
					tree.add(nextLevel);
				}
				level = nextLevel;
				depth++;
			}
			context[i_g] = tree;
		}
		return context;
	}
	
	// context cost matrix
	
	private double[][] getContextCostMatrix(double[][] localCosts) {
		double[][] contextCosts = new double[g1.size()][g2.size()];
		TreeContext[] context1 = getContext(g1);
		TreeContext[] context2 = getContext(g2);
		for (int i=0; i < g1.size(); i++) {
			TreeContext contextU = context1[i];
			for (int j=0; j < g2.size(); j++) {
				TreeContext contextV = context2[j];
				contextCosts[i][j] = getTreeEditDistance(contextU, contextV, localCosts);
				if (verbose && i == 10 && j == 2) {
					System.out.println(contextU);
					System.out.println(contextV);
					plotContextEditPath();
				}
			}
		}
		return contextCosts;
	}
	
	private double getTreeEditDistance(TreeContext contextU, TreeContext contextV, double[][] localCosts) {
		double hd = 0;
		contextEditPath = new EditPath(g1, g2, true);
		int maxLevel = Math.max(contextU.numLevels(), contextV.numLevels());
		TreeSet<Integer> levelU, levelV;
		for (int i=0; i < maxLevel; i++) {
			if (contextU.hasLevel(i)) {
				levelU = contextU.getLevel(i);
			} else {
				levelU = new TreeSet<Integer>();
			}
			if (contextV.hasLevel(i)) {
				levelV = contextV.getLevel(i);
			} else {
				levelV = new TreeSet<Integer>();
			}
			hd += getLevelEditDistance(levelU, levelV, localCosts);
		}
		return hd;
	}
	
	private double getLevelEditDistance(TreeSet<Integer> levelU, TreeSet<Integer> levelV, double[][] localCosts) {
		double hd = 0;
		Iterator<Integer> iterU, iterV;
		EditOperation eo;
		iterU = levelU.iterator();
		while (iterU.hasNext()) {
			int i = iterU.next();
			double best = localCosts[i][g2.size()];
			int map = -1;
			iterV = levelV.iterator();
			while (iterV.hasNext()) {
				int j = iterV.next();
				if (localCosts[i][j] < best) {
					best = localCosts[i][j];
					map = j;
				}
			}
			if (map >= 0) {
				eo = new EditOperation(EditOperation.SAB, g1.get(i), g2.get(map), best, 0);
			} else {
				eo = new EditOperation(EditOperation.DEL, g1.get(i), g1.get(i), best, 0);
			}
			contextEditPath.addEditOperation(eo);
			hd += best;
		}
		iterV = levelV.iterator();
		while (iterV.hasNext()) {
			int j = iterV.next();
			double best = localCosts[g1.size()][j];
			int map = -1;
			iterU = levelU.iterator();
			while (iterU.hasNext()) {
				int i = iterU.next();
				if (localCosts[i][j] < best) {
					best = localCosts[i][j];
					map = i;
				}
			}
			if (map >= 0) {
				eo = new EditOperation(EditOperation.SBA, g2.get(j), g1.get(map), best, 0);
			} else {
				eo = new EditOperation(EditOperation.INS, g2.get(j), g2.get(j), best, 0);
			}
			contextEditPath.addEditOperation(eo);
			hd += best;
		}
		return hd;
	}
	
	// context edit distance
	
	private double getContextEditDistance(double[][] localCosts, double[][] contextCosts) {
		double hd = 0;
		editPath = new EditPath(g1, g2, true);
		EditOperation eo;
		for (int i=0; i < g1.size(); i++) {
			double best = -1;
			int map = -1;
			for (int j=0; j < g2.size(); j++) {
				if (best == -1 || contextCosts[i][j] < best) {
					best = contextCosts[i][j];
					map = j;
				}
			}
			double epsilon = localCosts[i][g2.size()];
			if (map >= 0 && (localCosts[i][map] / 2) < epsilon) {
				double substitution = localCosts[i][map] / 2;
				eo = new EditOperation(EditOperation.SAB, g1.get(i), g2.get(map), substitution, 0);
				hd += substitution;
			} else {
				eo = new EditOperation(EditOperation.DEL, g1.get(i), g1.get(i), epsilon, 0);
				hd += epsilon;
			}
			editPath.addEditOperation(eo);
		}
		for (int j=0; j < g2.size(); j++) {
			double best = -1;
			int map = -1;
			for (int i=0; i < g1.size(); i++) {
				if (best == -1 || contextCosts[i][j] < best) {
					best = contextCosts[i][j];
					map = i;
				}
			}
			double epsilon = localCosts[g1.size()][j];
			if (map >= 0 && (localCosts[map][j] / 2) < epsilon) {
				double substitution = localCosts[map][j] / 2;
				eo = new EditOperation(EditOperation.SBA, g2.get(j), g1.get(map), substitution, 0);
				hd += substitution;
			} else {
				eo = new EditOperation(EditOperation.INS, g2.get(j), g2.get(j), epsilon, 0);
				hd += epsilon;
			}
			editPath.addEditOperation(eo);
		}
		return hd;
	}
	
	// plot
	
	private void plotContextEditPath() {
		LetterPlotter plotter;
		try {
			plotter = new LetterPlotter(g1, g2, contextEditPath, 600, 600);
			plotter.displayImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void plotEditPath() {
		LetterPlotter plotter;
		try {
			plotter = new LetterPlotter(g1, g2, editPath, 600, 600);
			plotter.displayImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
