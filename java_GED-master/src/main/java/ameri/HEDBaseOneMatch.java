package ameri;

import costs.CostFunctionManager;
import graph.Edge;
import graph.Graph;

import java.util.*;

/**
 * Created by ameri on 2016-02-22.
 * Last changed 2016-02-22 - 5:32 PM.
 */
public class HEDBaseOneMatch {

    public enum SCORE_TYPE{
        PLAIN,
        RATIO_SUB_ALL,
        VARIANCE,
        MEAN,
        MEAN_VARIANCE
    }
    SCORE_TYPE scoreType;

    static class NodeMapCompare implements Comparator<NodeMap> {
        public int compare(NodeMap one , NodeMap two)
        {
            return  Double.compare(one.getCostEdgesIncluded() , two.getCostEdgesIncluded());
        }

    }

    public HEDBaseOneMatch()
    {
        scoreType = SCORE_TYPE.PLAIN;
    }

    public HEDBaseOneMatch(SCORE_TYPE scoreTypeIn)
    {
        scoreType = scoreTypeIn;
    }

    private Vector<NodeMap> initNodeMap(Graph g,
                                        CostFunctionManager cf,
                                        NodeMap.NodeOperation nodeOp,
                                        EdgeMap.EdgeOperation edgeOp)
    {
        Vector<NodeMap> gNodeMapVec = new Vector<>(g.size());
        for (int i = 0; i < g.size(); i++) {

            LinkedList<Edge> uiEdgeList = g.get(i).getEdges();
            Vector<EdgeMap> uiEdgeMap = new Vector<>(uiEdgeList.size());

            for (int j = 0; j < uiEdgeList.size(); j++) {
                uiEdgeMap.add(j, new EdgeMap(edgeOp, cf.getEdgeCosts() / 2, uiEdgeList.get(j), null));
            }

            NodeMap uiNodeMap = new NodeMap(nodeOp, cf.getNodeCosts(), g.get(i), null, i, 0, g, null, uiEdgeMap);
            gNodeMapVec.add(i, uiNodeMap);
        }
        return gNodeMapVec;
    }

    private Vector<NodeMap> getNodeMapLowerBound(Graph source,
                                                 Graph target,
                                                 CostFunctionManager cf,
                                                 NodeMap.NodeOperation nop,
                                                 EdgeMap.EdgeOperation eop)
    {
        Vector<NodeMap> gNodeMapVecLowerBound = new Vector<>(source.size());

        for (int i = 0; i < source.size(); i++)
        {
            if (i >= target.size())
            {
                gNodeMapVecLowerBound.add(i, new NodeMap(nop, cf.getNodeCosts() / 2,
                        source.get(i), null, i, 0, source, null, null));
            }
            else
            {
                gNodeMapVecLowerBound.add(i, new NodeMap(
                        NodeMap.NodeOperation.SUB, 0, source.get(i), target.get(i), i, i, source, target, null));
            }
        }

        return gNodeMapVecLowerBound;
    }

