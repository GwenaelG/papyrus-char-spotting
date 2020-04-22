package ameri;

import graph.Edge;

import java.util.Vector;

/**
 * Created by ameri on 2016-02-18.
 */

public class EdgeMap
{
    enum EdgeOperation
    {
        INS, DEL, SUB, EMP
    }

    EdgeOperation operation;
    double        cost;
    Edge from;
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
