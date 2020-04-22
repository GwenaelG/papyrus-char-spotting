package ameri;

import costs.CostFunctionManager;
import graph.Edge;
import graph.Graph;

import java.util.LinkedList;

/**
 * @author Andreas Fischer
 */
public class HEDAndreas {

	public double getHausdorffEditDistance(Graph g1, Graph g2, CostFunctionManager cf) {

		double[] min1 = new double[g1.size()];
		for (int i=0; i < g1.size(); i++) {
			min1[i] = cf.getNodeCosts() + 0.5 * g1.get(i).getEdges().size() * cf.getEdgeCosts();
		}
		
		double[] min2 = new double[g2.size()];
		for (int j=0; j < g2.size(); j++) {
			min2[j] = cf.getNodeCosts() + 0.5 * g2.get(j).getEdges().size() * cf.getEdgeCosts();
		}
		
		for (int i=0; i < g1.size(); i++) {
			LinkedList<Edge> edges1out = g1.getOutgoingEdges(i);
			for (int j=0; j < g2.size(); j++) {
				LinkedList<Edge> edges2out = g2.getOutgoingEdges(j);

				// TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
				double subN = cf.getCost(g1.get(i), g2.get(j));
				double subE = 0;
				
				double minEout = Math.abs(edges1out.size() - edges2out.size()) * cf.getEdgeCosts();
				double subEout = hdEdges(edges1out, edges2out, cf);
				subE += Math.max(minEout, subEout);
				
				if (g1.isDirected()) {
					LinkedList<Edge> edges1in = g1.getIncomingEdges(i);
					LinkedList<Edge> edges2in = g2.getIncomingEdges(j);
					double minEin = Math.abs(edges1in.size() - edges2in.size()) * cf.getEdgeCosts();
					double subEin = hdEdges(edges1in, edges2in, cf);
					subE += Math.max(minEin, subEin);
				}

				double sub = (subN + (subE / 2)) / 2;
				min1[i] = Math.min(min1[i], sub);
				min2[j] = Math.min(min2[j], sub);
			}
		}
		
		double minNhd = Math.abs(g1.size() - g2.size()) * cf.getNodeCosts();
		double sumNhd = 0;
		for (int i=0; i < g1.size(); i++) {
			sumNhd += min1[i];
		}
		for (int j=0; j < g2.size(); j++) {
			sumNhd += min2[j];
		}
		double hd = Math.max(minNhd, sumNhd);
		
		return hd;
	}
	
	private double hdEdges(LinkedList<Edge> edges1, LinkedList<Edge> edges2, CostFunctionManager cf) {
		
		double[] min1 = new double[edges1.size()];
		for (int i=0; i < edges1.size(); i++) {
			min1[i] = cf.getEdgeCosts();
		}
		
		double[] min2 = new double[edges2.size()];
		for (int j=0; j < edges2.size(); j++) {
			min2[j] = cf.getEdgeCosts();
		}
		
		for (int i=0; i < edges1.size(); i++) {
			for (int j=0; j < edges2.size(); j++) {
				double sub = cf.getCost(edges1.get(i), edges2.get(j)) / 2;
				min1[i] = Math.min(min1[i], sub);
				min2[j] = Math.min(min2[j], sub);
			}
		}
		
		double hd = 0;
		for (int i=0; i < edges1.size(); i++) {
			hd += min1[i];
		}
		for (int j=0; j < edges2.size(); j++) {
			hd += min2[j];
		}
		return hd;
	}
	
}
