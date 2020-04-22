package andreas;

import algorithms.HungarianAlgorithm;
import costs.CostFunctionManager;

public class HeuristicAED extends AStarHeuristic {

	private HungarianAlgorithm ha;
	private double[][] fsub;
	private double[] fdel;
	private double[] fins;
	
	public HeuristicAED(AStarGraph g1, AStarGraph g2, CostFunctionManager cf) {
		super(g1, g2, cf);
		initAED();
	}
	
	@Override
	public void heuristicFunction(AStarMap map) {
		map.setFutureCost(calculateAED(map));
	}
	
	// EXECUTED IN THE CONSTRUCTOR BEFORE ASTAR
	
	private void initAED() {
		ha = new HungarianAlgorithm();
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
				double subE = getEdgeAED(i, j, g1.getAdjacentNodes(i), g2.getAdjacentNodes(j));
				fsub[i][j] = subN + 0.5 * subE;
			}
		}
	}
	
	private double getEdgeAED(int iStart, int jStart, AdjacentNodes adjNodes1, AdjacentNodes adjNodes2) {
		int sizeE1 = adjNodes1.size();
		int sizeE2 = adjNodes2.size();
		if (sizeE1 == 0 && sizeE2 == 0) {
			return 0;
		}
		double[][] gsub = new double[sizeE1][sizeE2];
		double[] gdel = new double[sizeE1];
		double[] gins = new double[sizeE2];
		for (int i=0; i < sizeE1; i++) {
			gdel[i] = cf.getEdgeCosts();
		}
		for (int j=0; j < sizeE2; j++) {
			gins[j] = cf.getEdgeCosts();
		}
		for (int i=0; i < sizeE1; i++) {
			int iEnd = adjNodes1.getAdjacentNode(i);
			boolean iIsOutgoing = adjNodes1.isOutgoing(i);
			for (int j=0; j < sizeE2; j++) {
				int jEnd = adjNodes2.getAdjacentNode(j);
				boolean jIsOutgoing = adjNodes2.isOutgoing(j);
				if (iIsOutgoing == jIsOutgoing) {
					gsub[i][j] = cf.getCost(g1.getEdge(iStart, iEnd, iIsOutgoing), g2.getEdge(jStart, jEnd, jIsOutgoing));
				} else {
					gsub[i][j] = Double.MAX_VALUE;
				}
			}
		}
		double[][] cost = getCostMatrix(gsub, gdel, gins);
		return ha.hgAlgorithmOnlyCost(cost);
	}
	
	private double[][] getCostMatrix(double[][] sub, double[] del, double [] ins) {
		int n1 = del.length;
		int n2 = ins.length;
		double[][] cost = new double[n1+n2][n1+n2];
		for (int i=0; i < n1; i++) {
			System.arraycopy(sub[i], 0, cost[i], 0, n2);
		}
		for (int i=n1; i < n1+n2; i++) {
			for (int j=0; j < n2; j++) {
				if (i-n1 == j) {
					cost[i][j] = ins[j];
				} else {
					cost[i][j] = Double.MAX_VALUE;
				}
			}
		}
		for (int i=0; i < n1; i++) {
			for (int j=n2; j < n1+n2; j++) {
				if (i == j-n2) {
					cost[i][j] = del[i];
				} else {
					cost[i][j] = Double.MAX_VALUE;
				}
			}
		}
		for (int i=n1; i < n1+n2; i++) {
			for (int j=n2; j < n1+n2; j++) {
				cost[i][j] = 0;
			}
		}
		return cost;
	}
	
	// EXECUTED AT RUNTIME DURING ASTAR
	
	private double calculateAED(AStarMap map) {
		int n1 = map.sizeFreeG1();
		int n2 = map.sizeFreeG2();
		double[][] cost = new double[n1+n2][n1+n2];
		for (int i=0; i < n1; i++) {
			int i0 = map.getFreeG1(i);
			for (int j=0; j < n2; j++) {
				int j0 = map.getFreeG2(j);
				cost[i][j] = fsub[i0][j0];
			}
		}
		for (int i=n1; i < n1+n2; i++) {
			for (int j=0; j < n2; j++) {
				if (i-n1 == j) {
					int j0 = map.getFreeG2(j);
					cost[i][j] = fins[j0];
				} else {
					cost[i][j] = Double.MAX_VALUE;
				}
			}
		}
		for (int i=0; i < n1; i++) {
			for (int j=n2; j < n1+n2; j++) {
				if (i == j-n2) {
					int i0 = map.getFreeG1(i);
					cost[i][j] = fdel[i0];
				} else {
					cost[i][j] = Double.MAX_VALUE;
				}
			}
		}
		for (int i=n1; i < n1+n2; i++) {
			for (int j=n2; j < n1+n2; j++) {
				cost[i][j] = 0;
			}
		}
		return ha.hgAlgorithmOnlyCost(cost);
	}

}
