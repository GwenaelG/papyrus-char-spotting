
package gwenael;

import algorithms.GMSF_save;
import algorithms.GraphMatchingSegFree;
import algorithms.GraphMatchingSegFreeDummy;
import algorithms.GraphMatchingSegFreeGW;

import java.io.File;
import java.io.IOException;


public class RunCompl {
	public static void main(String[] args) throws IOException {
		File propDirectory = new File("test/papyrus/properties/gw/cont/test3w/core1/");
		File[] props = propDirectory.listFiles();
		for (int i = 0; i < props.length; i++) {
//		for (int i : new int[]{0,2,3,4,5,6,7,8,9,1}) {
			if(props[i].isFile()) {
				try {
					System.out.println("Property file: "+props[i].getPath());
					new GraphMatchingSegFreeGW(props[i].getPath());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		propDirectory = new File("test/papyrus/properties/gw/cont/test10w/core1");
		props = propDirectory.listFiles();
		for (int i = 0; i < props.length; i++) {
			if(props[i].isFile()) {
				try {
					System.out.println("Property file: "+props[i].getPath());
					new GraphMatchingSegFreeGW(props[i].getPath());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
//		propDirectory = new File("test/papyrus/properties/test_02_3versions/core1");
//		props = propDirectory.listFiles();
//		for (int i = 0; i < props.length; i++) {
//			if(props[i].isFile()) {
//				try {
//					System.out.println("Property file: "+props[i].getPath());
//					new GraphMatchingSegFree(props[i].getPath());
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
	}
}