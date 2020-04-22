package andreas;

import costs.CostFunctionManager;
import graph.Graph;

public class ValHEDContextGreedy {

	private HEDContext hed;
	private EditPathFactory epf;
	private EditPath ep;
	
	private int radius;
	private int num1;
	private int num2;
	private int[] map12;
	private int[] map21;
	private double[][] nf;
	
	public ValHEDContextGreedy(int radius) {
		hed = null;
		epf = new EditPathFactory();
		ep = null;
		this.radius = radius;
	}
	
	// public
	
	public double getEditDistance(Graph g1, Graph g2, CostFunctionManager cf) {
		num1 = g1.size();
		num2 = g2.size();
		map12 = new int[num1]; 
		map21 = new int[num2];
		
		hed = new HEDContext(g1, g2, cf, radius);
		nf = hed.generateContextCostMatrix();

		for (int i=0; i < num1; i++) { map12[i] = -1; }
		for (int j=0; j < num2; j++) { map21[j] = -1; }
		
		hdNodes12();
		hdNodes21();
		EditPath ep1 = epf.fromHausdorffUniqueAssignments(g1, g2, map12, map21, cf);
		double cost1 = ep1.getCost();
		
		for (int i=0; i < num1; i++) { map12[i] = -1; }
		for (int j=0; j < num2; j++) { map21[j] = -1; }
		
		hdNodes21();
		hdNodes12();
		EditPath ep2 = epf.fromHausdorffUniqueAssignments(g1, g2, map12, map21, cf);
		double cost2 = ep2.getCost();
		
		if (cost2 < cost1) {
			ep = ep2;
			return cost2;
		} else {
			ep = ep1;
			return cost1;
		}
	}
	
	public EditPath getEditPath(Graph g1, Graph g2) {
		return ep;
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
