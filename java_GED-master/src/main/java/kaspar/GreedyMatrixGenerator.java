/**
 * 
 */
package kaspar;



import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Locale;

import costs.CostFunctionManager;
import graph.Edge;
import graph.Graph;
import graph.Node;
import algorithms.HungarianAlgorithm;


/**
 * @author riesen
 * 
 */
public class GreedyMatrixGenerator {

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
	private GreedyMatching gm;

	/**
	 * whether or not the cost matrix is logged on the console
	 */
	private int outputCostMatrix;

	/**
	 * the decimal format for the distances found
	 */
	private DecimalFormat decFormat;

	/**
	 * whether or not adjacent edges are considered
	 */
	private String adj;

	private double beta;

	private LinkedList<Edge> edges1;

	private LinkedList<Edge> edges2;

	private Edge[][] sourceEdges;

	private Edge[][] targetEdges;
	
	private double decay;
	
	

	/**
	 * constructs a MatrixGenerator
	 * @param costFunction
	 * @param outputCostMatrix
	 */
	public GreedyMatrixGenerator(CostFunctionManager costFunctionManager, int outputCostMatrix, double decay) {
		this.cf = costFunctionManager;
		this.ha = new HungarianAlgorithm();
		this.gm = new GreedyMatching();
		this.outputCostMatrix = outputCostMatrix;
		this.decFormat = (DecimalFormat) NumberFormat
				.getInstance(Locale.ENGLISH);
		this.decFormat.applyPattern("00.000");
		this.decay = decay;
	}





