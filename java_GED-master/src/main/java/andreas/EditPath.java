package andreas;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedList;

import graph.Graph;
import graph.Node;

public class EditPath {

	private Graph g1;
	private Graph g2;
	private boolean isDirected;
	private LinkedList<EditOperation> editOperations;
	
	public EditPath(Graph g1, Graph g2, boolean isDirected) {
		this.g1 = g1;
		this.g2 = g2;
		this.isDirected = isDirected;
		editOperations = new LinkedList<EditOperation>();
	}
	
	public void addEditOperation(EditOperation eo) {
		editOperations.add(eo);
	}
	
	public void removeEditOperation(EditOperation eo) {
		editOperations.remove(eo);
	}
	
	// cost
	
	public double getCost() {
		return eoCosts(editOperations);
	}
	
	public double getNodeCost() {
		return eoNodeCosts(editOperations);
	}
	
	public double getEdgeCost() {
		return eoEdgeCosts(editOperations);
	}
	
	public double getSubstitutionCost() {
		return eoCosts(getSubstitutions());
	}
	
	public double getDeletionCost() {
		return eoCosts(getDeletions());
	}
	
	public double getInsertionCost() {
		return eoCosts(getInsertions());
	}

	public LinkedList<EditOperation> getSubstitutions() {
		LinkedList<EditOperation> res = new LinkedList<EditOperation>();
		Iterator<EditOperation> iter = editOperations.iterator();
		while (iter.hasNext()) {
			EditOperation eo = iter.next();
			if (eo.isSubstitution()) {
				res.add(eo);
			}
		}
		return res;
	}

	public LinkedList<EditOperation> getDeletions() {
		LinkedList<EditOperation> res = new LinkedList<EditOperation>();
		Iterator<EditOperation> iter = editOperations.iterator();
		while (iter.hasNext()) {
			EditOperation eo = iter.next();
			if (eo.isDel()) {
				res.add(eo);
			}
		}
		return res;
	}

	public LinkedList<EditOperation> getInsertions() {
		LinkedList<EditOperation> res = new LinkedList<EditOperation>();
		Iterator<EditOperation> iter = editOperations.iterator();
		while (iter.hasNext()) {
			EditOperation eo = iter.next();
			if (eo.isIns()) {
				res.add(eo);
			}
		}
		return res;
	}
	
	private double eoCosts(LinkedList<EditOperation> eos) {
		double res = 0;
		Iterator<EditOperation> iter = eos.iterator();
		while (iter.hasNext()) {
			EditOperation eo = iter.next();
			res += eo.cost;
		}
		return res;
	}
	
	private double eoNodeCosts(LinkedList<EditOperation> eos) {
		double res = 0;
		Iterator<EditOperation> iter = eos.iterator();
		while (iter.hasNext()) {
			EditOperation eo = iter.next();
			res += eo.nodeCost;
		}
		return res;
	}
	
	private double eoEdgeCosts(LinkedList<EditOperation> eos) {
		double res = 0;
		Iterator<EditOperation> iter = eos.iterator();
		while (iter.hasNext()) {
			EditOperation eo = iter.next();
			res += eo.edgeCost;
		}
		return res;
	}
	
	public String toString() {
		DecimalFormat df = new DecimalFormat("#.##");
		DecimalFormat df2 = new DecimalFormat("#.#####");
		String str = "\n*** Edit Path ***\n";
		str += "Substitutions:\n";
		Iterator<EditOperation> iter = getSubstitutions().iterator();
		while (iter.hasNext()) {
			str += iter.next().toString();
		}
		str += "Deletions:\n";
		iter = getDeletions().iterator();
		while (iter.hasNext()) {
			str += iter.next().toString();
		}
		str += "Insertions:\n";
		iter = getInsertions().iterator();
		while (iter.hasNext()) {
			str += iter.next().toString();
		}
		str += "Cost: " + df2.format(getCost()) + " (n:" + df.format(getNodeCost()) + "/e:" + df.format(getEdgeCost()) + ")"
			+ " [s:" + df.format(getSubstitutionCost()) + "/d:" + df.format(getDeletionCost()) + "/i:" + df.format(getInsertionCost()) + "]\n";
		return str;
	}

	// graphviz
	
	public LinkedList<EditOperation> getUndirectedSubstitutions() {
		if (!this.isDirected) {
			return getSubstitutions();
		}
		LinkedList<EditOperation> res = new LinkedList<EditOperation>();
		LinkedList<EditOperation> subs = getSubstitutions();
		Iterator<EditOperation> iter = subs.iterator();
		while (iter.hasNext()) {
			EditOperation eo = iter.next();
			if (eo.isSAB() && hasReverseSubstitution(subs, eo)) {
				res.add(eo);
			}
		}
		return res;
	}
	
	public LinkedList<EditOperation> getSubstitutionsAB() {
		if (!this.isDirected) {
			return new LinkedList<EditOperation>();
		}
		LinkedList<EditOperation> res = new LinkedList<EditOperation>();
		LinkedList<EditOperation> subs = getSubstitutions();
		Iterator<EditOperation> iter = subs.iterator();
		while (iter.hasNext()) {
			EditOperation eo = iter.next();
			if (eo.isSAB() && !hasReverseSubstitution(subs, eo)) {
				res.add(eo);
			}
		}
		return res;
	}
	