    public double getHausdorffEditDistance(Graph g1, Graph g2, CostFunctionManager cf) {

        Vector<NodeMap> g1NodeMapVecLowerBound =
                getNodeMapLowerBound(g1, g2, cf, NodeMap.NodeOperation.DEL, EdgeMap.EdgeOperation.DEL);
        Vector<NodeMap> g2NodeMapVecLowerBound =
                getNodeMapLowerBound(g2, g1, cf, NodeMap.NodeOperation.INS, EdgeMap.EdgeOperation.INS);

        Vector<NodeMap> g1NodeMapVec = initNodeMap(g1, cf, NodeMap.NodeOperation.DEL, EdgeMap.EdgeOperation.DEL);
        Vector<NodeMap> g2NodeMapVec = initNodeMap(g2, cf, NodeMap.NodeOperation.INS, EdgeMap.EdgeOperation.INS);

        Boolean g1NodeMatched [] = new Boolean[g1.size()];
        Boolean g2NodeMatched [] = new Boolean[g2.size()];
        for (int i = 0; i <g1NodeMatched.length ; i++) {
            g1NodeMatched[i] = false;
        }
        for (int i = 0; i <g2NodeMatched.length ; i++) {
            g2NodeMatched[i] = false;
        }


        PriorityQueue<NodeMap> nodeMapPriorityQueue =
                new PriorityQueue<NodeMap>(g1.size()+g2.size(), new NodeMapCompare()); // the min size needed.

        for (int i = 0; i < g1.size(); i++) {
            LinkedList<Edge> edges1out = g1.getOutgoingEdges(i);

            for (int j = 0; j < g2.size(); j++) {
                LinkedList<Edge> edges2out = g2.getOutgoingEdges(j);

                Vector<EdgeMap> uiEdgeMapOut =
                        hedEdgesSourceToTarget(edges1out, edges2out, cf, EdgeMap.EdgeOperation.DEL);
                Vector<EdgeMap> ujEdgeMapOut =
                        hedEdgesSourceToTarget(edges2out, edges1out, cf, EdgeMap.EdgeOperation.INS);

                NodeMap uiNodeMap = new NodeMap(
                        NodeMap.NodeOperation.SUB,
                        // TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
                        cf.getCost(g1.get(i), g2.get(j)) / 2,
                        g1.get(i),
                        g2.get(j),
                        i,
                        j,
                        g1,
                        g2,
                        uiEdgeMapOut
                );

                NodeMap ujNodeMap = new NodeMap(
                        NodeMap.NodeOperation.SUB,
                        // TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
                        cf.getCost(g1.get(i), g2.get(j)) / 2,
                        g2.get(j),
                        g1.get(i),
                        j,
                        i,
                        g2,
                        g1,
                        ujEdgeMapOut
                );

                if (g1.isDirected())
                {
                    LinkedList<Edge> edges1in = g1.getIncomingEdges(i);
                    LinkedList<Edge> edges2in = g2.getIncomingEdges(j);
                    Vector<EdgeMap> uiEdgeMapIn = hedEdgesSourceToTarget(edges1in, edges2in, cf, EdgeMap.EdgeOperation.DEL);
                    Vector<EdgeMap> ujEdgeMapIn = hedEdgesSourceToTarget(edges2in, edges1in, cf, EdgeMap.EdgeOperation.INS);
                    uiNodeMap.getEdgeMapList().addAll(uiEdgeMapIn);
                    ujNodeMap.getEdgeMapList().addAll(ujEdgeMapIn);
                }


                // Adding the NodeMaps to the PQ if bigger than ins/del
                if (g1NodeMapVec.get(i).getCostEdgesIncluded() > uiNodeMap.getCostEdgesIncluded())
                {
                    nodeMapPriorityQueue.offer(uiNodeMap);
                }

                if (g2NodeMapVec.get(j).getCostEdgesIncluded() > ujNodeMap.getCostEdgesIncluded())
                {
                    nodeMapPriorityQueue.offer(ujNodeMap);
                }
            }
        }

        while(! nodeMapPriorityQueue.isEmpty())
        {
            NodeMap nodeMap = nodeMapPriorityQueue.poll();

            if(nodeMap.getGraphFrom() == g1 )
            {
                if (nodeMap.getCostEdgesIncluded() < g1NodeMapVec.get(nodeMap.getFromID()).getCostEdgesIncluded()
                        && ! g2NodeMatched[nodeMap.getToID()])
                {
                    g1NodeMapVec.set(nodeMap.getFromID(), nodeMap);
                    g2NodeMatched[nodeMap.getToID()] = true;
                }

            }
            else if (nodeMap.getGraphFrom() == g2)
            {
                if(nodeMap.getCostEdgesIncluded() < g2NodeMapVec.get(nodeMap.getFromID()).getCostEdgesIncluded()
                        && ! g1NodeMatched[nodeMap.getToID()])
                {
                    g2NodeMapVec.set(nodeMap.getFromID(), nodeMap);
                    g1NodeMatched[nodeMap.getToID()] = true;
                }
            }
        }


//        double minNhd = NodeMap.getCostVectorPlain(g1NodeMapVecLowerBound, cf) +
//                NodeMap.getCostVectorPlain(g2NodeMapVecLowerBound, cf);
        double sumNhd = Double.POSITIVE_INFINITY;
        switch (scoreType)
        {
            case PLAIN:
                sumNhd = NodeMap.getCostVectorPlain(g1NodeMapVec, cf) +
                        NodeMap.getCostVectorPlain(g2NodeMapVec, cf);

                break;
            case RATIO_SUB_ALL:
                double g1SubRatio = NodeMap.getCostVectorSubRatio(g1NodeMapVec, cf);
                double g2SubRatio = NodeMap.getCostVectorSubRatio(g2NodeMapVec, cf);
                if (g1SubRatio == 0 || g2SubRatio == 0) {
                    sumNhd = 2;
                }
                else {
                    sumNhd = 2 - (NodeMap.getCostVectorSubRatio(g1NodeMapVec, cf) +
                            NodeMap.getCostVectorSubRatio(g2NodeMapVec, cf));
                }
                break;

            case VARIANCE:
                sumNhd = NodeMap.getCostVectorVariance(g1NodeMapVec, cf) +
                        NodeMap.getCostVectorVariance(g2NodeMapVec, cf);
                break;
            case MEAN_VARIANCE:
                sumNhd  = NodeMap.getCostVectorMeanVariance(g1NodeMapVec, cf) +
                        NodeMap.getCostVectorMeanVariance(g2NodeMapVec, cf);

            case MEAN:

        }

//        if (minNhd > sumNhd)
//            return minNhd;

        return sumNhd;
    }

