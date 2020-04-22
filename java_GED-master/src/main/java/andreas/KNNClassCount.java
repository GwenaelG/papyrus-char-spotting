package andreas;

public class KNNClassCount implements Comparable<KNNClassCount> {
	public String classId;
	public Integer count;
	
	public KNNClassCount(String classId) {
		this.classId = classId;
		this.count = 0;
	}
	
	public void increment() {
		this.count++;
	}
	
	public int compareTo(KNNClassCount cnt) {
		int result = cnt.count.compareTo(this.count);
		return result;
	}
}
