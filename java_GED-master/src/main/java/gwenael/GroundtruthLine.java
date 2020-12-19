package gwenael;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class GroundtruthLine {

	private String lineID;

	private Polygon polygon;

	private ArrayList<String> groundtruth;

	public GroundtruthLine(Polygon pol, String[] text) {
		this.lineID = text[0];
		this.polygon = pol;
		this.groundtruth = new ArrayList<>();
		for (int i = 1; i < text.length; i++) {
			groundtruth.add(text[i]);
		}
	}

	public String getLineID() {
		return lineID;
	}

	public void setLineID(String lineID) {
		this.lineID = lineID;
	}

	public Polygon getPolygon() {
		return polygon;
	}

	public void setPolygon(Polygon polygon) {
		this.polygon = polygon;
	}

	public ArrayList<String> getGroundtruth() {
		return groundtruth;
	}

	public void setGroundtruth(ArrayList<String> groundtruth) {
		this.groundtruth = groundtruth;
	}
}
