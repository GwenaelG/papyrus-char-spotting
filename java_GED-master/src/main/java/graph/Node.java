/**
 * 
 */
package graph;

import graph.Edge;
import graph.Graph;
import net.sourceforge.gxl.*;

import java.util.Hashtable;
import java.util.LinkedList;

/**
 * @author riesen
 *
 */
public class Node {

	/** the identifier of the node */
	private String nodeID;

	/** the graph of the node*/
	private Graph graph;

	/** the attributes of the node*/
	private Hashtable<String, GXLValue> attributes;

	/** the edges adjacent with this node */
	private LinkedList<Edge> edges;

	/** the centrality measures */
	private double centralityMeasure;

	/**
	 * Constructors
	 */
	public Node() {
		this.attributes 	= new Hashtable<>();
		this.edges 			= new LinkedList<>();
	}

	public Node(String id) {
		this.nodeID 		= id;
		this.attributes 	= new Hashtable<>();
		this.edges 			= new LinkedList<>();
	}

	/**
	 * generates a printable string of the node
	 */
	public String toString(){
		String node = "NodeID = "+this.nodeID +"\n";
		node += this.attributes;
		return node;
	}

	/**
	 * puts a new attribute (key,value pair)
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
	 * some getters and setters
	 */
	public Graph getGraph() {
		return graph;
	}

	public void setGraph(Graph graph) {
		this.graph = graph;
	}

	public String getNodeID() {
		return nodeID;
	}

	public void setNodeID(String nodeID) {
		this.nodeID = nodeID;
	}

	public LinkedList<Edge> getEdges() {
		return edges;
	}

	public void setEdges(LinkedList<Edge> edges) {
		this.edges = edges;
	}

	public void setCentrality(double score) {
		this.centralityMeasure = score;
	}

	public double getCentralityMeasure() {
		return this.centralityMeasure;
	}

	public Hashtable<String, GXLValue> getAttributes() {
		return attributes;
	}

	public void setAttributes(Hashtable<String, GXLValue> attributes) {
		this.attributes = attributes;
	}
}
