/**
 * 
 */
package util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeSet;

import andreas.EditPath;
import andreas.EditPathFactory;
import costs.CostFunctionManager;
import costs.normalisation.NormalisationFunction;
import graph.Edge;
import graph.Graph;
import graph.Node;
import graph.TreeNode;

/**
 * @author riesen
 *
 */
public class EditDistance {
	/**
	 * whether or not the edges are directed
	 */

	private boolean undirected;
	
	/**
	 * whether or not the editpath found is outputted on the console
	 */
	private int outputEditpath;

	private EditPath editPath; //andreas
	public EditPath getEditPath() {
		return editPath;
	}

	/**
	 * constructor
	 * @param undirected
	 * @param outputEditpath
	 * @param bipartiteMatching 
	 * @param matrixGenerator 
	 */
	public EditDistance(int undirected, int outputEditpath) {
		this.undirected = undirected == 1;
		this.outputEditpath = outputEditpath;	
	}

	/**
	 * 
	 * @return the approximated edit distance between graph @param g1
	 * and graph @param g2 according to the @param matching using the cost function @param cf
	 */
	public double getEditDistance(Graph g1, Graph g2, int[][] matching, CostFunctionManager cf) {
		// if the edges are undirected
		// all of the edge operations have to be multiplied by 0.5
		// since all edge operations are performed twice 
		double factor = 1.0;
		if (this.undirected == true){
			factor = 0.5;
		}
		if (this.outputEditpath == 1){
			System.out.println("\nThe Edit Path:");
		}
		double ed = 0.;
		// the edges of graph g1 and graph g2
		Edge[][] edgesOfG1 = g1.getAdjacencyMatrix();
		Edge[][] edgesOfG2 = g2.getAdjacencyMatrix();
		
		for (int i = 0; i < matching.length; i++){
			if (matching[i][0] < g1.size()){
				if (matching[i][1] < g2.size()){
					// i-th node substitution with node from g2 with index matching[i][1]
					// TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
					ed += cf.getCost(g1.get(matching[i][0]), g2.get(matching[i][1]));
					if (this.outputEditpath ==1){
						System.out.println(g1.get(matching[i][0]).getNodeID()+"\t-->\t"+ g2.get(matching[i][1]).getNodeID()+"\t"+
								// TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
								cf.getCost(g1.get(matching[i][0]), g2.get(matching[i][1])));
					}
					// edge handling when i-th node is substituted with node with index matching[i][i];
					// iterating through all possible edges e_ij of node i
					for (int j = 0; j < matching.length; j++){
						if (j < edgesOfG1[0].length){
							Edge e_ij = edgesOfG1[matching[i][0]][j];
							if (matching[j][1] < g2.size()){
								// node with index j is NOT deleted but subtituted
								Edge e_ij_mapped = edgesOfG2[matching[i][1]][matching[j][1]];
								if (e_ij != null){
									if (e_ij_mapped != null){
										// case 1:
										// there is an edge between the i-th and j-th node (e_ij)
										// AND there is an edge between the mappings of the i-th and j-th node (e_ij_mapped)
										ed += factor * cf.getCost(e_ij, e_ij_mapped); // substitute the edge
										if (this.outputEditpath==1){
											System.out.println(e_ij.getEdgeID() +"\t-->\t"+e_ij_mapped.getEdgeID()+"\t"+factor * cf.getCost(e_ij, e_ij_mapped));
										}
									} else {
										// case 2:
										// there is an edge between the i-th and j-th node (e_ij)
										// BUT there is NO edge between the mappings of the i-th and j-th node (e_ij_mapped=null)
										ed += factor * cf.getEdgeCosts(); // delete the edge
										if (this.outputEditpath==1){
											System.out.println(e_ij.getEdgeID()+"\t-->\teps\t"+factor * cf.getEdgeCosts());
										}
									}
								} else {
									if (e_ij_mapped != null){
										// case 3:
										// there is NO edge between the i-th and j-th node
										// BUT there is an edge between the mappings
										if (this.outputEditpath==1){
											System.out.println("eps\t-->\t"+e_ij_mapped.getEdgeID()+"\t"+factor * cf.getEdgeCosts());
										}
										ed += factor * cf.getEdgeCosts(); // insert the edge
									} 
								}
								
							} else {
								// node with index j is deleted
								if (e_ij != null){
									// case 4:
									// there is an edge between the i-th and j-th node and the j-th node is deleted
									ed += factor * cf.getEdgeCosts(); // delete the edge
									if (this.outputEditpath==1){
										System.out.println(e_ij.getEdgeID()+"\t-->\teps\t"+factor * cf.getEdgeCosts());
									}
								}
							}
						} else {
							// node with index j is inserted
							if (matching[j][1] < g2.size()){
								Edge e_ij_mapped = edgesOfG2[matching[i][1]][matching[j][1]];
								if (e_ij_mapped != null){
									// case 5:
									// there is an edge between the mappings of i and j
									if (this.outputEditpath==1){
										System.out.println("eps\t-->\t"+e_ij_mapped.getEdgeID()+"\t"+factor * cf.getEdgeCosts());
									}
									ed += factor * cf.getEdgeCosts(); // insert the edge
								} 
							}
						}
					}
				} else{
					// i-th node deletion
					ed += cf.getNodeCosts();
					if (this.outputEditpath==1){
						System.out.println(g1.get(matching[i][0]).getNodeID()+"\t-->\t"+ "eps\t"+cf.getNodeCosts());
					}
					// edge handling
					for (int j = 0; j < edgesOfG1[0].length; j++){
						Edge e_ij = edgesOfG1[matching[i][0]][j];
						if (e_ij != null){
							// case 6:
							// there is an edge between the i-th and j-th node
							ed += factor * cf.getEdgeCosts(); // delete the edge
							if (this.outputEditpath==1){
								System.out.println(e_ij.getEdgeID()+"\t-->\teps\t"+ factor *cf.getEdgeCosts());
							}	
						}
					}
				}
			} else{
				if (matching[i][1] < g2.size()){
					// i-th node insertion
					ed += cf.getNodeCosts();
					if (this.outputEditpath==1){
						System.out.println("eps\t-->\t"+ g2.get(matching[i][1]).getNodeID()+"\t"+cf.getNodeCosts());
					}
					// edge handling
					for (int j = 0; j < edgesOfG2[0].length; j++){
						Edge e_ij_mapped = edgesOfG2[matching[i][1]][j];
						if (e_ij_mapped != null){
							// case 7:
							// there is an edge between the mapping of the i-th and j-th node
							ed += factor * cf.getEdgeCosts(); // insert the edge
							if (this.outputEditpath==1){
								System.out.println("eps\t-->\t"+e_ij_mapped.getEdgeID()+"\t"+ factor *cf.getEdgeCosts());
							}
						}
					}				
				}
			}
		}
		if (this.outputEditpath==1){
			System.out.println("The Edit Distance: "+ed);
		}
		//andreas
		EditPathFactory epf = new EditPathFactory();
		this.editPath = epf.fromBipartite(g1, g2, matching, cf);
		return ed;
	}
	
