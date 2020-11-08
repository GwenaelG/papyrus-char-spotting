package gwenael;

import java.util.ArrayList;


//inspiration: https://stackoverflow.com/questions/4401850/how-to-create-a-multidimensional-arraylist-in-java
// https://stackoverflow.com/a/4401871

public class TwoDimAL<T> extends ArrayList<ArrayList<T>> {
	public T get(int i, int j) {
		return this.get(i).get(j);
	}

	public void set(int i, int j, T val){
		while (i >= this.size()){
			this.add(new ArrayList<T>());
		}
		ArrayList<T> dim1 = this.get(i);
		while (j >= dim1.size()) {
			dim1.add(null);
		}
		dim1.set(j, val);
	}

}
