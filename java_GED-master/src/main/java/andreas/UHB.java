package andreas;

import costs.CostFunctionManager;
import graph.Graph;

// upper Hausdorff bound
public class UHB {

	private HEDViz hed;
	private EditPathFactory epf;
	
	private int num1;
	private int num2;
	private int[] map12;
	private int[] map21;
	private double[][] nf;
	
	public UHB() {
		hed = new HEDViz();
		epf = new EditPathFactory();
	}
	
	// public
	
	public double getEditDistance(Graph g1, Graph g2, CostFunctionManager cf) {
		// Hausdorff cost matrix
		nf = hed.getNodeCosts(g1, g2, cf);
		
		// greedy assignment
		num1 = g1.size();
		num2 = g2.size();
		map12 = new int[num1]; 
		map21 = new int[num2];
		for (int i=0; i < num1; i++) { map12[i] = -1; }
		for (int j=0; j < num2; j++) { map21[j] = -1; }
		hdNodes12();
		hdNodes21();
		
		// valid edit path
		EditPath ep = getEditPath(g1, g2, cf);
		return ep.getCost();
	}
	
	public EditPath getEditPath(Graph g1, Graph g2, CostFunctionManager cf) {
		return epf.fromHausdorffUniqueAssignments(g1, g2, map12, map21, cf);
	}
	
	// private
	
	private void hdNodes12() {
		for (int i=0; i < num1; i++) {
			if (map12[i] < 0) {
				double best = nf[i][num2];
				int map = -1;
				for (int j=0; j < num2; j++) {
					if (map21[j] < 0) {
						double curr = nf[i][j] / 2;
						if (curr < best) {
							best = curr;
							map = j;
						}
					}
				}
				map12[i] = map;
				if (map >= 0) {
					map21[map] = i;
				}
			}
		}
	}
	
	private void hdNodes21() {
		for (int j=0; j < num2; j++) {
			if (map21[j] < 0) {
				double best = nf[num1][j];
				int map = -1;
				for (int i=0; i < num1; i++) {
					if (map12[i] < 0) {
						double curr = nf[i][j] / 2;
						if (curr < best) {
							best = curr;
							map = i;
						}
					}
				}
				map21[j] = map;
				if (map >= 0) {
					map12[map] = j;
				}
			}
		}
	}
	
}
