package andreas;
// old version of HEDContext
// deprecated

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import costs.CostFunctionManager;
import graph.Edge;
import graph.Graph;
import graph.Node;

/**
 * @author Andreas Fischer
 */
public class HEDc1 {

	private int radius;
	private HEDViz hed;
	
	private int[] map12;
	private int[] map21;
	private double[] cost12;
	private double[] cost21;
	
	public HEDc1(int radius) {
		this.radius = radius;
		hed = new HEDViz();
	}
	
	// context hed
	
	public double getContextEditDistance(Graph g1, Graph g2, CostFunctionManager cf) {
		// init map
		map12 = new int[g1.size()];
		map21 = new int[g2.size()];
		cost12 = new double[g1.size()];
		cost21 = new double[g2.size()];
		
		// cost matrices
		double[][] localCosts = hed.getNodeCosts(g1, g2, cf);
		double[][] contextCosts = this.getContextCosts(g1, g2, cf);

		// node costs
		double min = Math.abs(g1.size() - g2.size()) * cf.getNodeCosts();
		double hed = getContextCost12(g1, g2, localCosts, contextCosts) + getContextCost21(g1, g2, localCosts, contextCosts);
		return Math.max(min, hed);
	}
	
	// directed context cost
	
	private double getContextCost12(Graph g1, Graph g2, double[][] localCosts, double[][] contextCosts) {
		double hd = 0;
		for (int i=0; i < g1.size(); i++) {
			double epsilon = localCosts[i][g2.size()];
			map12[i] = -1;
			cost12[i] = epsilon;
			if (g2.size() > 0) {
				double best = -1;
				int map = -1;
				for (int j=0; j < g2.size(); j++) {
					double curr = contextCosts[i][j];
					if (best == -1 || curr < best) {
						best = curr;
						map = j;
					}
				}
				double substitution = localCosts[i][map] / 2;
				if (substitution < epsilon) {
					map12[i] = map;
					cost12[i] = substitution;
				}
			}
			hd += cost12[i];
		}
		return hd;
	}
	
	private double getContextCost21(Graph g1, Graph g2, double[][] localCosts, double[][] contextCosts) {
		double hd = 0;
		for (int j=0; j < g2.size(); j++) {
			double epsilon = localCosts[g1.size()][j];
			map21[j] = -1;
			cost21[j] = epsilon;
			if (g1.size() > 0) {
				double best = -1;
				int map = -1;
				for (int i=0; i < g1.size(); i++) {
					double curr = contextCosts[i][j];
					if (best == -1 || curr < best) {
						best = curr;
						map = i;
					}
				}
				double substitution = localCosts[map][j] / 2;
				if (substitution < epsilon) {
					map21[j] = map;
					cost21[j] = substitution;
				}
			}
			hd += cost21[j];
		}
		return hd;
	}
	
	// context cost function
	
	private double[][] getContextCosts(Graph g1, Graph g2, CostFunctionManager cf) {
		double[][] contextCosts = new double[g1.size()][g2.size()];
		Graph[] context1 = getContext(g1);
		Graph[] context2 = getContext(g2);
		for (int i=0; i < g1.size(); i++) {
			Graph contextU = context1[i];
			for (int j=0; j < g2.size(); j++) {
				Graph contextV = context2[j];
				contextCosts[i][j] = hed.getHausdorffEditDistanceUV(contextU, contextV, cf, 0, 0);
				if (i == 10 && j == 4) {
					System.out.println(contextU.get(0));
					System.out.println(contextV.get(0));
					plotGraph(contextU, contextV, hed.getEditPath(contextU, contextV));
				}
			}
		}
		return contextCosts;
	}
	
	private void plotGraph(Graph g1, Graph g2, EditPath ep) {
		LetterPlotter plotter;
		try {
			plotter = new LetterPlotter(g1, g2, ep, 600, 600);
			plotter.displayImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// context graphs
	
	private Graph[] getContext(Graph g) {
		Graph[] context = new Graph[g.size()];
		for (int i_g=0; i_g < g.size(); i_g++) {
			LinkedList<Integer> closed = new LinkedList<Integer>();
			HashSet<Integer> open = new HashSet<Integer>();
			open.add(i_g);
			int depth = 0;
			while (depth <= radius && !open.isEmpty()) {
				HashSet<Integer> neighbors = new HashSet<Integer>();
				Iterator<Integer> iter_o = open.iterator();
				while (iter_o.hasNext()) {
					int idx_o = iter_o.next();
					closed.add(idx_o);
					if (depth < radius) {
						Iterator<Integer> iter_n = g.adjNeighborIdxs(idx_o).iterator();
						while (iter_n.hasNext()) {
							int idx_n = iter_n.next();
							if (!closed.contains(idx_n)) {
								neighbors.add(idx_n);
							}
						}
					}
				}
				open = neighbors;
				depth++;
			}
			Graph contextGraph = new Graph();
			for (int i=0; i < closed.size(); i++) {
				contextGraph.add(g.get(closed.get(i)));
			}
			Edge[][] adj = g.getAdjacencyMatrix();
			Edge[][] contextAdj = new Edge[contextGraph.size()][contextGraph.size()];
			for (int i=0; i < contextGraph.size(); i++) {
				for (int j=0; j < contextGraph.size(); j++) {
					contextAdj[i][j] = adj[closed.get(i)][closed.get(j)];
				}
			}
			contextGraph.setAdjacencyMatrix(contextAdj);
			context[i_g] = contextGraph;
		}
		return context;
	}
	
	// edit path
	
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
