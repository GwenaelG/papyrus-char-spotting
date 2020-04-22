package ameri;

import costs.CostFunctionManager;
import graph.Edge;
import graph.Graph;
import graph.Node;

import java.util.LinkedList;
import java.util.Vector;

/**
 * Created by ameri on 2016-02-18.
 */

public class HEDBaseEqAndreas {

    enum NodeOperation
    {
        INS, DEL, SUB, EMP
    }

    enum EdgeOperation
    {
        INS, DEL, SUB, EMP
    }

    static class EdgeMap
    {
        EdgeOperation operation;
        double        cost;
        Edge          from;
        Edge          to;

        public EdgeMap()
        {
            cost = 0;
            operation = EdgeOperation.EMP;
            from = null;
            to = null;
        }

        public EdgeMap(EdgeOperation op, double c, Edge from, Edge to)
        {
            operation  = op;
            cost = c;
            this.from = from;
            this.to = to;
        }

        public EdgeMap(EdgeMap edgeMap)
        {
            operation  = edgeMap.getOperation();
            cost = edgeMap.getCost();
            from = edgeMap.getFrom();
            to = edgeMap.getTo();
        }


        public void  set(EdgeOperation op, double c, Edge from, Edge to)
        {
            operation = op; cost =  c; this.from = from; this.to = to;
        }
        public double getCost() {
            return cost;
        }

        public Edge getFrom() {
            return from;
        }

        public Edge getTo() {
            return to;
        }

        public EdgeOperation getOperation() {
            return operation;
        }

        public static double getCostOfVector(Vector<EdgeMap> edgeMapVec)
        {
            double cost = 0;
            if (edgeMapVec != null)
                for (EdgeMap edgeMap :edgeMapVec)
                {
                    cost += edgeMap.getCost();
                }
            return cost;
        }

    }

    static class NodeMap
    {
        NodeOperation operation;
        double        nodeCost;
        double        tempOperatoinCost;
        Node from;
        Node          to;
        Vector<EdgeMap> edgeMapList;
        public NodeMap()
        {
            operation = NodeOperation.EMP; nodeCost = 0; from = null; to = null;
        }
        public NodeMap(NodeOperation op, double c, Node from, Node to, Vector<EdgeMap> edgeMapList)
        {
            this.operation = op; this.nodeCost = c; this.from = from; this.to= to;
            this.edgeMapList = edgeMapList;
        }

        public void setTempOperatoinCost(double tempOperatoinCost) {
            this.tempOperatoinCost = tempOperatoinCost;
        }

        public double getTempOperatoinCost() {
            return tempOperatoinCost;
        }

        public double getCostVariable() {
            return  nodeCost;
        }

        public double getCostEdgesIncluded() {
            double cost = nodeCost;
            cost += EdgeMap.getCostOfVector(edgeMapList);
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

        public static double getCostVector(Vector<NodeMap> nodeMapVector)
        {
            double cost = 0;
            if (nodeMapVector != null)
                for (NodeMap nodeMap: nodeMapVector)
                {
//                    cost += nodeMap.getCostEdgesIncluded();
                    cost += nodeMap.getTempOperatoinCost();
                }
            return cost;
        }

    }

    private Vector<NodeMap> initNodeMap(Graph g, CostFunctionManager cf, NodeOperation nodeOp, EdgeOperation edgeOp )
    {
        Vector<NodeMap> gNodeMapVec = new Vector<>(g.size());
        for (int i = 0; i < g.size(); i++) {
            LinkedList<Edge> uiEdgeList = g.get(i).getEdges();

            Vector<EdgeMap> uiEdgeMap = new Vector<>(uiEdgeList.size());
            for (int j = 0; j < uiEdgeList.size(); j++) {
                uiEdgeMap.add(j, new EdgeMap(edgeOp, cf.getEdgeCosts()/2, uiEdgeList.get(j), null) );
            }
            NodeMap uiNodeMap = new NodeMap(nodeOp, cf.getNodeCosts(), g.get(i), null, uiEdgeMap);
            uiNodeMap.setTempOperatoinCost(uiNodeMap.getCostEdgesIncluded());
            gNodeMapVec.add(i, uiNodeMap );
        }
        return gNodeMapVec;
    }

    private Vector<NodeMap> getNodeMapLowerBound(Graph source,Graph target, CostFunctionManager cf, NodeOperation nop, EdgeOperation eop)
    {
        Vector<NodeMap> gNodeMapVecLowerBound = new Vector<>(source.size());

        for (int i = 0; i < source.size() ; i++)
        {
            if (i >= target.size())
            {
                gNodeMapVecLowerBound.add(i, new NodeMap(nop, cf.getNodeCosts()/2, source.get(i), null, null ));
                gNodeMapVecLowerBound.get(i).setTempOperatoinCost(gNodeMapVecLowerBound.get(i).getCostEdgesIncluded());
            }
            else
            {
                gNodeMapVecLowerBound.add(i, new NodeMap(NodeOperation.SUB, 0, source.get(i), target.get(i), null));
                gNodeMapVecLowerBound.get(i).setTempOperatoinCost(gNodeMapVecLowerBound.get(i).getCostEdgesIncluded());
            }
        }

        return gNodeMapVecLowerBound;
    }