	public LinkedList<EditOperation> getSubstitutionsBA() {
		if (!this.isDirected) {
			return new LinkedList<EditOperation>();
		}
		LinkedList<EditOperation> res = new LinkedList<EditOperation>();
		LinkedList<EditOperation> subs = getSubstitutions();
		Iterator<EditOperation> iter = subs.iterator();
		while (iter.hasNext()) {
			EditOperation eo = iter.next();
			if (eo.isSBA() && !hasReverseSubstitution(subs, eo)) {
				res.add(eo);
			}
		}
		return res;
	}
	
	private boolean hasReverseSubstitution(LinkedList<EditOperation> subs, EditOperation eo0) {
		Iterator<EditOperation> iter = subs.iterator();
		while (iter.hasNext()) {
			EditOperation eo = iter.next();
			if (eo.n1 == eo0.n2 && eo.n2 == eo0.n1) {
				return true;
			}
		}
		return false;
	}
	
	private LinkedList<Node> getDeletionNodes() {
		LinkedList<Node> res = new LinkedList<Node>();
		Iterator<EditOperation> iter = getDeletions().iterator();
		while (iter.hasNext()) {
			EditOperation eo = iter.next();
			res.add(eo.n1);
		}
		return res;
	}
	
	private LinkedList<Node> getInsertionNodes() {
		LinkedList<Node> res = new LinkedList<Node>();
		Iterator<EditOperation> iter = getInsertions().iterator();
		while (iter.hasNext()) {
			EditOperation eo = iter.next();
			res.add(eo.n1);
		}
		return res;
	}
	
	public String toGraphViz(double scalexy) {

		// graph
		
		String str = "digraph G {\n";
		//str += "graph [outputorder=edgesfirst];";
		str += "node [shape=circle,style=filled,fontcolor=white,label=\"\"];\n";
		str += "edge [arrowsize=1.4];\n";

		String[] labelsG1 = new String[g1.size()];
		String[] labelsG2 = new String[g2.size()];
		for (int i=0; i < g1.size(); i++) {
			labelsG1[i] = Integer.toString(i);
		}
		for (int j=0; j < g2.size(); j++) {
			labelsG2[j] = "\"" + j + "'\"";
		}

		// nodes
		
		for (int i=0; i < g1.size(); i++) {
			LinkedList<Node> dels = getDeletionNodes();
			if (dels.contains(g1.get(i))) {
				str += labelsG1[i] + " [pos = \"" + (g1.get(i).getFloat("x") / scalexy) + "," + (g1.get(i).getFloat("y") / scalexy) + "!\",fillcolor=gray80,color=black,penwidth=5];\n";
			} else {
				str += labelsG1[i] + " [pos = \"" + (g1.get(i).getFloat("x") / scalexy) + "," + (g1.get(i).getFloat("y") / scalexy) + "!\",color=gray80];\n";
			}
		}
		for (int j=0; j < g2.size(); j++) {
			LinkedList<Node> inss = getInsertionNodes();
			if (inss.contains(g2.get(j))) {
				str += labelsG2[j] + " [pos = \"" + (g2.get(j).getFloat("x") / scalexy) + "," + (g2.get(j).getFloat("y") / scalexy) + "!\",fillcolor=gray40,color=black,penwidth=5];\n";
			} else {
				str += labelsG2[j] + " [pos = \"" + (g2.get(j).getFloat("x") / scalexy) + "," + (g2.get(j).getFloat("y") / scalexy) + "!\",color=gray40];\n";
			}
		}
		
		// edges
		
		for (int i=0; i < g1.size(); i++) {
			if (g1.isDirected()) {
				int[] eOut = g1.getOutgoingEdgeEndings(i);
				for (int k=0; k < eOut.length; k++) {
					str += labelsG1[i] + " -> " + labelsG1[eOut[k]] + " [color=gray80];\n";
				}
			} else {
				for (int i2=i; i2 < g1.size(); i2++) {
					if (g1.getEdge(i, i2) != null) {
						str += labelsG1[i] + " -> " + labelsG1[i2] + " [color=gray80,dir=none];\n";
					}
				}
			}
		}
		for (int j=0; j < g2.size(); j++) {
			if (g2.isDirected()) {
				int[] eOut = g2.getOutgoingEdgeEndings(j);
				for (int k=0; k < eOut.length; k++) {
					str += labelsG2[j] + " -> " + labelsG2[eOut[k]] + " [color=gray40];\n";
				}
			} else {
				for (int j2=j; j2 < g2.size(); j2++) {
					if (g2.getEdge(j, j2) != null) {
						str += labelsG2[j] + " -> " + labelsG2[j2] + " [color=gray40,dir=none];\n";
					}
				}
			}
		}
		
		// assignments
		
		Iterator<EditOperation> iter = getUndirectedSubstitutions().iterator();
		while (iter.hasNext()) {
			EditOperation eo = iter.next();
			int i = g1.indexOf(eo.n1);
			int j = g2.indexOf(eo.n2);
			str += labelsG1[i] + " -> " + labelsG2[j] + " [color=black,penwidth=5,dir=both];\n";
		}
		
		iter = getSubstitutionsAB().iterator();
		while (iter.hasNext()) {
			EditOperation eo = iter.next();
			int i = g1.indexOf(eo.n1);
			int j = g2.indexOf(eo.n2);
			str += labelsG1[i] + " -> " + labelsG2[j] + " [color=black,penwidth=5];\n";
		}
		
		iter = getSubstitutionsBA().iterator();
		while (iter.hasNext()) {
			EditOperation eo = iter.next();
			int i = g1.indexOf(eo.n2);
			int j = g2.indexOf(eo.n1);
			str +=  labelsG2[j] + " -> " + labelsG1[i] + " [color=black,penwidth=5];\n";
		}
		
		str += "}";
		return str;
	}
	
}
