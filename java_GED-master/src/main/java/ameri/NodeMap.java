package ameri;

import costs.CostFunctionManager;
import graph.Graph;
import graph.Node;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by ameri on 2016-02-18.
 */

public class NodeMap
{
    enum NodeOperation
    {
        INS, DEL, SUB, EMP
    }


    NodeOperation operation;
    double        nodeCost;
    Node          from;
    int           fromID;
    Node          to;
    int           toID;
    Vector<EdgeMap> edgeMapList;
    Graph graphFrom;
    Graph graphTo;

    //    double        tempOperatoinCost;

    public NodeMap()
    {
        operation = NodeOperation.EMP; nodeCost = 0; from = null; to = null;
        graphFrom = null; graphTo = null;
        fromID = 0; toID = 0;
    }
    public NodeMap(NodeOperation op, double c, Node from, Node to, int fromID, int toID,
                   Graph graphFrom, Graph graphTo, Vector<EdgeMap> edgeMapList)
    {
        this.operation = op; this.nodeCost = c; this.from = from; this.to= to;
        this.edgeMapList = edgeMapList;
        this.graphFrom = graphFrom; this.graphTo = graphTo;
        this.fromID = fromID; this.toID  = toID;
    }

    public Graph getGraphFrom() {
        return graphFrom;
    }

    public Graph getGraphTo() {
        return graphTo;
    }

    public int getFromID() {
        return fromID;
    }

    public int getToID() {
        return toID;
    }

    //    public void setTempOperatoinCost(double tempOperatoinCost) {
//        this.tempOperatoinCost = tempOperatoinCost;
//    }
//
//    public double getTempOperatoinCost() {
//        return tempOperatoinCost;
//    }

    public double getCostVariable() {
        return  nodeCost;
    }

    public double getCostEdgesIncluded() {
        double cost = nodeCost;
        cost += EdgeMap.getCostOfVector(edgeMapList) /2 ; // since half of the cost is for this node
        return  cost;
    }

    public Node getFrom() {
        return from;
    }

    public Node getTo() {
        return to;
    }

    public NodeOperation getOperation() {
        return operation;
    }

    public Vector<EdgeMap> getEdgeMapList() {
        return edgeMapList;
    }

//    public static double getCostVector(Vector<NodeMap> nodeMapVector)
//    {
//        double cost = 0;
//        if (nodeMapVector != null)
//            for (NodeMap nodeMap: nodeMapVector)
//            {
//                  cost += nodeMap.getCostEdgesIncluded();
////                cost += nodeMap.getTempOperatoinCost();
//            }
//        return cost;
//    }

    public static double getCostVectorVariance(Vector<NodeMap> nodeMapVector, CostFunctionManager cf)
    {

        int insDelNum = 0;
        int subNum = 0;
        double varianceCost = Double.POSITIVE_INFINITY;
        double sumCost = 0;
        ArrayList<Double> costList = new ArrayList<>(nodeMapVector.size());


        if (nodeMapVector.size() > 0)
            for (NodeMap nodeMap: nodeMapVector)
            {
                if(nodeMap.getOperation() == NodeOperation.SUB)
                {
                    double currCost = nodeMap.getCostEdgesIncluded();
                    costList.add(currCost);
                    sumCost += currCost;
                    subNum ++;
                }
                if(nodeMap.getOperation() == NodeOperation.DEL || nodeMap.getOperation() == NodeOperation.INS)
                {
                    insDelNum ++;
                }
            }

        if (insDelNum < subNum)
        {
            double mean = sumCost / nodeMapVector.size();
            double sumX2 = 0;

            for (Double costItem:costList)
            {
                sumX2 += (costItem-mean)*(costItem-mean);
            }
            varianceCost = Math.sqrt(sumX2);
        }

        return varianceCost;

    }

    public static double getCostVectorMeanVariance(Vector<NodeMap> nodeMapVector, CostFunctionManager cf)
    {

        //int insDelNum = 0;
        //int subNum = 0;
        int numOps = 0;
        double MeanVarianceCost = Double.POSITIVE_INFINITY;
        double sumCost = 0;
        ArrayList<Double> costList = new ArrayList<>(nodeMapVector.size());


        numOps = nodeMapVector.size();
        if (numOps > 0) {
            for (NodeMap nodeMap : nodeMapVector) {
                    double currCost = nodeMap.getCostEdgesIncluded();
                    costList.add(currCost);
                    sumCost += currCost;
            }
            double mean = sumCost / numOps;

            double sumX2 = 0;
            for (Double costItem:costList)
            {
                sumX2 += (costItem-mean)*(costItem-mean);
            }
            double Variance = Math.sqrt(sumX2/numOps);
            MeanVarianceCost = mean* Variance;
        }
        return MeanVarianceCost;

    }

    public static double getCostVectorPlain(Vector<NodeMap> nodeMapVector, CostFunctionManager cf)
    {
        double cost = 0;
        int  subNum = 0;
        double subCost = 0;
        int  insDelNum = 0;
        double insDelCost = 0;
        if (nodeMapVector != null)
            for (NodeMap nodeMap: nodeMapVector)
            {
                if(nodeMap.getOperation() == NodeOperation.SUB)
                {
                    subNum ++;
                    subCost += nodeMap.getCostEdgesIncluded();
                }
                if(nodeMap.getOperation() == NodeOperation.DEL || nodeMap.getOperation() == NodeOperation.INS)
                {
                    insDelNum ++;
                    insDelCost += nodeMap.getCostEdgesIncluded();
                }
            }

        if(subNum > 0)
        {
            cost += subCost;
        }
        if(insDelNum >0)
        {

            cost += insDelCost ;
        }

        if (insDelNum >  subNum)
        {
            return Double.POSITIVE_INFINITY;
        }

        // cost /= cf.getNodeCosts();
        // cost = subCost / subNum + insDelCost /(insDelNum * cf.getNodeCosts());

        return cost;
    }

    public static double getCostVectorSubRatio(Vector<NodeMap> nodeMapVector, CostFunctionManager cf)
    {
        double cost = 0;
        int  subNum = 0;
        double subCost = 0;
        int  insDelNum = 0;
        double insDelCost = 0;
        if (nodeMapVector != null)
            for (NodeMap nodeMap: nodeMapVector)
            {
                if(nodeMap.getOperation() == NodeOperation.SUB)
                {

                    double edgeSub = 0;
                    double edgeInsDel = 0;
                    for (EdgeMap edgeMap:
                         nodeMap.edgeMapList) {
                        if (edgeMap.getOperation() == EdgeMap.EdgeOperation.SUB)
                            edgeSub ++;
                        else
                            edgeInsDel ++;
                    }

                    subNum += (edgeSub / (edgeSub +  edgeInsDel));
                }
                if(nodeMap.getOperation() == NodeOperation.DEL ||
                        nodeMap.getOperation() == NodeOperation.INS)
                {
                    insDelNum ++;
                    // as all edges are removed or inserted
                    insDelNum += nodeMap.edgeMapList.size();
                }
            }

        cost = (subNum) /  (double) (subNum + insDelNum);

        if (insDelNum >  subNum)
        {
            return 0 ;
        }

        return cost;
    }

}


