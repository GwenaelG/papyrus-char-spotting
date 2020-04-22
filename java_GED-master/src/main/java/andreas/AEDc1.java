package andreas;

import java.text.DecimalFormat;
import java.text.NumberFormat;
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
public class AEDc1 {

	protected CostFunctionManager cf;
	protected HungarianAlgorithm ha;
	protected EditPathFactory epf;
	protected boolean verbose;
	protected EditPath ep;
	
	public AEDc1(CostFunctionManager cf) {
		this.cf = cf;
		ha = new HungarianAlgorithm();
		epf = new EditPathFactory();
		verbose = false;
		ep = null;
	}
	
	public double getEditDistance(Graph g1, Graph g2) {
		double[][] costA = getCostMatrix(g1, g2);
		double[][] costB = new double[costA.length][costA.length];
		for (int i=0; i < costA.length; i++) {
			System.arraycopy(costA[i], 0, costB[i], 0, costA.length);
		}
		
		for (int i=0; i < g1.size(); i++) {
			Node u = g1.get(i);
			for (int j=0; j < g2.size(); j++) {
				
				// node cost
				Node v = g2.get(j);
				// TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
				double nodeCost = cf.getCost(u, v);
				
				// plausible context assignment
				LinkedList<Integer> context1 = g1.adjNeighborIdxs(i);
				LinkedList<Integer> context2 = g2.adjNeighborIdxs(j);
				if (context1.size() > 0 || context2.size() > 0) {
					double[][] costContext = getContextMatrix(context1, context2, costA, g1.size(), g2.size());
					int[][] matching = ha.hgAlgorithm(costContext);
					int[] map12 = new int[context1.size()]; for(int ic=0;ic<context1.size();ic++){map12[ic] = -1;}
					int[] map21 = new int[context2.size()]; for(int ic=0;ic<context2.size();ic++){map21[ic] = -1;}
					for (int ic=0; ic < matching.length; ic++) {
						if (matching[ic][0] < context1.size() && matching[ic][1] < context2.size()) {
							map12[matching[ic][0]] = matching[ic][1];
							map21[matching[ic][1]] = matching[ic][0];
						}
					}
					
					// implied edge cost
					double edgeCost = 0;
					for (int ic=0; ic < context1.size(); ic++) {
						if (map12[ic] < 0) {
							edgeCost += cf.getEdgeCosts();
						} else {
							Edge p = g1.adjEdge(i, context1.get(ic));
							Edge q = g2.adjEdge(j, context2.get(map12[ic]));
							edgeCost += cf.getCost(p, q);
						}
					}
					for (int ic=0; ic < context2.size(); ic++) {
						if (map21[ic] < 0) {
							edgeCost += cf.getEdgeCosts();
						}
					}
					costB[i][j] = nodeCost + 0.5 * edgeCost; // without 0.5 ?
				} else {
					costB[i][j] = nodeCost;
				}
				
				
			}
		}
		
//		printMatrix(costA, "costA");
//		printMatrix(costB, "costB");
		
		int[][] matching = ha.hgAlgorithm(costB);
		ep = epf.fromBipartite(g1, g2, matching, cf);
		return ep.getCost();
	}
	
	/*
	 * print cost matrix
	 */
	
	protected void printMatrix(double[][] matrix, String title) {
		DecimalFormat decFormat = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
		decFormat.applyPattern("0.00000");
		System.out.println("\n" + title + ":");
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
	
	/*
	 * context cost matrix
	 */
	
	protected double[][] getContextMatrix(LinkedList<Integer> context1, LinkedList<Integer> context2, double[][] costA, int size1, int size2) {
		int c1Size = context1.size();
		int c2Size = context2.size();
		int dim = c1Size + c2Size;
		double[][] matrix = new double[dim][dim];
		for (int i = 0; i < c1Size; i++) {
			int idx1 = context1.get(i);
			for (int j = 0; j < c2Size; j++) {
				int idx2 = context2.get(j);
				matrix[i][j] = costA[idx1][idx2];
			}
		}
		for (int i = c1Size; i < dim; i++) {
			for (int j = 0; j < c2Size; j++) {
				// diagonal
				if ((i - c1Size) == j) {
					int idx2 = context2.get(j);
					matrix[i][j] = costA[size1+idx2][idx2];
				} else {
					matrix[i][j] = Double.POSITIVE_INFINITY;
				}
			}
		}
		for (int i = 0; i < c1Size; i++) {
			int idx1 = context1.get(i);
			for (int j = c2Size; j < dim; j++) {
				// diagonal
				if ((j - c2Size) == i) {
					matrix[i][j] = costA[idx1][size2+idx1];
				} else {
					matrix[i][j] = Double.POSITIVE_INFINITY;
				}
			}
		}
		for (int i = c1Size; i < dim; i++) {
			for (int j = c2Size; j < dim; j++) {
				matrix[i][j] = 0.0;
			}
		}
		return matrix;
	}

	public EditPath getEditPath() {
		return ep;
	}
	
	/*
	 * standard node matrix
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
					costs += this.ha.hgAlgorithmOnlyCost(edgeMatrix); // 0.5 ?
				}
				matrix[i][j] = costs;
			}
		}
		for (int i = sSize; i < dim; i++) {
			for (int j = 0; j < tSize; j++) {
				if ((i - sSize) == j) {
					double costs = cf.getNodeCosts();
					costs += g2.adjEdgeDegree(j) * cf.getEdgeCosts(); // 0.5 ?
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
					costs += g1.adjEdgeDegree(i) * cf.getEdgeCosts(); // 0.5 ?
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

	/*
	 * standard edge matrix
	 */
	
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
