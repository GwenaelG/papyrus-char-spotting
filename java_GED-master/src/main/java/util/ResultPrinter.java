package util;

import java.io.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

import andreas.AStarGED;
import andreas.EditPath;
import andreas.GEDFile;
import andreas.LetterPlotter;
import graph.Graph;
import graph.GraphSet;
import gwenael.FourDimAL;
import gwenael.ThreeDimAL;
import gwenael.TwoDimAL;

import javax.management.ObjectName;


public class ResultPrinter {

	/**
	 * where the results are printed
	 */
	private final String resultFolder;

	/**
	 * the decimalformat for the editdistances found
	 */
	private DecimalFormat decFormat;
	
	/**
	 * the properties defined via GUI or properties file
	 */
	private final Properties properties;

	
	/**
	 * constructs a ResultPrinter 
	 * @param resultFolder
	 * @param properties
	 */
	public ResultPrinter(String resultFolder, Properties properties) {
		this.resultFolder = resultFolder;
		this.properties = properties;
	}

	//andreas
	
	/**prints out the properties and the distance-matrix @param d 
	 * in the resultFolder
	 * @param prop 
	 * @param target
	 * @param source
	 */
	public void printResult(double[][] d, GraphSet source, GraphSet target, String prop, long time) {
		
		// graph ids
		int r = source.size();
		int c = target.size();
		String[] sourceIds = new String[r];
		String[] targetIds = new String[c];
		for (int i = 0; i < r; i++) {
			sourceIds[i] = source.get(i).getGraphID();
		}
		for (int j = 0; j < c; j++) {
			targetIds[j] = target.get(j).getGraphID();
		}
		
		// name of the result file
		String r1 = (new File(prop)).getName();
		int split = r1.lastIndexOf(".");
		String name = r1.substring(0,split);

		// formats
		this.decFormat = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
		this.decFormat.applyPattern("0.00000");
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		Calendar cal = Calendar.getInstance();
		
		// ged file
		String resultName = this.resultFolder+name+".ged";
		System.out.println("name: "+ name);
		System.out.println("folder: "+ this.resultFolder);
//		resultName = resultName.replace("/", sep);
		GEDFile.writeGED(d, sourceIds, targetIds, resultName);

		// info file
		resultName = this.resultFolder+name+".info";
		PrintWriter out;
		try {
			out = new PrintWriter(new FileOutputStream(resultName));

			// andreas
//			out.println("__time " + AStarGED.timeNano);
//			out.println("__open " + AStarGED.numOpen);
//			out.println("__matchings " + AStarGED.numMatchings);
//			out.println("__failures " + AStarGED.numFailures);
//			out.println("__failureIDs" + AStarGED.failures);

			out.println(time);
			out.println("(milliseconds runtime)");
			out.println("timestamp:" + dateFormat.format(cal.getTime()) + "\n");
			out.println("*** The properties of the matching ***\n");
			out.println("Source graph set:\t"+this.properties.getProperty("source")+" ("+d.length+" graphs)");
			out.println("Target graph set:\t"+this.properties.getProperty("target")+" ("+d[0].length+" graphs)\n");
			out.print("Graph edit distance procedure:\t"+this.properties.getProperty("matching")+"\n");
			if (this.properties.getProperty("matching").equals("Beam")){
				out.println("s = "+ Integer.parseInt(properties.getProperty("s"))+"\n");
			}
			int undirected = Integer
					.parseInt(properties.getProperty("undirected"));
			out.print("Edge mode:\t\t");
			if (undirected == 1){
				out.println("undirected\n");
			} else {
				out.println("directed\n");
			}
			out.println("Cost for node deletion/insertion:\t"+this.properties.getProperty("node"));
			out.println("Cost for edge deletion/insertion:\t"+this.properties.getProperty("edge")+"\n");
			out.println("Alpha weighting factor between node and edge costs:\t"+this.properties.getProperty("alpha")+"\n");
			int numOfNodeAttr = Integer.parseInt(properties
					.getProperty("numOfNodeAttr"));
			int numOfEdgeAttr = Integer.parseInt(properties
					.getProperty("numOfEdgeAttr"));

			for (int i = 0; i < numOfNodeAttr; i++) {
				out.print("Node attribute "+i+":\t"+ properties.getProperty("nodeAttr" + i)+";\t");
				out.print("Cost function:\t"+properties.getProperty("nodeCostType" + i)+";\t");
				if (properties.getProperty("nodeCostType" + i).equals("discrete")){
					out.print("mu = "+properties.getProperty("nodeCostMu" + i)+" nu = "+properties.getProperty("nodeCostNu" + i)+";\t");
				}
				out.println("Soft factor:\t"+properties.getProperty("nodeAttr" + i + "Importance"));
			}
			if (numOfNodeAttr==0){
				out.println("No attributes for nodes defined");
			}
			out.println();
			for (int i = 0; i < numOfEdgeAttr; i++) {
				out.print("Edge Attribute "+i+":\t"+ properties.getProperty("edgeAttr" + i)+";\t");
				out.print("Cost Function:\t"+properties.getProperty("edgeCostType" + i)+";\t");
				out.println("Soft Factor:\t"+properties.getProperty("edgeAttr" + i + "Importance"));
			}
			if (numOfEdgeAttr==0){
				out.println("No attributes for edges defined");
			}
			out.println();
			double squareRootNodeCosts = Double.parseDouble(properties
					.getProperty("pNode"));
			int multiplyNodeCosts = Integer.parseInt(properties
					.getProperty("multiplyNodeCosts"));
			double squareRootEdgeCosts = Double.parseDouble(properties
					.getProperty("pEdge"));
			int multiplyEdgeCosts = Integer.parseInt(properties
					.getProperty("multiplyEdgeCosts"));

			if (multiplyNodeCosts==1){
				out.println("Individual node costs are multiplied");
			} else {
				out.println("Individual node costs are added");
			}
			if (multiplyEdgeCosts==1){
				out.println("Individual edge costs are multiplied");
			} else {
				out.println("Individual edge costs are added");
			}
			System.out.println();
			out.println("(Combined node cost)^(1/"+squareRootNodeCosts+")");

			out.println("(Combined edge cost)^(1/"+squareRootEdgeCosts+")");

			int simKernel=Integer.parseInt(properties.getProperty("simKernel"));

			switch (simKernel){
				case 0:
					out.println("\n*** The distance matrix ***\n");
					break;
				case 1:
					out.println("\n*** The similarity matrix (-d^2) ***\n");break;
				case 2:
					out.println("\n*** The similarity matrix (-d) ***\n");break;
				case 3:
					out.println("\n*** The similarity matrix tanh(-d) ***\n");break;
				case 4:
					out.println("\n*** The similarity matrix exp(-d) ***\n");break;
			}
			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public void printEditPath(String prop, Graph source, Graph target, EditPath editPath, int width, int height, boolean display, double size) {

		// name of the result file
		String r1 = prop;
		String[] r2 = r1.split("/");
		String r3 = r2[r2.length-1];
		int split = r3.lastIndexOf(".");
		String name = r3.substring(0,split);

		String s1 = source.getGraphID();
		String[] s2 = s1.split("\\|");
		String sIdx = s2[1];

		String t1 = target.getGraphID();
		String[] t2 = t1.split("\\|");
		String tIdx = t2[1];

		// display editPath
		if (display) {
			LetterPlotter plotter;
			try {
				plotter = new LetterPlotter(source, target, editPath, width, height);
				System.out.println(editPath);
				System.out.println("Source: " + source.getGraphID());
				System.out.println("Target: " + target.getGraphID());
				plotter.displayImage();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// edit path description
			String resultName = this.resultFolder + name + "_" + sIdx + "_" + tIdx + ".edt";
			PrintWriter out;
			try {
				out = new PrintWriter(new FileOutputStream(resultName));
				out.println("source: " + source.getGraphID());
				out.println("target: " + target.getGraphID());
				out.println(editPath);
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			// edit path image
//			resultName = this.resultFolder + name + "_" + sIdx + "_" + tIdx + ".gif";
//			LetterPlotter plotter;
//			try {
//				plotter = new LetterPlotter(source, target, editPath, width, height);
//				plotter.saveImage(resultName);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}

			// edit path graphviz
			resultName = this.resultFolder + name + "_" + sIdx + "_" + tIdx + ".dot";
			try {
				out = new PrintWriter(new FileOutputStream(resultName));
				out.println(editPath.toGraphViz(size));
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		}
	}

	public void printResultGW(GraphSet source, GraphSet target, String prop, long initTime, long matchingTime) {

		// graph ids
		int r = source.size();
		int c = target.size();

		// name of the result file
		String r1 = (new File(prop)).getName();
		int split = r1.lastIndexOf(".");
		String name = r1.substring(0,split);

		// formats
		this.decFormat = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
		this.decFormat.applyPattern("0.00000");
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		Calendar cal = Calendar.getInstance();

		String resultName = this.resultFolder+name+".res";
		PrintWriter out;

		try {
			out = new PrintWriter(new FileOutputStream(resultName));

			out.println("timestamp:" + dateFormat.format(cal.getTime()) + "\n");

			out.println("Init Time:");
			out.print(initTime+" milliseconds = ~ ");
			long initTimeMin = initTime / 60000;
			long initTimeSec = (initTime / 1000) % 60;
			out.println(initTimeMin+"'"+initTimeSec+"\"");

			out.println("Matching Time: ");
			out.print(matchingTime+" milliseconds = ~ ");
			long matchingTimeMin = matchingTime / 60000;
			long matchingTimeSec = (matchingTime / 1000) % 60;
			out.println(matchingTimeMin+"'"+matchingTimeSec+"\"\n");

			out.println("*** The properties of the matching ***\n");
			out.println("Source graph set:\t"+this.properties.getProperty("sourceFile")+" ("+r+" graphs)");
			out.println("Target graph set:\t"+this.properties.getProperty("targetFile")+" ("+c+" graphs)");int totalNodeCount = 0;

			for (int i = 0; i < source.size(); i++){
				totalNodeCount += source.get(i).size();
			}
			for (int i = 0; i < target.size(); i++) {
				totalNodeCount += target.get(i).size();
			}
			out.println("Total number of nodes: "+totalNodeCount);

			out.println("Graph edit distance procedure:\t"+this.properties.getProperty("matching")+"\n");

			out.println("Cost for node deletion/insertion:\t"+this.properties.getProperty("node"));
			out.println("Cost for edge deletion/insertion:\t"+this.properties.getProperty("edge")+"\n");
			out.println("Alpha weighting factor between node and edge costs:\t"+this.properties.getProperty("alpha")+"\n");
			int numOfNodeAttr = Integer.parseInt(properties
					.getProperty("numOfNodeAttr"));
			int numOfEdgeAttr = Integer.parseInt(properties
					.getProperty("numOfEdgeAttr"));

			for (int i = 0; i < numOfNodeAttr; i++) {
				out.print("Node attribute "+i+":\t"+ properties.getProperty("nodeAttr" + i)+";\t");
				out.print("Cost function:\t"+properties.getProperty("nodeCostType" + i)+";\t");
				if (properties.getProperty("nodeCostType" + i).equals("discrete")){
					out.print("mu = "+properties.getProperty("nodeCostMu" + i)+" nu = "+properties.getProperty("nodeCostNu" + i)+";\t");
				}
				out.println("Soft factor:\t"+properties.getProperty("nodeAttr" + i + "Importance"));
			}
			if (numOfNodeAttr==0){
				out.println("No attributes for nodes defined");
			}
			out.println();
			for (int i = 0; i < numOfEdgeAttr; i++) {
				out.print("Edge Attribute "+i+":\t"+ properties.getProperty("edgeAttr" + i)+";\t");
				out.print("Cost Function:\t"+properties.getProperty("edgeCostType" + i)+";\t");
				out.println("Soft Factor:\t"+properties.getProperty("edgeAttr" + i + "Importance"));
			}
			if (numOfEdgeAttr==0){
				out.println("No attributes for edges defined");
			}
			out.println();

			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

}
