package andreas;

import costs.CostFunctionManager;
import kaspar.GreedyMatching;
import kaspar.GreedyMatrixGenerator;
import graph.Graph;

// upper greedy bound
public class UGB {
	
	private GreedyMatching gm;
	private EditPathFactory epf;
	
	private String version;
	private int[][] matching;
	
	public UGB(String version) {
		gm = new GreedyMatching();
		epf = new EditPathFactory();
		this.version = version;
		matching = null;
	}
	
	// public
	
	public double getEditDistance(Graph g1, Graph g2, CostFunctionManager cf) {
		// greedy cost matrix
		GreedyMatrixGenerator gmg = new GreedyMatrixGenerator(cf, 0, 0);
		gmg.setAdj("best");
		double[][] costMatrix = gmg.getSimpleMatrix(g1, g2);
		
		// greedy assignment
		if (version.compareTo("1a") == 0) { // greedy
			matching = gm.getMatching(costMatrix); 
		} else if (version.compareTo("2a") == 0) { // greedy refined
			matching = gm.getRefinedMatching(costMatrix);
		} else if (version.compareTo("1c") == 0) { // greedy tie
			matching = gm.getMatchingTieResolution(costMatrix);
		} else if (version.compareTo("2b") == 0) { // greedy refined tie
			matching = gm.getRefinedMatchingTie(costMatrix);
		}
		
		// valid edit path
		EditPath ep = getEditPath(g1, g2, cf);
		return ep.getCost();
	}
	
	public EditPath getEditPath(Graph g1, Graph g2, CostFunctionManager cf) {
		return epf.fromBipartite(g1, g2, matching, cf);
	}
	
}
