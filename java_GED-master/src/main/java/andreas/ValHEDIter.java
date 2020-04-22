package andreas;

import costs.CostFunctionManager;
import graph.Graph;

public class ValHEDIter {

	private HEDViz hed;
	private EditPathFactory epf;
	private EditPath ep;
	
	private int maxIter;
	private int iter;
	private int num1;
	private int num2;
	private int[] map12;
	private int[] map21;
	private double[][] nf;
	
	public ValHEDIter(int maxIter) {
		hed = new HEDViz();
		epf = new EditPathFactory();
		ep = null;
		this.maxIter = maxIter;
	}
	
	// public
	
	public double getEditDistance(Graph g1, Graph g2, CostFunctionManager cf) {
		num1 = g1.size();
		num2 = g2.size();
		map12 = new int[num1]; for (int i=0; i < num1; i++) { map12[i] = -1; }
		map21 = new int[num2]; for (int j=0; j < num2; j++) { map21[j] = -1; }
		nf = hed.getNodeCosts(g1, g2, cf);
		
		iter = 0;
		int sub = 0;
		int prev = 0;
		do {
			iter += 1;
			prev = sub;
			hdNodes12();
			hdNodes21();
			sub = getNumSub();
		} while (iter < maxIter && sub != prev);
		hdFinalize();
		
		ep = epf.fromHausdorffUniqueAssignments(g1, g2, map12, map21, cf);
		return ep.getCost();
	}
	
	public EditPath getEditPath(Graph g1, Graph g2) {
		return ep;
	}
	
	public int getIter() {
		return iter;
	}
	
	// private
	
	private void hdNodes12() {
		for (int i=0; i < num1; i++) {
			if (!isSub12(i)) {
				double best = nf[i][num2];
				int map = -1;
				for (int j=0; j < num2; j++) {
					if (!isSub21(j)) {
						double curr = nf[i][j] / 2;
						if (curr < best) {
							best = curr;
							map = j;
						}
					}
				}
				map12[i] = map;
			}
		}
	}
	
	private void hdNodes21() {
		for (int j=0; j < num2; j++) {
			if (!isSub21(j)) {
				double best = nf[num1][j];
				int map = -1;
				for (int i=0; i < num1; i++) {
					if (!isSub12(i)) {
						double curr = nf[i][j] / 2;
						if (curr < best) {
							best = curr;
							map = i;
						}
					}
				}
				map21[j] = map;
			}
		}
	}
	
	private void hdFinalize() {
		for (int i=0; i < num1; i++) {
			if (!isSub12(i)) {
				map12[i] = -1;
			}
		}
		for (int j=0; j < num2; j++) {
			if (!isSub21(j)) {
				map21[j] = -1;
			}
		}
	}
	
	private int getNumSub() {
		int numSub = 0;
		for (int i=0; i < map12.length; i++) {
			if (isSub12(i)) {
				numSub += 1;
			}
		}
		return numSub;
	}
	
	private boolean isSub12(int i) {
		return map12[i] >= 0 && map21[map12[i]] == i;
	}
	
	private boolean isSub21(int j) {
		return map21[j] >= 0 && map12[map21[j]] == j;
	}
	
}
