package andreas;

import java.util.TreeSet;

import costs.CostFunctionManager;
import graph.Edge;
import graph.Graph;

/**
 * @author Andreas Fischer
 */
public class AStarGED {

	public static final long maxMaps = 1000000; // maximum number of AStarMaps allowed to be instantiated in the RAM
	public static long timeNano = 0; // time in nanoseconds elapsed for A* search
	public static long numOpen = 0; // number of maps in the open list at the end of the A* search
	public static long numMatchings = 0; // number of GED computation performed with this class
	public static long numFailures = 0; // number of GED computations that failed due to the maxMaps limit
	public static String failures = ""; // IDs of the graph pairs for which GED computation failed
	
	private AStarGraph g1;
	private AStarGraph g2;
	private CostFunctionManager cf;
	private AStarHeuristic heuristic;
	private AStarMap map;
	private double ged;

	public AStarGED(Graph theG1, Graph theG2, CostFunctionManager theCf, String nameHeuristic) {
		g1 = new AStarGraph(theG1);
		g2 = new AStarGraph(theG2);
		cf = theCf;
		heuristic = getHeuristic(nameHeuristic);
		map = null;
		ged = -1;
	}
	
	private AStarHeuristic getHeuristic(String nameHeuristic) {
		if (nameHeuristic.compareTo("HED") == 0) {
			return new HeuristicHED(g1, g2, cf);
		} else if (nameHeuristic.compareTo("AED") == 0) {
			return new HeuristicAED(g1, g2, cf);
		} else {
			return new HeuristicZero(g1, g2, cf);
		}
	}
	
	public double getGraphEditDistance() {
		if (map == null) {
			calculateGraphEditDistance();
		}
		return ged;
	}
	
	public AStarMap getGraphEditDistanceMap() {
		if (map == null) {
			calculateGraphEditDistance();
		}
		return map;
	}

	private void calculateGraphEditDistance() {
		AStarMap.mapCounter = 0;
		long startTime = System.nanoTime();
		int sizeOpen = doCalculateGraphEditDistance();
		long duration = System.nanoTime() - startTime;
		numMatchings++;
		timeNano += duration;
		numOpen += sizeOpen;
		if (ged < 0) {
			numFailures++;
			failures += " " + g1.getGraph().getGraphID() + "," + g2.getGraph().getGraphID();
		}
	}
	
	/*
	 * Main Method
	 * => AStar search with heuristic future costs
	 */
	private int doCalculateGraphEditDistance() {
		// using a sorted tree set is relevant: log(n) add complexity vs n*log(n) sort complexity if a list would be sorted explicitly
		TreeSet<AStarMap> open = new TreeSet<AStarMap>();
		// add root map
		map = new AStarMap(g1.size(), g2.size());
		completeLeafMap();
		open.add(map);
		// main loop
		while (!open.isEmpty()) {
			// get and remove best map
			AStarMap bestMap = open.pollFirst();
			// return result if map is complete
			if (bestMap.isComplete()) {
				map = bestMap;
				ged = map.getTotalCost();
				return open.size();
			}
			// stop if number of maps is too large
			if (AStarMap.mapCounter > maxMaps) {
				map = null;
				ged = -1;
				return open.size();
			}
			// add successors if map is not complete: successors delete the next node in g1 or assign it to a free node in g2
			int i = bestMap.getIdxG1() + 1;
			for (int k=-1; k < bestMap.sizeFreeG2(); k++) {
				if (k == -1) {
					// add deletion cost
					int j = -1;
					map = new AStarMap(bestMap, i, j);
					map.addMappingCost(cf.getNodeCosts());
					map.addMappingCost(getEdgeDeletionCosts(i));
				} else {
					// add substitution cost
					int j = bestMap.getFreeG2(k);
					map = new AStarMap(bestMap, i, j);
					// TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
					map.addMappingCost(cf.getCost(g1.getNode(i), g2.getNode(j)));
					map.addMappingCost(getEdgeSubstitutionCosts(i, j));
				}
				if (!map.isLeaf()) {
					// estimate future cost with the heuristic
					heuristic.heuristicFunction(map);
				}
				// add map
				completeLeafMap();
				open.add(map);
			}
		}
		// failure
		map = null;
		ged = -1;
		return open.size();
	}
	
