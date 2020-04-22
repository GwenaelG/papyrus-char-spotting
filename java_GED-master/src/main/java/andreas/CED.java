package andreas;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

import algorithms.HungarianAlgorithm;
import costs.CostFunctionManager;
import graph.Edge;
import graph.Graph;
import graph.Node;

/**
 * @author Andreas Fischer
 */
public class CED {

	protected int radius;
	protected CostFunctionManager cf;
	protected HungarianAlgorithm ha;
	protected EditPathFactory epf;
	protected boolean verbose;
	protected EditPath ep;
	
	public CED(CostFunctionManager cf) {
		radius = 1;
		this.cf = cf;
		ha = new HungarianAlgorithm();
		epf = new EditPathFactory();
		verbose = false;
		ep = null;
	}
	
	public double getContextEditDistance(Graph g1, Graph g2) {
		double[][] costMatrix = getContextCostMatrix(g1, g2);
//		double[][] costMatrix = getCostMatrix(g1, g2);

		int[][] matching = ha.hgAlgorithm(costMatrix);
		ep = epf.fromBipartite(g1, g2, matching, cf);
		return ep.getCost();
//		return ha.hgAlgorithmOnlyCost(costMatrix);
	}
	
	public EditPath getEditPath() {
		return ep;
	}
	
	/*
	 * context edit distance
	 */
	
	/**
	 * @return the cost matrix for two graphs @param sourceGraph and @param targetGraph
	 * |         |
	 * | c_i,j   | del
	 * |_________|______
	 * |         |
	 * |  ins    |	0
	 * |         |
	 * 
	 */
	public double[][] getContextCostMatrix(Graph g1, Graph g2) {
		int sSize = g1.size();
		int tSize = g2.size();
		int dim = sSize + tSize;
		double[][] matrix = new double[dim][dim];
		Graph[] context1 = getContext(g1);
		Graph[] context2 = getContext(g2);
		for (int i = 0; i < sSize; i++) {
			Graph c1 = context1[i];
			for (int j = 0; j < tSize; j++) {
				Graph c2 = context2[j];
				double[][] costMatrix = getCostMatrix(c1, c2);
				int[][] matching = ha.hgAlgorithm(costMatrix);
				double costs = epf.fromBipartite(c1, c2, matching, cf).getCost();
				matrix[i][j] = costs;
			}
		}
		for (int i = sSize; i < dim; i++) {
			for (int j = 0; j < tSize; j++) {
				if ((i - sSize) == j) {
					Graph c2 = context2[j];
					double costs = getContextCost(c2);
					matrix[i][j] = costs;
				} else {
					matrix[i][j] = Double.POSITIVE_INFINITY;
				}
			}
		}
		for (int i = 0; i < sSize; i++) {
			Graph c1 = context1[i];
			for (int j = tSize; j < dim; j++) {
				if ((j - tSize) == i) {
					double costs = getContextCost(c1);
					matrix[i][j] = costs;
				} else {
					matrix[i][j] = Double.POSITIVE_INFINITY;
				}
			}
		}
		for (int i = sSize; i < dim; i++) {
			for (int j = tSize; j < dim; j++) {
				matrix[i][j] =0.0;
			}
		}
		if (verbose){
			DecimalFormat decFormat = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
			decFormat.applyPattern("0.00000");
			System.out.println("\nThe Cost Matrix:");
			for (int k = 0; k < matrix.length; k++){
				for (int l = 0; l < matrix[0].length; l++){
					if (matrix[k][l] < Double.POSITIVE_INFINITY){
						System.out.print(decFormat.format(matrix[k][l])+"\t");
						
					} else {
						System.out.print("infty\t");
					}
					
				}
				System.out.println();
			}
		}
		return matrix;
	}

	private Graph[] getContext(Graph g) {
		Graph[] context = new Graph[g.size()];
		for (int i_g=0; i_g < g.size(); i_g++) {
			HashSet<Integer> open = new HashSet<Integer>();
			LinkedList<Integer> closed = new LinkedList<Integer>();
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
			Edge[][] adj = g.getAdjacenyMatrix();
			Edge[][] contextAdj = new Edge[contextGraph.size()][contextGraph.size()];
			for (int i=0; i < contextGraph.size(); i++) {
				for (int j=0; j < contextGraph.size(); j++) {
					contextAdj[i][j] = adj[closed.get(i)][closed.get(j)];
				}
			}
			contextGraph.setAdjacenyMatrix(contextAdj);
			context[i_g] = contextGraph;
		}
		return context;
	}
	
