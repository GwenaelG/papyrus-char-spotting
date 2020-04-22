//package algorithms;
//
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.PrintWriter;
//
//import graph.Edge;
//import graph.Graph;
//import xml.XMLParser;
//
//public class GraphViz {
//
//	public static void main(String[] args) {
//		String type = args[0];
//		String dirIn = args[1];
//		String dirOut = args[2];
//		String id = args[3];
//
//		String fileGxl = dirIn + "/" + id + ".gxl";
//		String fileDot = dirOut + "/" + id + ".dot";
//
//		Graph g = null;
//		try {
//			XMLParser xmlParser = new XMLParser();
//			g = xmlParser.parseGXL(fileGxl);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		String dot = null;
//		if (type.compareTo("letters") == 0) {
//			dot = vizLetter(g);
//		} else if (type.compareTo("fingerprints") == 0) {
//			dot = vizFingerprint(g);
//		} else if (type.compareTo("molecules") == 0) {
//			dot = vizMolecule(g);
//		} else {
//			System.err.println("Unknown graph type: " + type);
//		}
//
//		if (dot != null) {
//			try {
//				PrintWriter out = new PrintWriter(new FileOutputStream(fileDot));
//				out.println(dot);
//				out.close();
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//
//	public static String vizLetter(Graph g) {
//		float scalexy = .3f;
//
//		// graph
//
//		String str = "digraph G {\n";
//		str += "graph [outputorder=edgesfirst];";
//		str += "node [shape=circle,style=filled,label=\"\",color=black];\n";
//		str += "edge [arrowsize=1.4,color=black,dir=none];\n";
//
//		String[] labels = new String[g.size()];
//		for (int i=0; i < g.size(); i++) {
//			labels[i] = Integer.toString(i);
//		}
//
//		// nodes
//
//		for (int i=0; i < g.size(); i++) {
//			str += labels[i] + " [pos = \"" + (new Float(g.get(i).getValue("x")) / scalexy) + "," + (new Float(g.get(i).getValue("y")) / scalexy) + "!\"];\n";
//		}
//
//		// edges
//
//		Edge[][] adj = g.getAdjacenyMatrix();
//		for (int i=0; i < g.size(); i++) {
//			for (int j=i; j < g.size(); j++) {
//				Edge e = adj[i][j];
//				if (e != null) {
//					str += labels[i] + " -> " + labels[j] + ";\n";
//				}
//			}
//		}
//
//		str += "}";
//		return str;
//	}
//
//	public static String vizFingerprint(Graph g) {
//		float scalexy = 8f;
//
//		// graph
//
//		String str = "digraph G {\n";
//		str += "graph [outputorder=edgesfirst];";
//		str += "node [shape=circle,style=filled,label=\"\",color=black];\n";
//		str += "edge [arrowsize=1.4,color=black];\n";
//
//		String[] labels = new String[g.size()];
//		for (int i=0; i < g.size(); i++) {
//			labels[i] = Integer.toString(i);
//		}
//
//		// nodes
//
//		for (int i=0; i < g.size(); i++) {
//			str += labels[i] + " [pos = \"" + (new Float(g.get(i).getValue("x")) / scalexy) + "," + (new Float(g.get(i).getValue("y")) / scalexy) + "!\"];\n";
//		}
//
//		// edges
//
//		for (int i=0; i < g.size(); i++) {
//			int[] eOut = g.getOutgoingEdgeEndings(i);
//			for (int k=0; k < eOut.length; k++) {
//				str += labels[i] + " -> " + labels[eOut[k]] + ";\n";
//			}
//		}
//
//		str += "}";
//		return str;
//	}
//
//	public static String vizMolecule(Graph g) {
//		float scalexy = .6f;
//
//		// graph
//
//		String str = "digraph G {\n";
//		str += "graph [outputorder=edgesfirst];";
//		str += "node [shape=circle,style=filled,color=black,fontcolor=white];\n";
//		str += "edge [arrowsize=1.4,color=black,dir=none];\n";
//
//		String[] labels = new String[g.size()];
//		for (int i=0; i < g.size(); i++) {
//			labels[i] = g.get(i).getValue("symbol").replaceAll(" ", "") + i;
//		}
//
//		// nodes
//
//		for (int i=0; i < g.size(); i++) {
//			str += labels[i] + " [label=\"" + g.get(i).getValue("symbol") + "\",pos = \"" + (new Float(g.get(i).getValue("x")) / scalexy) + "," + (new Float(g.get(i).getValue("y")) / scalexy) + "!\"];\n";
//		}
//
//		// edges
//
//		Edge[][] adj = g.getAdjacenyMatrix();
//		for (int i=0; i < g.size(); i++) {
//			for (int j=i; j < g.size(); j++) {
//				Edge e = adj[i][j];
//				if (e != null) {
//					str += labels[i] + " -> " + labels[j] + ";\n";
//				}
//			}
//		}
//
//		str += "}";
//		return str;
//	}
//
//}
