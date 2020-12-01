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

	public void printResultGw(String name, GraphSet source, GraphSet target, double[] windowSizes, double[] thresholds,
							  FourDimAL<Integer> TP, FourDimAL<Integer> FN, FourDimAL<Integer> FP,
							  FourDimAL<Integer> TN){
		String resultNameComplete = this.resultFolder + "/" + name + "_complete.res";
		String resultNamePrecRec = this.resultFolder + "/" + name + "_precision_recall.res";

		try {
			PrintWriter wrComp = new PrintWriter(new FileOutputStream(resultNameComplete));
			PrintWriter wrPrecRec = new PrintWriter(new FileOutputStream(resultNamePrecRec));

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

			ThreeDimAL<Double> bestPrecision = new ThreeDimAL<>();
			ThreeDimAL<Double> bestRecall = new ThreeDimAL<>();

			for (int i = 0; i < r; i++) {
				for (int j = 0; j < c; j++) {
					wrComp.println(sourceIds[i]+" "+targetIds[j]);
					bestPrecision.set(i,j,0,0.);
					bestPrecision.set(i,j,1,0.);
					bestPrecision.set(i,j,2,0.);
					bestPrecision.set(i,j,3,0.);
					bestRecall.set(i,j,0,0.);
					bestRecall.set(i,j,1,0.);
					bestRecall.set(i,j,2,0.);
					bestRecall.set(i,j,3,0.);

					for (int k = 0; k < windowSizes.length; k++){
						wrComp.println("-----------------");
						wrComp.println("winsize: x"+windowSizes[k]);
						for( int l = 0; l < thresholds.length; l++) {
							double thresh = thresholds[l];
							wrComp.println("threshold: "+thresh);
							int nbTP = TP.get(i, j, k,l);
							int nbFN = FN.get(i, j, k, l);
							int nbFP = FP.get(i, j, k, l);
							int nbTN = TN.get(i, j, k, l);
//							int sum = nbTP + nbTN + nbFP + nbFN;
//							wrComp.println("total: " + sum);
							double precision = (double) nbTP / (nbTP + nbFP);
							double recall = (double) nbTP / (nbTP + nbFN);
							if (precision > bestPrecision.get(i,j,0)) {
								bestPrecision.set(i,j,0,precision);
								bestPrecision.set(i,j,1,(double) k);
								bestPrecision.set(i,j,2,(double) l);
								bestPrecision.set(i,j,3,recall);
							}

							if (recall > bestRecall.get(i,j,0)) {
								bestRecall.set(i,j,0,recall);
								bestRecall.set(i,j,1, (double) k);
								bestRecall.set(i,j,2, (double) l);
								bestRecall.set(i,j,3, precision);
							}
							wrComp.print("\tP: " + String.format("%.3f", precision) + "; R: " + String.format("%.3f\n", recall));
							wrComp.print("\tTP: " + nbTP + "; ");
							wrComp.print("FN: " + nbFN + "; ");
							wrComp.print("FP: " + nbFP + "; ");
							wrComp.print("TN: " + nbTN + "\n");
						}
					}
					wrPrecRec.println(sourceIds[i]+" "+targetIds[j]);

					String nextLine = "best precision: "+ String.format("%.3f", bestPrecision.get(i,j,0));
					nextLine += ", recall: " + String.format("%.3f", bestPrecision.get(i,j,3));
					nextLine += ", win: " + String.format("%.2f", windowSizes[bestPrecision.get(i,j,1).intValue()]);
					nextLine += ", thresh: "+ String.format("%.2f", thresholds[bestPrecision.get(i,j,2).intValue()]);
					wrPrecRec.println(nextLine);
					nextLine = "best recall: "+ String.format("%.3f", bestRecall.get(i,j,0));
					nextLine += ", precision: "+ String.format("%.3f", bestRecall.get(i,j,3));
					nextLine += ", win: " + String.format("%.2f", windowSizes[bestRecall.get(i,j,1).intValue()]);
					nextLine += ", thresh: "+ String.format("%.2f", thresholds[bestRecall.get(i,j,2).intValue()])+"\n";
					wrPrecRec.println(nextLine);
				}
			}


			wrComp.close();
			wrPrecRec.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
