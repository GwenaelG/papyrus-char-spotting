package gwenael;

import java.util.ArrayList;


//inspiration: https://stackoverflow.com/questions/4401850/how-to-create-a-multidimensional-arraylist-in-java
// https://stackoverflow.com/a/4401871

public class FourDimAL<T> extends ArrayList<ArrayList<ArrayList<ArrayList<T>>>> {
	public T get(int i, int j, int k, int l) {
		return this.get(i).get(j).get(k).get(l);
	}

	public void set(int i, int j, int k, int l, T val){
		while (i >= this.size()){
			this.add(new ArrayList<ArrayList<ArrayList<T>>>());
		}
		ArrayList<ArrayList<ArrayList<T>>> dim1 = this.get(i);
		while (j >= dim1.size()) {
			dim1.add(new ArrayList<ArrayList<T>>());
		}
		ArrayList<ArrayList<T>> dim2 = dim1.get(j);
		while (k >= dim2.size()) {
			dim2.add(new ArrayList<T>());
		}
		ArrayList<T> dim3 = dim2.get(k);
		while (l >= dim3.size()) {
			dim3.add(null);
		}
		dim3.set(l, val);
	}

}