	/*
	 * Finalize leaf nodes
	 * => all nodes in g1 have been processed
	 * => add insertion costs for all remaining nodes in g2
	 */
	private void completeLeafMap() {
		if (map.isLeaf()) {
			for (int k=0; k < map.sizeFreeG2(); k++) {
				int j = map.getFreeG2(k);
				map.setInsertedG2(j);
				map.addMappingCost(cf.getNodeCosts());
				map.addMappingCost(getEdgeInsertionCosts(j));
			}
			map.setFutureCost(0);
			map.resetFreeIdxG2();
			map.setComplete();
		}
	}

	/*
	 * Edge Deletion
	 * => edge costs are only added if the adjacent node is already mapped
	 */
	private double getEdgeDeletionCosts(int i) {
		double cost = 0;
		AdjacentNodes adjNodes1 = g1.getAdjacentNodes(i);
		for (int k=0; k < adjNodes1.size(); k++) {
			int iEnd = adjNodes1.getAdjacentNode(k);
			if (!map.isFreeG1(iEnd)) {
				cost += cf.getEdgeCosts(); // deletion
			}
		}
		return cost;
	}
	
	/*
	 * Edge Insertion
	 * => edge costs are only added if the adjacent node is already mapped
	 */
	private double getEdgeInsertionCosts(int j) {
		double cost = 0;
		AdjacentNodes adjNodes2 = g2.getAdjacentNodes(j);
		for (int k=0; k < adjNodes2.size(); k++) {
			int jEnd = adjNodes2.getAdjacentNode(k);
			if (!map.isFreeG2(jEnd)) {
				cost += cf.getEdgeCosts(); // insertion
			}
		}
		return cost;
	}
	
	/*
	 * Edge Substitution
	 * => edge costs are only added if the adjacent node is already mapped
	 */
	private double getEdgeSubstitutionCosts(int i, int j) {
		double cost = 0;
		AdjacentNodes adjNodes1 = g1.getAdjacentNodes(i);
		for (int k=0; k < adjNodes1.size(); k++) {
			int iEnd = adjNodes1.getAdjacentNode(k);
			boolean isOutgoing = adjNodes1.isOutgoing(k);
			if (map.isDeletedG1(iEnd)) {
				cost += cf.getEdgeCosts(); // deletion 1: end node is deleted
			} else if (!map.isFreeG1(iEnd)) {
				int jEnd = map.getMappedG1(iEnd);
				if (!g2.hasEdge(j, jEnd, isOutgoing)) {
					cost += cf.getEdgeCosts(); // deletion 2: no corresponding edge in g2
				} else {
					Edge e1 = g1.getEdge(i, iEnd, isOutgoing);
					Edge e2 = g2.getEdge(j, jEnd, isOutgoing);
					cost += cf.getCost(e1, e2); // substitution
				}
			}
		}
		AdjacentNodes adjNodes2 = g2.getAdjacentNodes(j);
		for (int k=0; k < adjNodes2.size(); k++) {
			int jEnd = adjNodes2.getAdjacentNode(k);
			boolean isOutgoing = adjNodes2.isOutgoing(k);
			if (map.isInsertedG2(jEnd)) {
				cost += cf.getEdgeCosts(); // insertion 1: end node is inserted
			} else if (!map.isFreeG2(jEnd)) {
				int iEnd = map.getMappedG2(jEnd);
				if (!g1.hasEdge(i, iEnd, isOutgoing)) {
					cost += cf.getEdgeCosts(); // insertion 2: no corresponding edge in g1
				}
			}
		}
		return cost;
	}
	
}
