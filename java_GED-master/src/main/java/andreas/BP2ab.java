package andreas;

import costs.CostFunctionManager;
import graph.Graph;

public class BP2ab {

	private HEDViz hed;
	private EditPathFactory epf;
	private EditPath ep;
	
	private int num1;
	private int num2;
	private int[] map12;
	private int[] map21;
	private double[][] nf;
	
	public BP2ab() {
		hed = new HEDViz();
		epf = new EditPathFactory();
		ep = null;
	}
	
	// public
	
	public double getEditDistance(Graph g1, Graph g2, CostFunctionManager cf) {
		num1 = g1.size();
		num2 = g2.size();
		map12 = new int[num1]; 
		map21 = new int[num2];
		nf = hed.getNodeCosts(g1, g2, cf);
		
//		System.out.println(nf.length + "/" + (num1 + 1));
//		System.out.println(nf[0].length + "/" + (num2 + 1));
//		for (int i=0; i < num1 + 1; i++) {
//			String str = "";
//			for (int j=0; j < num2 + 1; j++) {
//				str += nf[i][j] + " ";
//			}
//			System.out.println(str);
//		}
		
		for (int i=0; i < num1; i++) { map12[i] = -1; }
		for (int j=0; j < num2; j++) { map21[j] = -1; }
		hdNodes12();
		ep = epf.fromHausdorffUniqueAssignments(g1, g2, map12, map21, cf);
		return ep.getCost();
	}
	
	public EditPath getEditPath(Graph g1, Graph g2) {
		return ep;
	}
	
	// private
	
	private void hdNodes12() {
		for (int i=0; i < num1; i++) {
			int map = -1;
			double bestSub = -1;
			double del = nf[i][num2];
			for (int j=0; j < num2; j++) {
				if (map21[j] < 0) {
					double sub = nf[i][j];
					double ins = nf[num1][j];
					if ((sub < del + ins) && ((map < 0) || (sub < bestSub))) {
						map = j;
						bestSub = sub;
					}
				}
			}
			if (map >= 0) {
				map12[i] = map;
				map21[map] = i;
			}
		}
	}
	
}
