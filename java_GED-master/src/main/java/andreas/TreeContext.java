package andreas;

import java.util.ArrayList;
import java.util.TreeSet;

public class TreeContext {

	ArrayList<Integer> nodes;
	ArrayList<TreeSet<Integer>> levels;
	
	public TreeContext() {
		nodes = new ArrayList<Integer>();
		levels = new ArrayList<TreeSet<Integer>>();
	}
	
	public void add(TreeSet<Integer> level) {
		nodes.addAll(level);
		levels.add(level);
	}
	
	public int size() {
		return nodes.size();
	}
	
	public boolean contains(int i) {
		return nodes.contains(i);
	}
	
	public int numLevels() {
		return levels.size();
	}
	
	public boolean hasLevel(int i) {
		return numLevels() > i;
	}
	
	public TreeSet<Integer> getLevel(int i) {
		return levels.get(i);
	}
	
	public String toString() {
		String res = "";
		for (int i=0; i < levels.size(); i++) {
			res += "level " + i + ": " + levels.get(i).toString() + "\n";
		}
		return res;
	}
	
}