    public double getHausdorffEditDistance(Graph g1, Graph g2, CostFunctionManager cf) {

        Vector<NodeMap> g1NodeMapVecLowerBound = getNodeMapLowerBound(g1,g2, cf, NodeOperation.DEL, EdgeOperation.DEL);
        Vector<NodeMap> g2NodeMapVecLowerBound = getNodeMapLowerBound(g2,g1, cf, NodeOperation.INS, EdgeOperation.INS);

        Vector<NodeMap> g1NodeMapVec = initNodeMap(g1, cf, NodeOperation.DEL, EdgeOperation.DEL);
        Vector<NodeMap> g2NodeMapVec = initNodeMap(g2, cf, NodeOperation.INS, EdgeOperation.INS);

        double[] min1 = new double[g1.size()];
        double sum1 = 0;
        for (int i=0; i < g1.size(); i++) {
            min1[i] = cf.getNodeCosts() + 0.5 * g1.get(i).getEdges().size() * cf.getEdgeCosts();
            sum1 += min1[i];
        }

        double sum2 = 0;
        double[] min2 = new double[g2.size()];
        for (int j=0; j < g2.size(); j++) {
            min2[j] = cf.getNodeCosts() + 0.5 * g2.get(j).getEdges().size() * cf.getEdgeCosts();
            sum2 += min2[j];
        }

        if(sum1 != NodeMap.getCostVector( g1NodeMapVec) )
        {
            System.out.println("sum1 " + sum1 + " nodeMap: " + NodeMap.getCostVector( g1NodeMapVec));
        }

        if(sum2 != NodeMap.getCostVector(g2NodeMapVec))
        {
            System.out.println("sum2 " + sum2 + " nodeMap: " + NodeMap.getCostVector( g2NodeMapVec));
        }


        for (int i = 0; i < g1.size(); i++) {
            LinkedList<Edge> edges1out = g1.getOutgoingEdges(i);

            for (int j = 0; j < g2.size(); j++) {
                LinkedList<Edge> edges2out = g2.getOutgoingEdges(j);

                //double minEout = Math.abs(edges1out.size() - edges2out.size()) * cf.getEdgeCosts();
                Vector<EdgeMap> uiEdgeMapOut =  hedEdgesSourceToTarget(edges1out, edges2out, cf, EdgeOperation.DEL);
                Vector<EdgeMap> ujEdgeMapOut =  hedEdgesSourceToTarget(edges2out, edges1out, cf, EdgeOperation.INS);


                double minEout = Math.abs(edges1out.size() - edges2out.size()) * cf.getEdgeCosts();
                double subEout = hdEdges(edges1out, edges2out, cf);
                double subE = Math.max(minEout, subEout);

                if(subE != EdgeMap.getCostOfVector( uiEdgeMapOut) + EdgeMap.getCostOfVector(ujEdgeMapOut) )
                {
                    System.out.println("edgeout: " + subE + " edgeMap: " +
                            (EdgeMap.getCostOfVector( uiEdgeMapOut) + EdgeMap.getCostOfVector(ujEdgeMapOut))
                    );
                }

                NodeMap uiNodeMap = new NodeMap(
                        NodeOperation.SUB,
                        // TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
                        cf.getCost(g1.get(i), g2.get(j)) /2,
                        g1.get(i),
                        g2.get(j),
                        uiEdgeMapOut
                );
                uiNodeMap.setTempOperatoinCost(uiNodeMap.getCostVariable()+ subE/4);
                NodeMap ujNodeMap = new NodeMap(
                        NodeOperation.SUB,
                        // TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
                        cf.getCost(g1.get(i), g2.get(j)) /2,
                        g2.get(j),
                        g1.get(i),
                        ujEdgeMapOut
                );
                ujNodeMap.setTempOperatoinCost(ujNodeMap.getCostVariable()+ subE/4);

                if (g1.isDirected()){
                    LinkedList<Edge> edges1in = g1.getIncomingEdges(i);
                    LinkedList<Edge> edges2in = g2.getIncomingEdges(j);
                    Vector<EdgeMap> uiEdgeMapIn =  hedEdgesSourceToTarget(edges1in, edges2in, cf, EdgeOperation.DEL);
                    Vector<EdgeMap> ujEdgeMapIn =  hedEdgesSourceToTarget(edges2in, edges1in, cf, EdgeOperation.INS);
                    uiNodeMap.getEdgeMapList().addAll(uiEdgeMapIn);
                    ujNodeMap.getEdgeMapList().addAll(ujEdgeMapIn);
                }

                double subN = cf.getCost(g1.get(i), g2.get(j));
                // TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
                double sub = (subN + (subE / 2)) / 2;
                min1[i] = Math.min(min1[i], sub);
                min2[j] = Math.min(min2[j], sub);
                if(uiNodeMap.getCostVariable() + ujNodeMap.getCostVariable() != subN)
                {
                    System.out.println("NodeCost: "+ (uiNodeMap.getCostVariable() + ujNodeMap.getCostVariable() )+
                            " subN:" + subN);
                }

                if (sub != uiNodeMap.getTempOperatoinCost())
                {
                    System.out.println(" sub: " + sub + "  uinodemapAll: "+ uiNodeMap.getCostEdgesIncluded() +
                            " subN: " +subN + " subE: " + subE + " nodeCost: " +uiNodeMap.getCostVariable() +
                            " EdgeMap: " + EdgeMap.getCostOfVector(uiNodeMap.edgeMapList));
                }
                if (sub != ujNodeMap.getTempOperatoinCost())
                {
                    System.out.println(" sub: " + sub + " ujnodemap "+ ujNodeMap.getCostEdgesIncluded());
                }

                if(g1NodeMapVec.get(i).getTempOperatoinCost() > uiNodeMap.getTempOperatoinCost())
                {
                    g1NodeMapVec.set(i, uiNodeMap);
                }
                if(g2NodeMapVec.get(j).getTempOperatoinCost() > ujNodeMap.getTempOperatoinCost())
                {
                    g2NodeMapVec.set(j, ujNodeMap);
                }
            }
        }

        double minNhd = NodeMap.getCostVector(g1NodeMapVecLowerBound) + NodeMap.getCostVector(g2NodeMapVecLowerBound);

        double sumNhd = NodeMap.getCostVector(g1NodeMapVec)+ NodeMap.getCostVector(g2NodeMapVec);

        double sumHED = 0;
        for (int i=0; i < g1.size(); i++) {
            sumHED += min1[i];
            if(min1[i] != g1NodeMapVec.get(i).getTempOperatoinCost())
            {
                System.out.println("i:" +i+ " min1[i]"+ min1[i] + "g1NodeMapVec.:" + g1NodeMapVec.get(i).getTempOperatoinCost());
            }
        }
        for (int j=0; j < g2.size(); j++) {
            sumHED += min2[j];
        }
        if (sumHED != sumNhd)
        {
            System.out.println("sumHED:"+ sumHED+ " sumHEDSCORE " + sumNhd);
        }

        if(minNhd > sumNhd)
            return minNhd;

        return sumNhd;
    }


