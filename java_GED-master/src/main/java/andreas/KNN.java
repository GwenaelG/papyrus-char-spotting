package andreas;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;


public class KNN {
	
	public KNN(String prop) throws Exception {

		// read settings
		Properties properties = new Properties();
		FileInputStream fis = new FileInputStream(prop);
		properties.load(fis);
		fis.close();
		int knnK = new Integer(properties.getProperty("knn"));
		String resultFolder = properties.getProperty("result");
		String r1 = prop;
		String[] r2 = r1.split("/");
		String r3 = r2[r2.length-1];
		int split = r3.lastIndexOf(".");
		String name = r3.substring(0,split);
		String gedResult = resultFolder + name + ".ged";
		String knnResult = resultFolder + name + ".knn";
		String sigResult = resultFolder + name + ".sig";
		
		// read ged results
		LinkedList<GEDPattern> patterns = GEDFile.readGED(gedResult);
//		System.out.println(patterns.size() + " patterns");
//		System.out.println(patterns.getFirst());
//		System.out.println("...");
//		System.out.println(patterns.getLast());
		
		// classify
		int success = 0;
		int num = 0;
		Iterator<GEDPattern> iter = patterns.iterator();
		KNNClassHist hist = new KNNClassHist();
		GEDPattern pattern;
		LinkedList<GEDPatternDistance> sortedDistances;
		LinkedList<String> sigs = new LinkedList<String>();
		LinkedList<String> theknns = new LinkedList<String>();
		while (iter.hasNext()) {
			pattern = iter.next();
			hist.clear();
			sortedDistances = pattern.getDistances();
			Collections.sort(sortedDistances);
			for (int i=0; i < knnK; i++) {
				hist.increment(sortedDistances.get(i).getClassId());
			}
			String theClassId = hist.mostFrequent();
			if (hist.hasTie()) {
				theClassId = sortedDistances.getFirst().getClassId();
			}
			if (pattern.getClassId().compareTo(theClassId) == 0) {
				success++;
				sigs.add(pattern.fullId + " 100");
			} else {
				sigs.add(pattern.fullId + " 0");
			}
			String theknn = pattern.fullId + "#" + hist.mostFrequent();
			for (int i=0; i < knnK; i++) {
				theknn += "#" + sortedDistances.get(i).toString();
			}
			theknns.add(theknn);
			num++;
		}
		double acc = 1.0*success/num;
		System.out.println(round(acc) + " [" + success + "/" + num + "]");
		
		// write results
		PrintWriter out;
		try {
			out = new PrintWriter(new FileOutputStream(knnResult));
			out.println(round(acc));
			out.println(success + "/" + num);
			Iterator<String> iter2 = theknns.iterator();
			while (iter2.hasNext()) {
				out.println(iter2.next());
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// write significance file
		try {
			out = new PrintWriter(new FileOutputStream(sigResult));
			Iterator<String> iter2 = sigs.iterator();
			while (iter2.hasNext()) {
				out.println(iter2.next());
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	private double round(double d) {
		d *= 100000.0;
		d = Math.round(d);
		d /= 100000.0;
		return d;
	}
	
	public static void main(String[] args) {
		try {
			String p = args[0];
			new KNN(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
