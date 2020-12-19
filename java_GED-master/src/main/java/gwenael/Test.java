package gwenael;

import java.io.IOException;
import java.nio.file.Paths;

public class Test {
	public static void main(String[] args){
		GroundtruthPage gtp = new GroundtruthPage("a");
		try {
			gtp.extractGroundtruthLines(Paths.get("C:/Users/Gwenael/Desktop/MT/Georges Washington/gwdb/pages-gt/270.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
