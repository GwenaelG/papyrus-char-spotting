
package gwenael;

import algorithms.GraphMatchingSegFree;
import algorithms.GraphMatchingSegFreeGW;

import java.io.File;
import java.io.IOException;


public class RunCompl {
	public static void main(String[] args) throws IOException {
		File propDirectory = new File("test/papyrus/properties/gw/cont/test/core1");
		File[] props = propDirectory.listFiles();
//		for (int i = 0; i < props.length; i++) {
		for (int i = 0; i < 1; i++) {
			if(props[i].isFile()) {
				try {
					System.out.println("Property file: "+props[i].getPath());
					new GraphMatchingSegFreeGW(props[i].getPath());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
//		try {
//			ImageDisp iD = new ImageDisp("C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/files/original_bin/patches/orig_bin_02_patch.png");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
}