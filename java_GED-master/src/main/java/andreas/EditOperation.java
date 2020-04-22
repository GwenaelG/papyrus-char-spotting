package andreas;

import java.text.DecimalFormat;

import graph.Node;

public class EditOperation {

	public static final int SUB = 0;
	public static final int DEL = 1;
	public static final int INS = 2;
	public static final int SAB = 4;
	public static final int SBA = 5;
	
	private int type;
	public Node n1;
	public Node n2;
	public double cost;
	public double nodeCost;
	public double edgeCost;
	
	public EditOperation(int type, Node n1, Node n2, double nodeCost, double edgeCost) {
		this.type = type;
		this.n1 = n1;
		this.n2 = n2;
		this.cost = nodeCost + edgeCost;
		this.nodeCost = nodeCost;
		this.edgeCost = edgeCost;
	}
	
	public void halfCosts() {
		this.cost /= 2;
		this.nodeCost /= 2;
		this.edgeCost /= 2;
	}
	
	public boolean isSubstitution() {
		return isSub() || isSAB() || isSBA();
	}
	
	public boolean isSub() {
		return type == EditOperation.SUB;
	}
	
	public boolean isDel() {
		return type == EditOperation.DEL;
	}
	
	public boolean isIns() {
		return type == EditOperation.INS;
	}
	
	public boolean isSAB() {
		return type == EditOperation.SAB;
	}
	
	public boolean isSBA() {
		return type == EditOperation.SBA;
	}
	
	public String toString() {
		DecimalFormat df = new DecimalFormat("#.###");
		String str = "";
		if (isSubstitution()) {
			str += n1.getNodeID() + " => " + n2.getNodeID();
		} else if (isDel()) {
			str += n1.getNodeID() + " => epsilon";
		} else if (isIns()) {
			str += "epsilon => " + n2.getNodeID();
		}
		str += ": " + df.format(cost) + " (n:" + df.format(nodeCost) + "/e:" + df.format(edgeCost) + ")\n";
		return str;
	}
	
}