    private Vector<EdgeMap> initEdgeMap(LinkedList<Edge> edges, CostFunctionManager cf, EdgeOperation op)
    {
        Vector<EdgeMap> edgeMap = new Vector<>(edges.size());
        for (int i = 0; i < edges.size(); i++) {
            edgeMap.add(i, new EdgeMap(op, cf.getEdgeCosts(), edges.get(i), null));
        }
        return edgeMap;
    }


    private Vector<EdgeMap> hedEdgesSourceToTarget(LinkedList<Edge> edges1, LinkedList<Edge> edges2, CostFunctionManager cf,
                                                   EdgeOperation op) {

        /// lower bound edge map. At least |M1-M2| INS or DEL and min(M1, M2) SUB
        /// Then there will be matching appropriate to the number of matching
        Vector<EdgeMap> edgeMapLowerBound = initEdgeMap(edges1, cf, op);
        if(edges1.size() > edges2.size())
        {
            for (int i = 0; i < edges2.size(); i++) {
                edgeMapLowerBound.get(i).set(EdgeOperation.SUB, 0,edges1.get(i), edges2.get(i));
            }
        }
        else
        {
            for (int i = 0; i <edges1.size() ; i++) {
                edgeMapLowerBound.get(i).set(EdgeOperation.SUB, 0, edges1.get(i), edges2.get(i));
            }
        }

        Vector<EdgeMap> edge1Map = initEdgeMap(edges1, cf, op);
        for (int i=0; i < edges1.size(); i++) {
            for (int j=0; j < edges2.size(); j++) {
                double sub = cf.getCost(edges1.get(i), edges2.get(j));
                EdgeMap edgeMapij = new EdgeMap(EdgeOperation.SUB, sub, edges1.get(i), edges2.get(j));
                if (edgeMapij.getCost() < edge1Map.get(i).getCost() )
                    edge1Map.set(i, edgeMapij);
            }
        }

        if(EdgeMap.getCostOfVector(edge1Map) < EdgeMap.getCostOfVector(edgeMapLowerBound) )
        {
            return edgeMapLowerBound;
        }
        return edge1Map;
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
