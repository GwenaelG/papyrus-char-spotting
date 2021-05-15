
package gwenael;

import algorithms.GMSF_save;
import algorithms.GraphMatchingSegFree;

import java.io.File;
import java.io.IOException;


public class RunCompl3 {
	public static void main(String[] args) throws IOException {
		File propDirectory = new File("test/papyrus/properties/test_02_3versions/core1");
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
//		try {
//			ImageDisp iD = new ImageDisp("C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/files/original_bin/patches/orig_bin_02_patch.png");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
}