package andreas;

import java.util.LinkedList;

public class GEDPattern {
	
	public String fullId;
	protected LinkedList<GEDPatternDistance> distances;
	
	public GEDPattern(String fullId) {
		this.fullId = fullId;
		this.distances = new LinkedList<GEDPatternDistance>();
	}
	
	public void add(GEDPatternDistance dist) {
		distances.add(dist);
	}
	
	public LinkedList<GEDPatternDistance> getDistances() {
		return distances;
	}
	
	public String getClassId() {
		int split = fullId.indexOf("|");
		return fullId.substring(0,split);
	}
	
	public String toString() {
		String str = "";
		str += fullId + " (" + distances.size() + "): ";
		str += distances.getFirst().toString() + " ... ";
		str += distances.getLast().toString();
		return str;
	}
	
}
