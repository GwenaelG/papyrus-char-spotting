package costs.functions;

import graph.Edge;
import graph.Node;

/**
 * User: Michael Stauffer
 * Org.: University of Applied Sciences and Arts Northwestern Switzerland
 * Date: 24.05.17
 * Time: 10:52
 */
public class EuclideanDistanceScaling implements CostFunction {

    @Override
    public double getCost(Node u, Node v, String[] nodeAttributes, double[] nodeAttrImportance) {

        double cost = 0;

        for (int i = 0; i < nodeAttributes.length; i++) {

            String nodeAttribute = nodeAttributes[i];
            String nodeAttributeStd = nodeAttribute + "_std";

            double n1 = u.getDouble(nodeAttribute);
            double n2 = v.getDouble(nodeAttribute);

            double stdDeviation = u.getGraph().getDouble(nodeAttributeStd);

            cost += Math.pow((n1 - n2) * stdDeviation, 2) * nodeAttrImportance[i];
        }

        return Math.sqrt(cost);
    }

    @Override
    public double getCost(Edge u, Edge v, String[] edgeAttributes, double[] edgeAttrImportance) {

        double cost = 0;

        for (int i = 0; i < edgeAttributes.length; i++) {

            String edgeAttribute        = edgeAttributes[i];
            String edgeAttributeStd     = edgeAttribute+"_std";

            double n1 = u.getDouble(edgeAttribute);
            double n2 = v.getDouble(edgeAttribute);

            double stdDeviation = u.getStartNode().getGraph().getDouble(edgeAttributeStd);

            cost += Math.pow((n1 - n2) * stdDeviation, 2) * edgeAttrImportance[i];
        }

        return Math.sqrt(cost);
    }

    @Override
    public CostFunction getBaseCostFunction() {
        return null;
    }

    @Override
    public String getName() {
        return "Euclidean Distance Scaling";
    }

    @Override
    public String getParameter() {
        return "";
    }
}