	/**
	 * @param centrality 
	 * @return the cost matrix for two graphs @param sourceGraph and @param targetGraph
	 * |         |
	 * | c_i,j   | del
	 * |_________|______
	 * |         |
	 * |  ins    |	0
	 * |         |
	 * 
	 */
	public double[][] getMatrix(Graph sourceGraph, Graph targetGraph, String centrality) {
		this.source = sourceGraph;
		this.target = targetGraph;

//		if (!centrality.equals("none")){
//			CentralityInterface.rankNodes(this.source, centrality);
//			CentralityInterface.rankNodes(this.target, centrality);
//		}
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
				if (this.adj.equals("worst")){
					costs += u.getEdges().size()*cf.getEdgeCosts();
					costs += v.getEdges().size()*cf.getEdgeCosts();
				}
				if (this.adj.equals("best")){
					// adjacency information is added to the node costs
					edgeMatrix = this.getEdgeMatrix(u, v);
					costs += this.ha.hgAlgorithmOnlyCost(edgeMatrix) ;
				}
				
				if (this.adj.equals("more")){
					// adjacency information is added to the node costs
					edgeMatrix = this.getEdgeMatrixUsingNodes(u, v);
					costs += this.ha.hgAlgorithmOnlyCost(edgeMatrix) ;
				}
				
//				if (!centrality.equals("none")){
//					// difference of centralities
//					double uMeasure = u.getCentralityMeasure();
//					double vMeasure = v.getCentralityMeasure();			
//					double deltaC = Math.abs(uMeasure -vMeasure);
//					costs *= this.beta;
//					costs += (1-this.beta)*deltaC;	
//				}
				matrix[i][j] = costs;
			}
		}
		for (int i = sSize; i < dim; i++) {
			for (int j = 0; j < tSize; j++) {
				if ((i - sSize) == j) {
					v = (Node) this.target.get(j);
					double costs = cf.getNodeCosts();
					if (this.adj.equals("worst") || this.adj.equals("best")){
						double f = v.getEdges().size();
						costs += (f * cf.getEdgeCosts() );
					}
					if (this.adj.equals("more")){
						double f = v.getEdges().size();
						costs += (f * (cf.getEdgeCosts()));//+this.decay*cf.getNodeCosts()));
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
					if (this.adj.equals("worst") || this.adj.equals("best")){
						double f = u.getEdges().size();
						costs += (f * cf.getEdgeCosts() );
					}
					if (this.adj.equals("more")){
						double f = u.getEdges().size();
						costs += (f * (cf.getEdgeCosts()));//+this.decay*cf.getNodeCosts()));
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
						System.out.print("inf\t");
					}
					
				}
				System.out.println();
			}
		}
		return matrix;
	}
	
	
	private void printMatrix(double[][] m) {
		for (int i = 0; i < m.length; i++){
			for (int j = 0; j < m[0].length; j++){
				System.out.print(m[i][j]+" ");
			}
			System.out.println();
		}
		
	}





	private double[][] getEdgeMatrixUsingNodes(Node u, Node v) {
		int uSize = u.getEdges().size();
		int vSize = v.getEdges().size();
		int dim = uSize + vSize;
		double[][] edgeMatrix = new double[dim][dim];
		Edge e_u;
		Edge e_v;
		for (int i = 0; i < uSize; i++) {
			e_u = (Edge) u.getEdges().get(i);
			Node otherEndu = e_u.getOtherEnd(u);
			for (int j = 0; j < vSize; j++) {
				e_v = (Edge) v.getEdges().get(j);
				Node otherEndv = e_u.getOtherEnd(v);
				double costs = cf.getCost(e_u, e_v);
//				costs += (this.decay * cf.getCost(otherEndu, otherEndv));
				edgeMatrix[i][j] = costs;
			}
		}
		for (int i = uSize; i < dim; i++) {
			for (int j = 0; j < vSize; j++) {
				// diagonal
				if ((i - uSize) == j) {
					double costs = cf.getEdgeCosts();
					//costs += (this.decay * cf.getNodeCosts());
					edgeMatrix[i][j] = costs;
				} else {
					edgeMatrix[i][j] = Double.POSITIVE_INFINITY;
				}
			}
		}
		for (int i = 0; i < uSize; i++) {
			for (int j = vSize; j < dim; j++) {
				// diagonal
				if ((j - vSize) == i) {
					double costs = cf.getEdgeCosts();
				//	costs += (this.decay * cf.getNodeCosts());
					edgeMatrix[i][j] = costs;
				} else {
					edgeMatrix[i][j] = Double.POSITIVE_INFINITY;
				}
			}
		}
		
		return edgeMatrix;
	}





	public double[][] getPureStructuralMatrix(Graph sourceGraph, Graph targetGraph) {
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
				double costs = 0;
				edgeMatrix = this.getEdgeMatrix(u, v);
				costs += this.ha.hgAlgorithmOnlyCost(edgeMatrix);
				matrix[i][j] = costs;
			}
		}
		for (int i = sSize; i < dim; i++) {
			for (int j = 0; j < tSize; j++) {
				if ((i - sSize) == j) {
					v = (Node) this.target.get(j);
					double costs = 0;
					double f = v.getEdges().size();
					costs += (f * cf.getEdgeCosts());
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
					double costs = 0;
					double f = u.getEdges().size();
					costs += (f * cf.getEdgeCosts());
					matrix[i][j] = costs;
				} else {
					matrix[i][j] = Double.POSITIVE_INFINITY;
				}
			}
		}
		for (int i = sSize; i < dim; i++) {
			for (int j = tSize; j < dim; j++) {
				matrix[i][j] = 0.0;
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
					double costs = cf.getEdgeCosts();
					edgeMatrix[i][j] = costs;
				} else {
					edgeMatrix[i][j] = Double.POSITIVE_INFINITY;
				}
			}
		}
		for (int i = 0; i < uSize; i++) {
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
		
		return edgeMatrix;
	}





	public void setAdj(String adj) {
		this.adj = adj;
		
	}





	public void setBeta(double beta) {
		this.beta = beta;
		
	}





	




	





	private int getMap(int[][] m, int k) {
		for (int i = 0; i < m.length; i++){
			if (m[i][0] == k ){
				return m[i][1];
			}
		}
		return -7;
	}





	private boolean matchingContains(int[][] m, int from, int to) {
		for (int i = 0; i < m.length; i++){
			if (m[i][0] == from && m[i][1] == to){
				return true;
			}
		}
		return false;
	}





	public double[][] getMatrix(Graph sourceGraph, Graph targetGraph,
			int[][] structMatching, double[][] originalCM) {
		this.source = sourceGraph;
		this.target = targetGraph;

		
		int sSize = sourceGraph.size();
		int tSize = targetGraph.size();
		int dim = sSize + tSize;
		double[][] matrix = new double[dim][dim];
		for (int i = 0; i < dim; i++){
			for (int j = 0; j < dim; j++){
				matrix[i][j] = originalCM[i][j]*10000.0;
			}
		}
		
		int[] usedOfG1 = new int[sSize];
		int[] usedOfG2 = new int[tSize];
		for (int i = 0; i < structMatching.length; i++){
			if (structMatching[i][0] < this.edges1.size()){
				Edge e1 = this.edges1.get(structMatching[i][0]);
				if (structMatching[i][1] < this.edges2.size()){
					Edge e2 = this.edges2.get(structMatching[i][1]);
					Node u1 = e1.getStartNode();
					Node u2 = e1.getEndNode();
					Node v1 = e2.getStartNode();
					Node v2 = e2.getEndNode();
					int u1Index = sourceGraph.indexOf(u1);
					int u2Index = sourceGraph.indexOf(u2);
					int v1Index = targetGraph.indexOf(v1);
					int v2Index = targetGraph.indexOf(v2);
					// TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
					matrix[u1Index][v1Index] = this.cf.getCost(u1, v1);
					matrix[u2Index][v2Index] = this.cf.getCost(u2, v2);
					matrix[u1Index][v2Index] = this.cf.getCost(u1, v2);
					matrix[u2Index][v1Index] = this.cf.getCost(u2, v1);
					usedOfG1[u1Index]=1;
					usedOfG1[u2Index]=1;
					usedOfG2[v1Index]=1;
					usedOfG2[v2Index]=1;
				} else {
					Node u1 = e1.getStartNode();
					Node u2 = e1.getEndNode();
					int u1Index = sourceGraph.indexOf(u1);
					int u2Index = sourceGraph.indexOf(u2);
					usedOfG1[u1Index]=1;
					usedOfG1[u2Index]=1;
					matrix[u1Index][tSize+u1Index] = this.cf.getNodeCosts();
					matrix[u2Index][tSize+u2Index] = this.cf.getNodeCosts();
				}
			} else {
				if (structMatching[i][1] < this.edges2.size()){
					Edge e2 = this.edges2.get(structMatching[i][1]);
					Node v1 = e2.getStartNode();
					Node v2 = e2.getEndNode();
					int v1Index = targetGraph.indexOf(v1);
					int v2Index = targetGraph.indexOf(v2);
					usedOfG2[v1Index]=1;
					usedOfG2[v2Index]=1;
					matrix[sSize+v1Index][v1Index] = this.cf.getNodeCosts();
					matrix[sSize+v2Index][v2Index] = this.cf.getNodeCosts();
				}
			}	
		}
		// handle isolated nodes!
		for (int i = 0; i < usedOfG1.length; i++){
			if (usedOfG1[i]==0){
				for (int j = 0; j < tSize; j++){
					// TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
					matrix[i][j] = this.cf.getCost(sourceGraph.get(i), targetGraph.get(j));
					matrix[i][tSize+i] = this.cf.getNodeCosts();
				}
			}
		}
		for (int j = 0; j < usedOfG2.length; j++){
			if (usedOfG2[j]==0){
				for (int i = 0; i < sSize; i++){
					// TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
					matrix[i][j] = this.cf.getCost(sourceGraph.get(i), targetGraph.get(j));
					matrix[sSize+j][j] = this.cf.getNodeCosts();
				}
			}
		}
		
		if (sSize != tSize && this.edges1.size()==this.edges2.size()){
			for (int i = 0; i < sSize; i++){
				matrix[i][tSize+i] = this.cf.getNodeCosts();
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





	public double[][] getSimpleMatrix(Graph sourceGraph, Graph targetGraph) {
		this.source = sourceGraph;
		this.target = targetGraph;
		this.sourceEdges = this.source.getAdjacenyMatrix();
		this.targetEdges = this.target.getAdjacenyMatrix();
		
		
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
				if (this.adj.equals("worst")){
					costs += u.getEdges().size()*cf.getEdgeCosts();
					costs += v.getEdges().size()*cf.getEdgeCosts();
				}
				if (this.adj.equals("best")){
					// adjacency information is added to the node costs
					edgeMatrix = this.getEdgeMatrix(u, v);
					costs += this.ha.hgAlgorithmOnlyCost(edgeMatrix);
				}
				
				matrix[i][j] = costs;
			}
		}

		for (int i = sSize; i < dim; i++) {
			for (int j = 0; j < tSize; j++) {
					v = (Node) this.target.get(j);
					double costs = cf.getNodeCosts();
					if (this.adj.equals("worst") || this.adj.equals("best")){
						double f = v.getEdges().size();
						costs += (f * cf.getEdgeCosts());
					}
					matrix[i][j] = costs;
				
			}
		}
	
		
		for (int i = 0; i < sSize; i++) {
			u = (Node) this.source.get(i);
			for (int j = tSize; j < dim; j++) {
			
					double costs = cf.getNodeCosts();
					if (this.adj.equals("worst") || this.adj.equals("best")){
						double f = u.getEdges().size();
						costs += (f * cf.getEdgeCosts());
					}
					matrix[i][j] = costs;
			}
		}
		
		
		for (int i = sSize; i < dim; i++) {
			for (int j = tSize; j < dim; j++) {
				matrix[i][j] =0.0;
			}
		}
		
		
//		this.printMatrix(matrix);
//		System.out.println("******");
		return matrix;
	}
	

	
	
	public double[][] getSimpleMatrix2(Graph sourceGraph, Graph targetGraph) {
		this.source = sourceGraph;
		this.target = targetGraph;
		this.sourceEdges = this.source.getAdjacenyMatrix();
		this.targetEdges = this.target.getAdjacenyMatrix();
		int sSize = sourceGraph.size();
		int tSize = targetGraph.size();
		int dim = sSize + tSize;
		double[][] matrix = new double[dim][dim];
		
		Node u;
		Node v;

		for (int i = 0; i < sSize; i++) {
			u = (Node) this.source.get(i);
			for (int j = 0; j < tSize; j++) {
				v = (Node) this.target.get(j);
				// TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
				double costs = cf.getCost(u, v);
				matrix[i][j] = costs;
			}
		}

		for (int i = sSize; i < dim; i++) {
			for (int j = 0; j < tSize; j++) {
					v = (Node) this.target.get(j);
					double costs = cf.getNodeCosts();
					double f = v.getEdges().size();
					costs += (f * cf.getEdgeCosts());
					matrix[i][j] = costs;
			}
		}
	
		
		for (int i = 0; i < sSize; i++) {
			u = (Node) this.source.get(i);
			for (int j = tSize; j < dim; j++) {
				double costs = cf.getNodeCosts();
				double f = u.getEdges().size();
				costs += (f * cf.getEdgeCosts());
				matrix[i][j] = costs;
			}
		}
		for (int i = sSize; i < dim; i++) {
			for (int j = tSize; j < dim; j++) {
				matrix[i][j] =0.0;
			}
		}
		return matrix;
	}





	public double[][] getMoreAdjacencyMatrix(Graph g1, Graph g2) {
		Edge[][] a1 = g1.getAdjacenyMatrix();
		Edge[][] a2 = g2.getAdjacenyMatrix();
		this.edges1 = new LinkedList<Edge>();
		this.edges2 = new LinkedList<Edge>();
		for (int i = 0; i < a1.length-1; i++){
			for (int j = i+1; j < a1.length;j++){
				if (a1[i][j]!=null){
					edges1.add(a1[i][j]);
				}
			}
		}
		for (int i = 0; i < a2.length-1; i++){
			for (int j = i+1; j < a2.length;j++){
				if (a2[i][j]!=null){
					edges2.add(a2[i][j]);
				}
			}
		}
		int sSize = edges1.size();
		int tSize = edges2.size();
		int dim = sSize + tSize;
		double[][] matrix = new double[dim][dim];
		double[][] edgeMatrix;
		
		
		Edge e1;
		Edge e2;
		Node u1, u2, v1,v2;

		for (int i = 0; i < sSize; i++) {
			e1 = (Edge) edges1.get(i);
			for (int j = 0; j < tSize; j++) {
				e2 = (Edge) edges2.get(j);
				double costs = 0;
				double alternativeCosts = 0;
				u1 = e1.getStartNode();
				u2 = e1.getEndNode();
				v1 = e2.getStartNode();
				v2 = e2.getEndNode();
				// TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
				costs += this.cf.getCost(u1, v1);
				costs += this.cf.getCost(u2, v2);
				edgeMatrix = this.getEdgeMatrix(u1, v1);
				costs += this.ha.hgAlgorithmOnlyCost(edgeMatrix);
				edgeMatrix = this.getEdgeMatrix(u2, v2);
				costs += this.ha.hgAlgorithmOnlyCost(edgeMatrix);
				// TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
				alternativeCosts += this.cf.getCost(u1, v2);
				alternativeCosts += this.cf.getCost(u2, v1);
				edgeMatrix = this.getEdgeMatrix(u1, v2);
				alternativeCosts += this.ha.hgAlgorithmOnlyCost(edgeMatrix);
				edgeMatrix = this.getEdgeMatrix(u2, v1);
				alternativeCosts += this.ha.hgAlgorithmOnlyCost(edgeMatrix);
				costs = Math.min(costs, alternativeCosts);
				costs+= this.cf.getCost(e1, e2);	
				matrix[i][j] = costs;
			}		
		}
		for (int i = sSize; i < dim; i++) {
			for (int j = 0; j < tSize; j++) {
				if ((i - sSize) == j) {
					e2 = (Edge) edges2.get(j);
					v1 = e2.getStartNode();
					v2 = e2.getEndNode();
					double costs = 2 * cf.getNodeCosts();
					costs += cf.getEdgeCosts();
					costs += (cf.getEdgeCosts() * v1.getEdges().size());
					costs += (cf.getEdgeCosts() * v2.getEdges().size());
					matrix[i][j] = costs;
				} else {
					matrix[i][j] = Double.POSITIVE_INFINITY;
				}
			}
		}
		for (int i = 0; i < sSize; i++) {
			for (int j = tSize; j < dim; j++) {
				if ((j - tSize) == i) {
					e1 = (Edge) edges1.get(i);
					u1 = e1.getStartNode();
					u2 = e1.getEndNode();
					double costs = 2 * cf.getNodeCosts();
					costs += cf.getEdgeCosts();
					costs += (cf.getEdgeCosts() * u1.getEdges().size());
					costs += (cf.getEdgeCosts() * u2.getEdges().size());
					matrix[i][j] = costs;
				} else {
					matrix[i][j] = Double.POSITIVE_INFINITY;
				}
			}
		}
		return matrix;		
	}





	public void updateCostMatrix(double[][] cm, int[][] assignment, int c) {
		int from = assignment[c][0]; 
		int to = assignment[c][1];
		
		for (int i = c+1; i < this.source.size(); i++){
			if (from < this.source.size()){
				if (to < this.target.size()){
					// substitution: from-->to
					if (this.sourceEdges[i][from] != null){
						for (int j = 0; j < this.target.size(); j++){
							if (this.targetEdges[j][to] != null){
								cm[i][j] += cf.getCost(this.sourceEdges[i][from], this.targetEdges[j][to]);
							} else {
								cm[i][j] += cf.getEdgeCosts();
							}
						}
					} else {
						for (int j = 0; j < this.target.size(); j++){
							if (this.targetEdges[j][to] != null){
								cm[i][j] += cf.getEdgeCosts();
							} 
						}
					}
				} else {
					// deletion: from-->eps
					if (this.sourceEdges[i][from] != null){
						for (int j = 0; j < this.target.size(); j++){
							cm[i][j] += cf.getEdgeCosts();
						}
					}
				}
				
			} else {
				if (to < this.target.size()){
					// insertion
					for (int j = 0; j < this.target.size(); j++){
						if (this.targetEdges[j][to] != null){
							cm[i][j] += cf.getEdgeCosts();
						}
					}
				}
			}		
		}

	
	}




	public double[][] getUtilityMatrix(double[][] costMatrix) {
		
		
		double[] maxOfRows = new double[costMatrix.length];
		double[] minOfRows = new double[costMatrix.length];
		double[] maxOfCols = new double[costMatrix.length];
		double[] minOfCols = new double[costMatrix.length];
		
		Arrays.fill(maxOfRows, Double.NEGATIVE_INFINITY);
		Arrays.fill(maxOfCols, Double.NEGATIVE_INFINITY);
		Arrays.fill(minOfRows, Double.POSITIVE_INFINITY);
		Arrays.fill(minOfCols, Double.POSITIVE_INFINITY);
		for (int i = 0; i < costMatrix.length; i++){
			for (int j = 0; j < costMatrix.length; j++){
				if (costMatrix[i][j] > maxOfRows[i]){
					maxOfRows[i] = costMatrix[i][j];
				}
				if (costMatrix[i][j] < minOfRows[i]){
					minOfRows[i] = costMatrix[i][j];
				}
				if (costMatrix[i][j] > maxOfCols[j]){
					maxOfCols[j] = costMatrix[i][j];
				}
				if (costMatrix[i][j] < minOfCols[j]){
					minOfCols[j] = costMatrix[i][j];
				}
			}
		}
		
		double utilityMatrix[][] = new double[costMatrix.length][costMatrix.length];
		for (int i = 0; i < costMatrix.length; i++){
			for (int j = 0; j < costMatrix.length; j++){
				if (minOfRows[i] != maxOfRows[i] && minOfCols[j] != maxOfCols[j])
					utilityMatrix[i][j] = (minOfRows[i]+maxOfRows[i] - 2 * costMatrix[i][j])/(maxOfRows[i]-minOfRows[i])+(minOfCols[j]+maxOfCols[j] - 2 * costMatrix[i][j])/(maxOfCols[j]-minOfCols[j]);
				else
					utilityMatrix[i][j] = 0;
			}
		}
//		this.printMatrix(utilityMatrix);
		
		
		return utilityMatrix;
	}
}
