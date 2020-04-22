package graph;

import net.sourceforge.gxl.*;

import java.util.Hashtable;

public class Edge {

	/** the identifier of the edge */
	private String edgeID;

	/** the attributes of the edge*/
	private Hashtable<String, GXLValue> attributes;

	/** the start and end node of the edge*/
	private Node startNode;
	private Node endNode;

	/**
	 * Constructors
	 */
	public Edge() {
		this.attributes = new Hashtable<String, GXLValue>();
	}

	public Edge(String id) {
		this.edgeID		= id;
		this.attributes	= new Hashtable<String, GXLValue>();
	}

	/**
	 * gets the other end of the edge
	 */
	public Node getOtherEnd(Node n) {
		if (n.equals(this.startNode)){
			return this.endNode;
		} else {
			return this.startNode;
		}

	}

	/**
	 * puts a new attribute
	 * in the attribute-table
	 */
	public void put(String key, GXLValue value){
		this.attributes.put(key, value);
	}

	/**
	 * @return the attribute-value of
	 * @param key
	 */
	public GXLValue getValue(String key){
		return this.attributes.get(key);
	}

	public void setBoolean(String key, Boolean value){
		GXLBool gxlBool = new GXLBool(value);
		this.attributes.put(key,gxlBool);
	}

	public boolean getBoolean(String key){
		GXLBool gxlBool = (GXLBool) this.attributes.get(key);
		return gxlBool.getBooleanValue();
	}

	public void setFloat(String key, Float value){
		GXLFloat gxlFloat = new GXLFloat(value);
		this.attributes.put(key,gxlFloat);
	}

	public float getFloat(String key){
		GXLFloat gxlFloat = (GXLFloat) this.attributes.get(key);
		return gxlFloat.getFloatValue();
	}

	public void setDouble(String key, Double value){
		GXLFloat gxlFloat = new GXLFloat(value.floatValue());
		this.attributes.put(key,gxlFloat);
	}

	public double getDouble(String key){
		return this.getFloat(key);
	}

	public void setInt(String key, Integer value){
		GXLInt gxlInt = new GXLInt(value);
		this.attributes.put(key,gxlInt);
	}

	public int getInt(String key){
		GXLInt gxlFloat = (GXLInt) this.attributes.get(key);
		return gxlFloat.getIntValue();
	}

	public void setString(String key, String value){
		GXLString gxlString = new GXLString(value);
		this.attributes.put(key,gxlString);
	}

	public String getString(String key){
		GXLString gxlString = (GXLString) this.attributes.get(key);
		return gxlString.getValue();
	}

	/**
	 * generates a printable string of the edge
	 */
	public String toString(){
		String edge = this.edgeID+" ";
		edge += this.attributes;
		return edge;
	}

	/**
	 * some getters and setters
	 */

	public String getEdgeID() {
		return edgeID;
	}

	public void setEdgeID(String edgeID) {
		this.edgeID = edgeID;
	}

	public Node getStartNode() {
		return startNode;
	}

	public void setStartNode(Node startNode) {
		this.startNode = startNode;
	}

	public Node getEndNode() {
		return endNode;
	}

	public void setEndNode(Node endNode) {
		this.endNode = endNode;
	}

	public Hashtable<String, GXLValue> getAttributes() {
		return attributes;
	}

	public void setAttributes(Hashtable<String, GXLValue> attributes) {
		this.attributes = attributes;
	}
}
