package andreas;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class KNNClassHist {
	
	public HashMap<String,KNNClassCount> counts;
	
	public KNNClassHist() {
		counts = new HashMap<String,KNNClassCount>();
	}
	
	public void clear() {
		counts.clear();
	}
	
	public void increment(String classId) {
		if (counts.get(classId) == null) {
			counts.put(classId, new KNNClassCount(classId));
		}
		counts.get(classId).increment();
	}
	
	public boolean hasTie() {
		LinkedList<KNNClassCount> sortedCounts = getSortedCounts();
		return sortedCounts.size() > 1 && sortedCounts.get(0).compareTo(sortedCounts.get(1)) == 0;
	}
	
	public String mostFrequent() {
		LinkedList<KNNClassCount> sortedCounts = getSortedCounts();
		return sortedCounts.getFirst().classId;
	}
	
	public int mostFrequentCount() {
		LinkedList<KNNClassCount> sortedCounts = getSortedCounts();
		return sortedCounts.getFirst().count;
	}
	
	private LinkedList<KNNClassCount> getSortedCounts() {
		LinkedList<KNNClassCount> sortedCounts = new LinkedList<KNNClassCount>(counts.values());
		Collections.sort(sortedCounts);
		return sortedCounts;
	}
	
}
