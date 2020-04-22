package andreas;

import costs.CostFunctionManager;

public abstract class AStarHeuristic {

	protected AStarGraph g1;
	protected AStarGraph g2;
	protected CostFunctionManager cf;
	
	public AStarHeuristic(AStarGraph g1, AStarGraph g2, CostFunctionManager cf) {
		this.g1 = g1;
		this.g2 = g2;
		this.cf = cf;
	}
	
	public abstract void heuristicFunction(AStarMap map);
	
}
