package gwenael;

import java.util.ArrayList;


//inspiration: https://stackoverflow.com/questions/4401850/how-to-create-a-multidimensional-arraylist-in-java
// https://stackoverflow.com/a/4401871

public class ThreeDimAL<T> extends ArrayList<ArrayList<ArrayList<T>>> {
	public T get(int i, int j, int k) {
		return this.get(i).get(j).get(k);
	}

	public void set(int i, int j, int k, T val){
		while (i >= this.size()){
			this.add(new ArrayList<ArrayList<T>>());
		}
		ArrayList<ArrayList<T>> dim1 = this.get(i);
		while (j >= dim1.size()) {
			dim1.add(new ArrayList<T>());
		}
		ArrayList<T> dim2 = dim1.get(j);
		while (k >= dim2.size()) {
			dim2.add(null);
		}
		dim2.set(k, val);
	}

}
