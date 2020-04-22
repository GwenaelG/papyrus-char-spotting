package andreas;

import costs.CostFunctionManager;
import graph.Graph;

public class HEDMatrixGenerator {

	/**
	 * |         |
	 * | c_i,j   | del
	 * |_________|______
	 * |         |
	 * |  ins    |	0
	 * |         |
	 */
	public double[][] getHEDContextMatrix(Graph sourceGraph, Graph targetGraph, CostFunctionManager cf, int radius) {
		int sSize = sourceGraph.size();
		int tSize = targetGraph.size();
		int dim = sSize + tSize;
		double[][] matrix = new double[dim][dim];
		
		// HED costs
		
		HEDContext hed = new HEDContext(sourceGraph, targetGraph, cf, radius);
		double[][] contextCosts = hed.generateContextCostMatrix();
		
		// sub
		
		for (int i = 0; i < sSize; i++) {
			System.arraycopy(contextCosts[i], 0, matrix[i], 0, tSize);
		}
		
		// ins

		for (int i = sSize; i < dim; i++) {
			System.arraycopy(contextCosts[sSize], 0, matrix[i], 0, tSize);
		}
		
		// del
		
		for (int i = 0; i < sSize; i++) {
			for (int j = tSize; j < dim; j++) {
				matrix[i][j] = contextCosts[i][tSize];
			}
		}
		
		// zero

		for (int i = sSize; i < dim; i++) {
			for (int j = tSize; j < dim; j++) {
				matrix[i][j] = 0.0;
			}
		}
		
		return matrix;
	}
	
}
