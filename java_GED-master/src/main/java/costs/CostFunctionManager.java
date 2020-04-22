package costs;

import costs.functions.CostFunction;
import graph.Edge;
import graph.Node;

/**
 * User: Michael Stauffer
 * Org.: University of Applied Sciences and Arts Northwestern Switzerland
 * Date: 24.05.17
 * Time: 10:47
 */
public class CostFunctionManager {

    private CostFunction costFunction;

    /**
     * the weighting factor alpha measures the relative importance of node and
     * edge operations
     */
    private double alpha;

    /**
     * the constant cost for node and edge deletions/insertions
     */
    private double nodeCost;
    private double edgeCost;

    /**
     * all identifiers (names) of node and edge attributes
     */
    private String[] nodeAttributes;
    private String[] edgeAttributes;

    /**
     * the weighting factors for the node and edge attributes
     */
    private double[] nodeAttrImportance;
    private double[] edgeAttrImportance;

    /**
     * initializes the costfunction according to the properties read in the properties file
     */
    public CostFunctionManager(CostFunction costFunction, double alpha, double nodeCost, double edgeCost, String[] nodeAttributes, String[] edgeAttributes, double[] nodeAttrImportance, double[] edgeAttrImportance) {

        this.costFunction	= costFunction;

        this.alpha 			= alpha;

        this.nodeCost		= nodeCost;
        this.edgeCost 		= edgeCost;

        this.nodeAttributes = nodeAttributes;
        this.edgeAttributes = edgeAttributes;

        this.nodeAttrImportance = nodeAttrImportance;
        this.edgeAttrImportance = edgeAttrImportance;
    }

    /**
     * @return the substitution cost between node @param u
     * and node @param v according to their attribute values and the cost functions.
     * The individual costs are softened by the importance factors and finally
     * added or multiplied (and possibly the result is "square rooted")
     * The final result is multiplied by alpha
     */
    public double getCost(Node u, Node v) {

        double cost = this.costFunction.getCost(u,v,this.nodeAttributes,this.nodeAttrImportance);
        cost *= this.alpha;
        return cost;
    }

    /**
     * @return the substitution cost between edge @param u
     * and edge @param v according to their attribute values and the cost functions.
     * The individual costs are softened by the importance factors and finally
     * added or multiplied (and possibly the result is "square rooted")
     * The final result is multiplied by (1-alpha)
     */
    public double getCost(Edge u, Edge v) {

        double cost = this.costFunction.getCost(u,v,this.edgeAttributes,this.edgeAttrImportance);
        cost *= (1 - this.alpha);
        return cost;
    }


//	/**
//	 * @return the string edit distance between strings
//	 * @param s1 and @param s2
//	 *
//	 */
//	private double stringEditDistance(String s1, String s2) {
//		int n = s1.length();
//		int m = s2.length();
//		double[][] stringMatrix = new double[n + 1][m + 1];
//		stringMatrix[0][0] = 0;
//		for (int i = 1; i <= n; i++) {
//			stringMatrix[i][0] = stringMatrix[i - 1][0] + 1.;
//		}
//		for (int j = 1; j <= m; j++) {
//			stringMatrix[0][j] = stringMatrix[0][j - 1] + 1.;
//		}
//
//		for (int i = 1; i <= n; i++) {
//			for (int j = 1; j <= m; j++) {
//				double subst = 0.;
//				if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
//					subst = 0.;
//				} else {
//					subst = 2;
//				}
//				double m1 = stringMatrix[i - 1][j - 1] + subst;
//				double m2 = stringMatrix[i - 1][j] + 1;
//				double m3 = stringMatrix[i][j - 1] + 1;
//				stringMatrix[i][j] = Math.min(m1, Math.min(m2, m3));
//			}
//		}
//		return stringMatrix[n][m];
//	}

    /**
     * @return the constant cost for node deletion/insertion
     * multiplied by alpha
     */
    public double getNodeCosts() {
        return this.alpha * this.nodeCost;
    }

    /**
     * @return the constant cost for edge deletion/insertion
     * multiplied by (1-alpha)
     */
    public double getEdgeCosts() {
        return (1 - this.alpha) * this.edgeCost;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getNodeCost() {
        return nodeCost;
    }

    public void setNodeCost(double nodeCost) {
        this.nodeCost = nodeCost;
    }

    public double getEdgeCost() {
        return edgeCost;
    }

    public void setEdgeCost(double edgeCost) {
        this.edgeCost = edgeCost;
    }

    public CostFunction getCostFunction() {
        return costFunction;
    }

    public void setCostFunction(CostFunction costFunction) {
        this.costFunction = costFunction;
    }

    public String[] getNodeAttributes() {
        return nodeAttributes;
    }

    public void setNodeAttributes(String[] nodeAttributes) {
        this.nodeAttributes = nodeAttributes;
    }

    public String[] getEdgeAttributes() {
        return edgeAttributes;
    }

    public void setEdgeAttributes(String[] edgeAttributes) {
        this.edgeAttributes = edgeAttributes;
    }

    public double[] getNodeAttrImportance() {
        return nodeAttrImportance;
    }

    public void setNodeAttrImportance(double[] nodeAttrImportance) {
        this.nodeAttrImportance = nodeAttrImportance;
    }

    public void setNodeAttrImportance(String nodeAttribute, double nodeAttributeImportance){
        for(int i=0; i<this.nodeAttributes.length; i++){
            if(this.nodeAttributes[i].equals(nodeAttribute)){
                this.nodeAttrImportance[i] = nodeAttributeImportance;
                break;
            }
        }
    }

    public double[] getEdgeAttrImportance() {
        return edgeAttrImportance;
    }

    public void setEdgeAttrImportance(double[] edgeAttrImportance) {
        this.edgeAttrImportance = edgeAttrImportance;
    }

    public void setEdgeAttrImportance(String edgeAttribute, double edgeAttributeImportance){
        for(int i=0; i<this.edgeAttributes.length; i++){
            if(this.edgeAttributes[i].equals(edgeAttribute)){
                this.edgeAttrImportance[i] = edgeAttributeImportance;
                break;
            }
        }
    }
}
