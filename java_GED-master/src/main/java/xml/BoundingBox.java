package xml;

import java.util.ArrayList;
import java.util.HashMap;

public class BoundingBox {

	private String id;

	private String character;

	private int[] coords;

	public BoundingBox(String id, String character, int[] coords){
		this.id = id;
		this.character = character;
		this.coords = coords;
	}

	@Override
	public String toString() {
		return "BoundingBox{" +
				"id='" + id + '\'' +
				", character='" + character + '\'' +
				", coords=" + coords +
				'}';
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCharacter() {
		return character;
	}

	public void setCharacter(String character) {
		this.character = character;
	}

	public int[] getCoords() {
		return coords;
	}

	public void setCoords(int[] coords) {
		this.coords = coords;
	}
}
