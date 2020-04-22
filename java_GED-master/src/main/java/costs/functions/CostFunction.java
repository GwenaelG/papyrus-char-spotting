package costs.functions;

import graph.Edge;
import graph.Node;

/**
 * User: Michael Stauffer
 * Org.: University of Applied Sciences and Arts Northwestern Switzerland
 * Date: 24.05.17
 * Time: 10:49
 */
public interface CostFunction {

    double getCost(Node u, Node v, String[] nodeAttributes, double[] nodeAttrImportance);

    double getCost(Edge u, Edge v, String[] edgeAttributes, double[] edgeAttrImportance);

    CostFunction getBaseCostFunction();

    String getName();

    String getParameter();
}
