
package gwenael;

import algorithms.GraphMatchingSegFree;

import java.io.IOException;


public class Test {
	public static void main(String[] args) throws IOException {
		String prop = "test/papyrus/properties/gwenael_small.prop";
		String prop2 = "test/papyrus/properties/gwenael_small2.prop";
		try {
			new GraphMatchingSegFree(prop);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			new	GraphMatchingSegFree(prop2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
