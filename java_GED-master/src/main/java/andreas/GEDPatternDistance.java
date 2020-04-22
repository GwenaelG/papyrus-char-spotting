package andreas;

public class GEDPatternDistance implements Comparable<GEDPatternDistance> {

	public String fullId;
	public Double distance;
	
	public GEDPatternDistance(String fullId, double distance) {
		this.fullId = fullId;
		this.distance = distance;
	}
	
	public int compareTo(GEDPatternDistance res) {
		int result = this.distance.compareTo(res.distance);
		if (result == 0) {
			result = this.fullId.compareTo(res.fullId);
		}
		return result;
	}
	
	public String getClassId() {
		int split = fullId.indexOf("|");
		return fullId.substring(0,split);
	}
	
	public String toString() {
		return fullId + "," + distance;
	}
	
}
