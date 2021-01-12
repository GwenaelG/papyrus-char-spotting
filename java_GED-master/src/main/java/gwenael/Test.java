package gwenael;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class Test {
	public static void main(String[] args){
		FourDimAL<Integer> fdal = new FourDimAL<>();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				for (int k = 0; k < 5; k++) {
					for (int l = 0; l < 2; l++) {
						fdal.set(i,j,k,l,9);
					}
				}
			}
		}
		fdal.get(2).get(2).add(new ArrayList<Integer>(Arrays.asList(8,8)));
		for (int i = 0; i < fdal.size(); i++) {
			System.out.println(i);
			for (int j = 0; j < fdal.get(i).size(); j++) {
				System.out.println("\t"+j);
				for (int k = 0; k < fdal.get(i).get(j).size(); k++) {
					System.out.println("\t\t"+k+" "+fdal.get(i,j,k,0)+" "+fdal.get(i,j,k,1));
				}
			}
		}
	}
}