	private double getContextCost(Graph contextGraph) {
		double cost = 0;
		cost += contextGraph.size() * cf.getNodeCosts();
		for (int i=0; i < contextGraph.size(); i++) {
			cost += 0.5 * contextGraph.adjEdgeDegree(i) * cf.getEdgeCosts();
		}
		return cost;
	}
	
	/*
	 * standard edit distance
	 */

	protected double[][] getCostMatrix(Graph g1, Graph g2) {
		int sSize = g1.size();
		int tSize = g2.size();
		int dim = sSize + tSize;
		double[][] matrix = new double[dim][dim];
		double[][] edgeMatrix;
		Node u;
		Node v;
		for (int i = 0; i < sSize; i++) {
			u = (Node) g1.get(i);
			for (int j = 0; j < tSize; j++) {
				v = (Node) g2.get(j);
				// TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
				double costs = cf.getCost(u, v);
				edgeMatrix = this.getEdgeMatrix(g1.adjEdges(i), g2.adjEdges(j));
				if (edgeMatrix.length > 0) {
					costs += 0.5 * this.ha.hgAlgorithmOnlyCost(edgeMatrix);
				}
				matrix[i][j] = costs;
			}
		}
		for (int i = sSize; i < dim; i++) {
			for (int j = 0; j < tSize; j++) {
				if ((i - sSize) == j) {
					double costs = cf.getNodeCosts();
					costs += 0.5 * g2.adjEdgeDegree(j) * cf.getEdgeCosts();
					matrix[i][j] = costs;
				} else {
					matrix[i][j] = Double.POSITIVE_INFINITY;
				}
			}
		}
		for (int i = 0; i < sSize; i++) {
			for (int j = tSize; j < dim; j++) {
				if ((j - tSize) == i) {
					double costs = cf.getNodeCosts();
					costs += 0.5 * g1.adjEdgeDegree(i) * cf.getEdgeCosts();
					matrix[i][j] = costs;
				} else {
					matrix[i][j] = Double.POSITIVE_INFINITY;
				}
			}
		}
		for (int i = sSize; i < dim; i++) {
			for (int j = tSize; j < dim; j++) {
				matrix[i][j] =0.0;
			}
		}
		return matrix;
	}

	protected double[][] getEdgeMatrix(LinkedList<Edge> uEdges, LinkedList<Edge> vEdges) {
		int uSize = uEdges.size();
		int vSize = vEdges.size();
		int dim = uSize + vSize;
		double[][] edgeMatrix = new double[dim][dim];
		Edge e_u;
		Edge e_v;
		for (int i = 0; i < uSize; i++) {
			e_u = (Edge) uEdges.get(i);
			for (int j = 0; j < vSize; j++) {
				e_v = (Edge) vEdges.get(j);
				double costs = cf.getCost(e_u, e_v);
				edgeMatrix[i][j] = costs;
			}
		}
		for (int i = uSize; i < dim; i++) {
			for (int j = 0; j < vSize; j++) {
				// diagonal
				if ((i - uSize) == j) {
					e_v = (Edge) vEdges.get(j);
					double costs = cf.getEdgeCosts();
					edgeMatrix[i][j] = costs;
				} else {
					edgeMatrix[i][j] = Double.POSITIVE_INFINITY;
				}
			}
		}
		for (int i = 0; i < uSize; i++) {
			e_u = (Edge) uEdges.get(i);
			for (int j = vSize; j < dim; j++) {
				// diagonal
				if ((j - vSize) == i) {
					double costs = cf.getEdgeCosts();
					edgeMatrix[i][j] = costs;
				} else {
					edgeMatrix[i][j] = Double.POSITIVE_INFINITY;
				}
			}
		}
		for (int i = uSize; i < dim; i++) {
			for (int j = vSize; j < dim; j++) {
				edgeMatrix[i][j] = 0.0;
			}
		}
		return edgeMatrix;
	}
	
}
