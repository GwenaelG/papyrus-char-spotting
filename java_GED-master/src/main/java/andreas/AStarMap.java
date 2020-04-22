package andreas;

public class AStarMap implements Comparable<AStarMap> {

	public static int mapCounter = 0; // number of AStarMaps instantiated in the RAM
	private static final int FREE = -2; // index to indicate unmapped nodes
	private static final int EPSILON = -1; // index to indicate deleted or inserted nodes

	private int mapID;
	private int idxG1; // currently mapped node of g1, i.e. the depth of the search tree
	private boolean complete;
	private double pastCost;
	private double futureCost;
	private int[] map12;
	private int[] map21;
	private int[] freeIdxG2; // currently unmapped nodes of g2, i.e. the branches of the search tree
	
	public AStarMap(int n1, int n2) {
		mapID = mapCounter++;
		idxG1 = -1;
		complete = false;
		pastCost = 0;
		futureCost = 0;
		map12 = new int[n1];
		for (int i=0; i < n1; i++) {
			map12[i] = FREE;
		}
		map21 = new int[n2];
		for (int j=0; j < n2; j++) {
			map21[j] = FREE;
		}
		freeIdxG2 = new int[n2];
		for (int j=0; j < n2; j++) {
			freeIdxG2[j] = j;
		}
	}
	
	// create a successor map such that node i in g1 is either deleted (j is epsilon) or mapped to node j in g2
	public AStarMap(AStarMap map, int i, int j) {
		mapID = mapCounter++;
		idxG1 = i;
		complete = false;
		pastCost = map.pastCost;
		futureCost = 0;
		map12 = map.map12.clone();
		map21 = map.map21.clone();
		map12[i] = j;
		if (j == EPSILON) {
			freeIdxG2 = map.freeIdxG2.clone();
		} else {
			map21[j] = i;
			freeIdxG2 = removeFreeIdxG2(map.freeIdxG2, j);
		}
	}
	
	private int[] removeFreeIdxG2(int[] oldFree, int j) {
		int[] newFree = new int[oldFree.length - 1];
		int k = 0;
		for (int l=0; l < oldFree.length; l++) {
			int freeIdx = oldFree[l];
			if (freeIdx != j) {
				newFree[k] = freeIdx;
				k++;
			}
		}
		return newFree;
	}
	
	// sort

	@Override
	public int compareTo(AStarMap map) {
		int result = this.getTotalCost().compareTo(map.getTotalCost());
		if (result != 0) {
			return result;
		} else {
			return this.getMapID().compareTo(map.getMapID());
		}
	}

	// cost
	
	public void addMappingCost(double cost) {
		pastCost += cost;
	}
	
	public void setFutureCost(double cost) {
		futureCost = cost;
	}

	public Double getTotalCost() {
		return pastCost + futureCost;
	}
	
	// map
	
	public Integer getMapID() {
		return mapID;
	}
	
	public int getIdxG1() {
		return idxG1;
	}
	
	public boolean isLeaf() {
		return idxG1 == (map12.length - 1);
	}
	
	public boolean isComplete() {
		return complete;
	}
	
	public void setComplete() {
		complete = true;
	}

	public int[] getMap12() {
		return map12;
	}

	public int[] getMap21() {
		return map21;
	}

	// g1
	
	public int sizeFreeG1() {
		return map12.length - 1 - idxG1;
	}

	public boolean isFreeG1(int i) {
		return map12[i] == FREE;
	}
	
	public int getFreeG1(int k) {
		return idxG1 + 1 + k;
	}
	
	public boolean isDeletedG1(int i) {
		return map12[i] == EPSILON;
	}
	
	public int getMappedG1(int i) {
		return map12[i];
	}
	
	// g2
	
	public int sizeFreeG2() {
		return freeIdxG2.length;
	}

	public boolean isFreeG2(int j) {
		return map21[j] == -2;
	}
	
	public int getFreeG2(int k) {
		return freeIdxG2[k];
	}
	
	public void resetFreeIdxG2() {
		freeIdxG2 = new int[0];
	}
	
	public boolean isInsertedG2(int j) {
		return map21[j] == -1;
	}
	
	public void setInsertedG2(int j) {
		map21[j] = -1;
	}
	
	public int getMappedG2(int j) {
		return map21[j];
	}

}