    private Vector<EdgeMap> initEdgeMap(LinkedList<Edge> edges,
                                        CostFunctionManager cf,
                                        EdgeMap.EdgeOperation op)
    {
        Vector<EdgeMap> edgeMap = new Vector<>(edges.size());
        for (int i = 0; i < edges.size(); i++) {
            edgeMap.add(i, new EdgeMap(op, cf.getEdgeCosts(), edges.get(i), null));
        }
        return edgeMap;
    }


    private Vector<EdgeMap> hedEdgesSourceToTarget(LinkedList<Edge> edges1,
                                                   LinkedList<Edge> edges2,
                                                   CostFunctionManager cf,
                                                   EdgeMap.EdgeOperation op) {

        /// lower bound edge map. At least |M1-M2| INS or DEL and min(M1, M2) SUB
        /// Then there will be matching appropriate to the number of matching
        Vector<EdgeMap> edgeMapLowerBound = initEdgeMap(edges1, cf, op);
        if (edges1.size() > edges2.size())
        {
            for (int i = 0; i < edges2.size(); i++)
            {
                edgeMapLowerBound.get(i).set(EdgeMap.EdgeOperation.SUB, 0, edges1.get(i), edges2.get(i));
            }
        }
        else
        {
            for(int i = 0; i < edges1.size(); i++)
            {
                edgeMapLowerBound.get(i).set(EdgeMap.EdgeOperation.SUB, 0, edges1.get(i), edges2.get(i));
            }
        }

        Vector<EdgeMap> edge1Map = initEdgeMap(edges1, cf, op);
        for (int i = 0; i < edges1.size(); i++)
        {
            for (int j = 0; j < edges2.size(); j++)
            {
                double sub = cf.getCost(edges1.get(i), edges2.get(j));
                EdgeMap edgeMapij = new EdgeMap(EdgeMap.EdgeOperation.SUB, sub, edges1.get(i), edges2.get(j));
                if (edgeMapij.getCost() < edge1Map.get(i).getCost())
                    edge1Map.set(i, edgeMapij);
            }
        }

        if (EdgeMap.getCostOfVector(edge1Map) < EdgeMap.getCostOfVector(edgeMapLowerBound))
        {
            return edgeMapLowerBound;
        }
        return edge1Map;
    }

}
