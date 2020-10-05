package gwenael;

import java.util.ArrayList;


//inspiration: https://stackoverflow.com/questions/4401850/how-to-create-a-multidimensional-arraylist-in-java
// https://stackoverflow.com/a/4401871

public class FiveDimAL<T> extends ArrayList<ArrayList<ArrayList<ArrayList<ArrayList<T>>>>> {

	public T get(int i, int j, int k, int l,int m) {
		return this.get(i).get(j).get(k).get(l).get(m);
	}

	public void set(int i, int j, int k, int l, int m, T val){
		while (i >= this.size()){
			this.add(new ArrayList<ArrayList<ArrayList<ArrayList<T>>>>());
		}
		ArrayList<ArrayList<ArrayList<ArrayList<T>>>> dim1 = this.get(i);
		while (j >= dim1.size()) {
			dim1.add(new ArrayList<ArrayList<ArrayList<T>>>());
		}
		ArrayList<ArrayList<ArrayList<T>>> dim2 = dim1.get(j);
		while (k >= dim2.size()) {
			dim2.add(new ArrayList<ArrayList<T>>());
		}
		ArrayList<ArrayList<T>> dim3 = dim2.get(k);
		while (l >= dim3.size()) {
			dim3.add(new ArrayList<T>());
		}
		ArrayList<T> dim4 = dim3.get(l);
		while(m >= dim4.size()) {
			dim4.add(null);
		}
		dim4.set(m, val);
	}

}
