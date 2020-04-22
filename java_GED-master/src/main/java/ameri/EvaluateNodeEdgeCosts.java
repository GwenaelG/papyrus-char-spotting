package ameri;

import costs.CostFunctionManager;
import graph.Edge;
import graph.Graph;

import java.util.Vector;

/**
 * Created by mameri on 3/1/2016.
 * Last changed 3/1/2016 - 1:22 PM.
 */
public class EvaluateNodeEdgeCosts
{
    CostFunctionManager cf;
    Graph        g1;
    Graph        g2;

    int           g1EdgeNum;
    int           g2EdgeNum;
    int           g1NodeNum;
    int           g2NodeNum;

    public EvaluateNodeEdgeCosts(CostFunctionManager costFunction, Graph sourceGraph, Graph targetGraph)
    {
        this.cf = costFunction;
        this.g1 = sourceGraph;
        this.g2 = targetGraph;

        g1NodeNum = g1.size();
        g2NodeNum = g2.size();

        this.g1EdgeNum = 0;
        this.g2EdgeNum = 0;

    }

    public int getG1EdgeNum() {
        return g1EdgeNum;
    }

    public int getG2EdgeNum() {
        return g2EdgeNum;
    }

    public int getG1NodeNum() {
        return g1NodeNum;
    }

    public int getG2NodeNum() {
        return g2NodeNum;
    }


    public void estimateCosts()
    {
        cf.setEdgeCost(estimateEdgeCost());
        cf.setNodeCost(estimateNodeCost());

    }

    private double estimateEdgeCost()
    {
        Edge [][] g1Edges  = g1.getAdjacenyMatrix();
        Edge [][] g2Edges  = g2.getAdjacenyMatrix();

        Vector<Edge> g1EdgeList = new Vector<>(g1.size());
        Vector<Edge> g2EdgeList = new Vector<>(g2.size());

        for (int i = 0; i < g1Edges.length; i++)
        {
            for (int j = 0; j < g1Edges[i].length; j++)
            {
                if (g1Edges[i][j] != null)
                {
                    g1EdgeList.add(g1Edges[i][j]);
                }
            }
        }

        for (int i = 0; i < g2Edges.length; i++)
        {
            for (int j = 0; j < g2Edges[i].length; j++)
            {
                if( g2Edges[i][j] != null)
                {
                    g2EdgeList.add(g2Edges[i][j]);
                }
            }
        }

        double[] min1 = new double[g1EdgeList.size()];
        double[] min2 = new double[g2EdgeList.size()];

        this.g1EdgeNum = min1.length;
        this.g2EdgeNum = min2.length;

        for (int i=0; i < min1.length; i++) {
            min1[i] = Double.POSITIVE_INFINITY;
        }

        for (int j=0; j < min2.length; j++) {
            min2[j] = Double.POSITIVE_INFINITY;
        }

        for (int i = 0; i <g1EdgeList.size() ; i++)
        {
            for (int j = 0; j < g2EdgeList.size(); j++)
            {
                double subE = cf.getCost(g1EdgeList.get(i), g2EdgeList.get(j));
                double sub  = subE / 2;
                min1[i] = Math.min(min1[i], sub);
                min2[j] = Math.min(min2[j], sub);
            }

        }

        double subE = 0;

        for (int i = 0; i < min1.length; i++)
        {
            if( min1[i] != Double.POSITIVE_INFINITY)
                subE += min1[i];

        }

        for (int i = 0; i < min2.length; i++)
        {
            if ( min2[i] != Double.POSITIVE_INFINITY)
                subE += min2[i];
        }


        double edgeCost = subE /( min1.length + min2.length);

        if (edgeCost ==  0)
            edgeCost = 1;

//        System.out.println("Edge cost "+ edgeCost);

        edgeCost *= 2;

        return edgeCost;
    }
    private double estimateNodeCost()
    {
        double[] min1 = new double[g1.size()];
        for (int i=0; i < g1.size(); i++) {
            min1[i] = Double.POSITIVE_INFINITY;
        }

        double[] min2 = new double[g2.size()];
        for (int j=0; j < g2.size(); j++) {
            min2[j] = Double.POSITIVE_INFINITY;
        }

        for (int i = 0; i < g1.size(); i++) {
            for (int j = 0; j <g2.size() ; j++) {
                // TODO mind to change  with "getCost(Graph g1, Graph g2, int g1_node_id, int g2_node_id)"
                double subN = cf.getCost(g1.get(i), g2.get(j));
                double sub = (subN) / 2;
                min1[i] = Math.min(min1[i], sub);
                min2[j] = Math.min(min2[j], sub);
            }
        }

        double sumN = 0;
        for (int i=0; i < g1.size(); i++) {
            if ( min1[i] != Double.POSITIVE_INFINITY)
                sumN += min1[i];
        }
        for (int j=0; j < g2.size(); j++) {
            if( min2[j] != Double.POSITIVE_INFINITY)
                sumN += min2[j];
        }

        double nodeCost = sumN /(g1.size() + g2.size());

        if (nodeCost == 0)
            nodeCost = 1;

//        System.out.println("Node cost "+ nodeCost);
        nodeCost *= 2;

        return  nodeCost;
    }

}
