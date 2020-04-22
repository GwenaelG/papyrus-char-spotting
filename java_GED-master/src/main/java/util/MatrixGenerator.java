/**
 * 
 */
package util;



import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import algorithms.HungarianAlgorithm;
import costs.CostFunctionManager;
import graph.Edge;
import graph.Graph;
import graph.Node;


/**
 * @author riesen
 * 
 */
public class MatrixGenerator {

	/**
	 * the cource and target graph whereon the cost matrix is built
	 */
	private Graph source, target;
	
	/**
	 * the cost function actually employed
	 */
	private CostFunctionManager cf;

	/**
	 * the matching algorithm for recursive
	 * edge matchings (hungarian is used in any case!)
	 */
	private HungarianAlgorithm ha;

	/**
	 * whether or not the cost matrix is logged on the console
	 */
	private int outputCostMatrix;

	/**
	 * the decimal format for the distances found
	 */
	private DecimalFormat decFormat;
	
	//andreas
	
	public static final String FULL = "FULL";
	public static final String HALF = "HALF";
	private String localEdgeCosts;
	public void setFullLocalEdgeCosts() {
		this.localEdgeCosts = MatrixGenerator.FULL;
	}
	
	/**
	 * constructs a MatrixGenerator
	 * @param costFunction
	 * @param outputCostMatrix
	 */
	public MatrixGenerator(CostFunctionManager costFunctionManager, int outputCostMatrix) {
		this.cf = costFunctionManager;
		this.ha = new HungarianAlgorithm();
		this.outputCostMatrix = outputCostMatrix;
		this.decFormat = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
		this.decFormat.applyPattern("0.00000");
		this.localEdgeCosts = MatrixGenerator.HALF;
	}

	public double[][] getSmallMatrix(Graph sourceGraph, Graph targetGraph) {
		int sSize = sourceGraph.size();
		int tSize = targetGraph.size();
		int dim = sSize + tSize;
		double[][] bigMatrix = getMatrix(sourceGraph, targetGraph);
		double[][] smallMatrix = new double[sSize + 1][tSize + 1];
		for (int i=0; i < sSize; i++) {
			System.arraycopy(bigMatrix[i], 0, smallMatrix[i], 0, tSize);
		}
		for (int i=0; i < sSize; i++) {
			smallMatrix[i][tSize] = bigMatrix[i][tSize + i];
		}
		for (int j=0; j < tSize; j++) {
			smallMatrix[sSize][j] = bigMatrix[sSize + j][j];
		}
		return smallMatrix;
	}
	
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
	public double[][] getMatrix(Graph sourceGraph, Graph targetGraph) {
		this.source = sourceGraph;
		this.target = targetGraph;
		int sSize = sourceGraph.size();
		int tSize = targetGraph.size();
		int dim = sSize + tSize;
		double[][] matrix = new double[dim][dim];
		double[][] edgeMatrix;
		Node u;
		Node v;
		for (int i = 0; i < sSize; i++) {
			u = (Node) this.source.get(i);
			for (int j = 0; j < tSize; j++) {
				v = (Node) this.target.get(j);
				// TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
				double costs = cf.getCost(u, v);
				// adjacency information is added to the node costs
				edgeMatrix = this.getEdgeMatrix(u, v);
				if (edgeMatrix.length > 0) {
					if (this.localEdgeCosts.compareTo(MatrixGenerator.FULL) == 0) {
						costs += this.ha.hgAlgorithmOnlyCost(edgeMatrix);
					} else {
						costs += 0.5 * this.ha.hgAlgorithmOnlyCost(edgeMatrix);
					}
				}
				matrix[i][j] = costs;
			}
		}
		for (int i = sSize; i < dim; i++) {
			for (int j = 0; j < tSize; j++) {
				if ((i - sSize) == j) {
					v = (Node) this.target.get(j);
					double costs = cf.getNodeCosts();
					double f = v.getEdges().size();
					if (this.localEdgeCosts.compareTo(MatrixGenerator.FULL) == 0) {
						costs += f * cf.getEdgeCosts();
					} else {
						costs += 0.5 * f * cf.getEdgeCosts();
					}
					matrix[i][j] = costs;
				} else {
					matrix[i][j] = Double.POSITIVE_INFINITY;
				}
			}
		}
		for (int i = 0; i < sSize; i++) {
			u = (Node) this.source.get(i);
			for (int j = tSize; j < dim; j++) {
				if ((j - tSize) == i) {
					double costs = cf.getNodeCosts();
					double f = u.getEdges().size();
					if (this.localEdgeCosts.compareTo(MatrixGenerator.FULL) == 0) {
						costs += f * cf.getEdgeCosts();
					} else {
						costs += 0.5 * f * cf.getEdgeCosts();
					}
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
		if (this.outputCostMatrix==1){
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

	
	/**
	 * @return the cost matrix for the edge operations 
	 * between the nodes @param u
	 * @param v
	 */
	private double[][] getEdgeMatrix(Node u, Node v) {
		int uSize = u.getEdges().size();
		int vSize = v.getEdges().size();
		int dim = uSize + vSize;
		double[][] edgeMatrix = new double[dim][dim];
		Edge e_u;
		Edge e_v;
		for (int i = 0; i < uSize; i++) {
			e_u = (Edge) u.getEdges().get(i);
			for (int j = 0; j < vSize; j++) {
				e_v = (Edge) v.getEdges().get(j);
				double costs = cf.getCost(e_u, e_v);
				edgeMatrix[i][j] = costs;
			}
		}
		for (int i = uSize; i < dim; i++) {
			for (int j = 0; j < vSize; j++) {
				// diagonal
				if ((i - uSize) == j) {
					e_v = (Edge) v.getEdges().get(j);
					double costs = cf.getEdgeCosts();
					edgeMatrix[i][j] = costs;
				} else {
					edgeMatrix[i][j] = Double.POSITIVE_INFINITY;
				}
			}
		}
		for (int i = 0; i < uSize; i++) {
			e_u = (Edge) u.getEdges().get(i);
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
