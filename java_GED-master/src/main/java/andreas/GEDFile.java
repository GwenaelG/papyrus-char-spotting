package andreas;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class GEDFile {

	public static void writeGED(double[][] distances, String[] sourceClasses, String[] targetClasses, String resultName){
		System.out.println("writing GED ..");
		PrintWriter out;
		try {
			out = new PrintWriter(new FileOutputStream(resultName));
			for (int i = 0; i < distances.length; i++){
				out.print(sourceClasses[i]+" ");
				for (int j = 0; j < distances[0].length; j++){
					double d = distances[i][j];
					d = GEDFile.round(d);
					out.print(targetClasses[j]+","+d+" ");
				}
				out.print("\n");
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static double round(double d) {
		d *= 100000.0;
		d = Math.round(d);
		d /= 100000.0;
		return d;
	}

	public static LinkedList<GEDPattern> readGED(String resultName) {
		try {
			LinkedList<GEDPattern> patterns = new LinkedList<GEDPattern>();
			BufferedInputStream stream = new BufferedInputStream(new FileInputStream(resultName));
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String line;
			while ((line = reader.readLine()) != null) {
				StringTokenizer tokenizer = new StringTokenizer(line);
				String fullId = tokenizer.nextToken();
				GEDPattern pattern = new GEDPattern(fullId);
				while(tokenizer.hasMoreElements()) {
					String str = tokenizer.nextToken();
					int comma = str.indexOf(",");
					fullId = str.substring(0,comma);
					double distance = new Double(str.substring(comma+1));
					pattern.add(new GEDPatternDistance(fullId, distance));
				}
				patterns.add(pattern);
			}
			stream.close();
			return patterns;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
