package costs.normalisation;

/**
 * User: Michael Stauffer
 * Org.: University of Applied Sciences and Arts Northwestern Switzerland
 * Date: 24.05.17
 * Time: 13:39
 */
public class GEDNormalisation2 implements NormalisationFunction {

    double nodeCost;
    double edgeCost;

    public GEDNormalisation2(double nodeCost,double edgeCost){
        this.nodeCost = nodeCost;
        this.edgeCost = edgeCost;
    }

    @Override
    public double normaliseGED(double GED, int n1, int n2, int e1, int e2) {
        return GED / ((n1 * nodeCost)  + (e1 * edgeCost));
    }
}