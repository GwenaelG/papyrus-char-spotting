package andreas;

import costs.CostFunctionManager;

public class HeuristicHED extends AStarHeuristic {

	private double[][] fsub;
	private double[] fdel;
	private double[] fins;
	
	public HeuristicHED(AStarGraph g1, AStarGraph g2, CostFunctionManager cf) {
		super(g1, g2, cf);
		initHED();
	}
	
	@Override
	public void heuristicFunction(AStarMap map) {
		map.setFutureCost(calculateHED(map));
	}
	
	// EXECUTED IN THE CONSTRUCTOR BEFORE ASTAR
	
	private void initHED() {
		int sizeN1 = g1.size();
		int sizeN2 = g2.size();
		fsub = new double[sizeN1][sizeN2];
		fdel = new double[sizeN1];
		fins = new double[sizeN2];
		for (int i=0; i < sizeN1; i++) {
			fdel[i] = cf.getNodeCosts() + 0.5 * g1.sizeAdjacent(i) * cf.getEdgeCosts();
		}
		for (int j=0; j < sizeN2; j++) {
			fins[j] = cf.getNodeCosts() + 0.5 * g2.sizeAdjacent(j) * cf.getEdgeCosts();
		}
		for (int i=0; i < sizeN1; i++) {
			for (int j=0; j < sizeN2; j++) {
				// TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
				double subN = cf.getCost(g1.getNode(i), g2.getNode(j));
				double minE = Math.abs(g1.sizeAdjacent(i) - g2.sizeAdjacent(j)) * cf.getEdgeCosts();
				double subE = Math.max(minE, getEdgeHED(i, j, g1.getAdjacentNodes(i), g2.getAdjacentNodes(j)));
				fsub[i][j] = 0.5 * (subN + 0.5 * subE);
			}
		}
	}

	private double getEdgeHED(int iStart, int jStart, AdjacentNodes adjNodes1, AdjacentNodes adjNodes2) {
		int sizeE1 = adjNodes1.size();
		int sizeE2 = adjNodes2.size();
		double[] costE1 = new double[sizeE1];
		for (int i=0; i < sizeE1; i++) {
			costE1[i] = cf.getEdgeCosts();
		}
		double[] costE2 = new double[sizeE2];
		for (int j=0; j < sizeE2; j++) {
			costE2[j] = cf.getEdgeCosts();
		}
		for (int i=0; i < sizeE1; i++) {
			int iEnd = adjNodes1.getAdjacentNode(i);
			boolean iIsOutgoing = adjNodes1.isOutgoing(i);
			for (int j=0; j < sizeE2; j++) {
				int jEnd = adjNodes2.getAdjacentNode(j);
				boolean jIsOutgoing = adjNodes2.isOutgoing(j);
				if (iIsOutgoing == jIsOutgoing) {
					double sub = cf.getCost(g1.getEdge(iStart, iEnd, iIsOutgoing), g2.getEdge(jStart, jEnd, jIsOutgoing)) / 2;
					costE1[i] = Math.min(costE1[i], sub);
					costE2[j] = Math.min(costE2[j], sub);
				}
			}
		}
		double costE = 0;
		for (int i=0; i < sizeE1; i++) {
			costE += costE1[i];
		}
		for (int j=0; j < sizeE2; j++) {
			costE += costE2[j];
		}
		return costE;
	}
	
	// EXECUTED AT RUNTIME DURING ASTAR
	
	private double calculateHED(AStarMap map) {
		int sizeN1 = map.sizeFreeG1();
		int sizeN2 = map.sizeFreeG2();
		double[] costN1 = new double[sizeN1];
		double[] costN2 = new double[sizeN2];
		for (int iN=0; iN < sizeN1; iN++) {
			int i = map.getFreeG1(iN);
			costN1[iN] = fdel[i];
		}
		for (int jN=0; jN < sizeN2; jN++) {
			int j = map.getFreeG2(jN);
			costN2[jN] = fins[j];
		}
		for (int iN=0; iN < sizeN1; iN++) {
			int i = map.getFreeG1(iN);
			for (int jN=0; jN < sizeN2; jN++) {
				int j = map.getFreeG2(jN);
				costN1[iN] = Math.min(costN1[iN], fsub[i][j]);
				costN2[jN] = Math.min(costN2[jN], fsub[i][j]);
			}
		}
		double minN = Math.abs(sizeN1 - sizeN2) * cf.getNodeCosts();
		double costN = 0;
		for (int iN=0; iN < sizeN1; iN++) {
			costN += costN1[iN];
		}
		for (int jN=0; jN < sizeN2; jN++) {
			costN += costN2[jN];
		}
		return Math.max(minN, costN);
	}

}