	/**
	 * 
	 * @return the exact edit distance between graph @param g1
	 * and graph @param g2 using the cost function @param cf
	 * s is the maximum number of open paths used in beam-search
	 */
	public double getEditDistance(Graph g1, Graph g2, CostFunctionManager cf, int s) {
		// list of partial edit paths (open) organized as TreeSet
		TreeSet<TreeNode> open = new TreeSet<TreeNode>();

		// if the edges are undirected
		// all of the edge operations have to be multiplied by 0.5
		// since all edge operations are performed twice 
		double factor = 1.0;
		if (this.undirected == true){
			factor = 0.5;
		}
				
		// each treenode represents a (partial) solution (i.e. edit path)
		// start is the first (empty) partial solution
		TreeNode start = new TreeNode(g1, g2, cf, factor);
		open.add(start);
		
		// main loop of the tree search
		while (!open.isEmpty()){	
			TreeNode u = open.pollFirst(); 
			if (u.allNodesUsed()){
				//andreas
				EditPathFactory epf = new EditPathFactory();
				this.editPath = epf.fromAStar(u);
				return u.getCost();
			}
			// generates all successors of node u in the search tree
			// and add them to open
			LinkedList<TreeNode> successors = u.generateSuccessors();
			open.addAll(successors);
			
			// in beam search the maximum number of open paths
			// is limited to s
			while (open.size() > s){
				open.pollLast();
			}		
		}
		// error case
		return -1;
	}

	public double[] getNormalisedEditDistance(Graph sourceGraph, Graph targetGraph, int[][] matching, CostFunctionManager cf, NormalisationFunction normalisationFunction){
		double GED = this.getEditDistance(sourceGraph, targetGraph, matching, cf);
		return getNormalisedEditDistance(sourceGraph,targetGraph,GED,normalisationFunction);
	}

	public double[] getNormalisedEditDistance(Graph sourceGraph, Graph targetGraph, double GED, NormalisationFunction normalisationFunction) {

		// Get all edges from the source graph
		HashSet<Edge> sourceGraphEdges = new HashSet<>();
		for (Node node : sourceGraph) {
			for (Edge edge : node.getEdges()) {
				sourceGraphEdges.add(edge);
			}
		}

		// Get all edges from the target graph
		HashSet<Edge> targetGraphEdges = new HashSet<>();
		for (Node node : targetGraph) {
			for (Edge edge : node.getEdges()) {
				targetGraphEdges.add(edge);
			}
		}

		int n1 = sourceGraph.size();
		int e1 = sourceGraphEdges.size();

		int n2 = targetGraph.size();
		int e2 = targetGraphEdges.size();

		double normalisedGED = normalisationFunction.normaliseGED(GED, n1, n2, e1, e2);

		double[] distances = new double[2];
		distances[0] = GED;
		distances[1] = normalisedGED;

		return distances;
	}
}
