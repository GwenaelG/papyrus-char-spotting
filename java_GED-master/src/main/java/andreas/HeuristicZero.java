package andreas;

import costs.CostFunctionManager;

public class HeuristicZero extends AStarHeuristic {

	public HeuristicZero(AStarGraph g1, AStarGraph g2, CostFunctionManager cf) {
		super(g1, g2, cf);
	}
	
	@Override
	public void heuristicFunction(AStarMap map) {
		map.setFutureCost(0);
	}

}
