
package gwenael;

import algorithms.GMSF_save;

import java.io.File;
import java.io.IOException;


public class RunCompl2 {
	public static void main(String[] args) throws IOException {
		File propDirectory = new File("test/papyrus/properties/gw/cont/test10w/core1");
		File[] props = propDirectory.listFiles();
		for (int i = 0; i < props.length; i++) {
			if(props[i].isFile()) {
				try {
					System.out.println("Property file: "+props[i].getPath());
					new GMSF_save(props[i].getPath());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}