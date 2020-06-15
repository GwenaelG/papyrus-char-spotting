package xml;

import graph.Edge;
import graph.Graph;
import graph.GraphSet;
import graph.Node;
import net.sourceforge.gxl.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * User: Michael Stauffer
 * Org.: University of Applied Sciences and Arts Northwestern Switzerland
 * Date: 24.05.17
 * Time: 11:26
 */
public class GraphParser {

    public GraphSet parseCXL(Path cxlPath, Path gxlPath) throws Exception {

        GraphSet graphSet = new GraphSet();

        // Create DOM parser for CXL-file
        DocumentBuilderFactory dbf  = DocumentBuilderFactory.newInstance();
        DocumentBuilder db          = dbf.newDocumentBuilder();

        Document xmlDocument        = db.parse(cxlPath.toFile());

        NodeList childNodes         = xmlDocument.getDocumentElement().getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {

            org.w3c.dom.Node childNode = childNodes.item(i);

            if (childNode instanceof Element) {

                NodeList graphNodes = childNode.getChildNodes();

                for(int j=0; j< graphNodes.getLength(); j++){

                    org.w3c.dom.Node graphNode = graphNodes.item(j);

                    if(graphNode instanceof Element){

                        // Get attributes
                        String graphFile    = ((Element) graphNode).getAttribute("file");
                        String graphClass   = ((Element) graphNode).getAttribute("class");

                        // Parse GXL file
                        Graph graph = parseGXL(gxlPath.resolve(graphFile));
                        graph.setFileName(graphFile);
                        graph.setClassName(graphClass);

                        graphSet.add(graph);
                    }
                }
            }
        }

        return graphSet;

    }

    public Graph parseGXL(Path gxlPath) throws IOException, SAXException {

        InputStream inputStream = new FileInputStream(gxlPath.toFile());

        GXLDocument gxlDocument = new GXLDocument(inputStream);
        GXLGraph gxlGraph       = gxlDocument.getDocumentElement().getGraphAt(0);
        String graphID          = gxlGraph.getID();
        String edgeMode         = gxlGraph.getEdgeMode();

        // Create graph
        Graph graph             = new Graph();
        graph.setGraphID(graphID);

        if (edgeMode.equals("undirected")){
            graph.setDirected(false);
        } else {
            graph.setDirected(true);
        }

        // Add attributes
        for(int i=0; i<gxlGraph.getAttrCount(); i++){

            GXLAttr graphAttribute  = gxlGraph.getAttrAt(i);

            String attributeName    = graphAttribute.getName();
            GXLValue attributeValue = graphAttribute.getValue();

            graph.put(attributeName,attributeValue);
        }

        // Create nodes
        int nrOfNodes = 0;
        for(int i=0; i<gxlGraph.getChildCount(); i++){
            if(gxlGraph.getChildAt(i) instanceof GXLNode){

                GXLNode gxlNode = (GXLNode) gxlGraph.getChildAt(i);
                String nodeID   = gxlNode.getID();

                Node node       = new Node(nodeID);
                node.setGraph(graph);

                graph.add(node);

                // Add attributes
                for(int j=0; j<gxlNode.getAttrCount(); j++){

                    GXLAttr nodeAttribute = gxlNode.getAttrAt(j);

                    String attributeName    = nodeAttribute.getName();
                    GXLValue attributeValue = nodeAttribute.getValue();

                    node.put(attributeName,attributeValue);
                }

                nrOfNodes++;
            }
        }

        // Create edges
        Edge[][] edges = new Edge[nrOfNodes][nrOfNodes];
        graph.setAdjacencyMatrix(edges);

        for(int i=0; i<gxlGraph.getChildCount(); i++){
            if(gxlGraph.getChildAt(i) instanceof GXLEdge){

                GXLEdge gxlEdge = (GXLEdge) gxlGraph.getChildAt(i);
                String edgeID   = gxlEdge.getID();

                GXLNode gxlSourceNode   = (GXLNode) gxlEdge.getSource();
                String sourceNodeID     = gxlSourceNode.getID();

                GXLNode gxlTargetNode   = (GXLNode) gxlEdge.getTarget();
                String targetNodeID     = gxlTargetNode.getID();

                Edge edge = new Edge(edgeID);

                // Add attributes
                for(int j=0; j<gxlEdge.getAttrCount(); j++){

                    GXLAttr edgeAttribute   = gxlEdge.getAttrAt(j);

                    String attributeName    = edgeAttribute.getName();
                    GXLValue attributeValue = edgeAttribute.getValue();

                    edge.put(attributeName,attributeValue);
                }

                // Add edge to adjacency matrix
                for (int s = 0; s < graph.size(); s++){

                    Node sourceNode = graph.get(s);

                    if (sourceNode.getNodeID().equals(sourceNodeID)) {

                        edge.setStartNode(sourceNode);
                        sourceNode.getEdges().add(edge);

                        for (int t = 0; t < graph.size(); t++){

                            Node targetNode = graph.get(t);

                            if (targetNode.getNodeID().equals(targetNodeID)) {

                                edge.setEndNode(targetNode);
                                targetNode.getEdges().add(edge);

                                edges[s][t] = edge;
                                if (!graph.isDirected()){
                                    edges[t][s] = edge;
                                }
                            }
                        }
                    }
                }
            }
        }

        return graph;
    }
}
