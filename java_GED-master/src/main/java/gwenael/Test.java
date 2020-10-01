
package gwenael;

import algorithms.GraphMatchingSegFree;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class Test {
	public static void main(String[] args) throws IOException {
		File propDirectory = new File("test/papyrus/properties/core1");
		File[] props = propDirectory.listFiles();
		for (int i = 0; i < props.length; i++) {
			if(props[i].isFile()) {
				try {
					System.out.println("Property file: "+props[i].getPath());
					new GraphMatchingSegFree(props[i].getPath());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}