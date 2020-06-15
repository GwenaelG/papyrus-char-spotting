package andreas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import graph.Edge;
import graph.Graph;
import graph.Node;

public class LetterPlotter {
	
	BufferedImage img;
	
	public LetterPlotter(Graph g1, Graph g2, EditPath editPath, int width, int height) throws Exception {
		Coordinates coord = new Coordinates(width, height);
		coord.updateExtrema(g1);
		coord.updateExtrema(g2);

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D draw = (Graphics2D) img.createGraphics();
        draw.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        draw.setPaint(Color.white);
        draw.fillRect(0, 0, width, height);

        drawGraph(g1, draw, coord, Color.green);
        drawGraph(g2, draw, coord, Color.red);
        drawEditPath(editPath, draw, coord, Color.black);
        drawIds(g1, draw, coord, Color.black, false);
        drawIds(g2, draw, coord, Color.black, true);
	}
	
	public void displayImage() {
		JFrame frame = new JFrame("VizLetter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new JLabel(new ImageIcon(img)));
        frame.pack();
        frame.setVisible(true);
	}
	
	public void saveImage(String file) {
		File outputfile = new File(file);
	    try {
			ImageIO.write(img, "gif", outputfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void drawGraph(Graph graph, Graphics2D draw, Coordinates coord, Color color) {
		draw.setStroke(new BasicStroke(coord.getS()));
		draw.setColor(color);
		draw.setPaint(color);
		Iterator<Node> iter = graph.iterator();
		while (iter.hasNext()) {
			Node n = iter.next();
			draw.fillOval(coord.getX(n)-coord.getR()/2, coord.getY(n)-coord.getR()/2, coord.getR(), coord.getR());
		}
		LinkedList<Edge> edges = new LinkedList<Edge>();
		Edge[][] adjacencyMatrix = graph.getAdjacencyMatrix();
		for (int i = 0; i < adjacencyMatrix.length; i++){
			for (int j = 0; j < adjacencyMatrix.length; j++){
				if (adjacencyMatrix[i][j] != null){
					edges.add(adjacencyMatrix[i][j]);
				}
			}
		}
		Iterator<Edge> iter2 = edges.iterator();
		while (iter2.hasNext()) {
			Edge e = (Edge) iter2.next();
			draw.drawLine(coord.getX(e.getStartNode()), coord.getY(e.getStartNode()), coord.getX(e.getEndNode()), coord.getY(e.getEndNode()));
		}
	}
	
	private void drawEditPath(EditPath editPath, Graphics2D draw, Coordinates coord, Color color) {
		draw.setStroke(new BasicStroke(coord.getS2()));
		draw.setColor(color);
		draw.setPaint(color);
		Iterator<EditOperation> iter = editPath.getSubstitutions().iterator();
		while (iter.hasNext()) {
			EditOperation eo = iter.next();
			drawArrow(draw, coord, coord.getX(eo.n2), coord.getY(eo.n2), coord.getX(eo.n1), coord.getY(eo.n1));
			if (eo.isSub()) {
				drawArrow(draw, coord, coord.getX(eo.n1), coord.getY(eo.n1), coord.getX(eo.n2), coord.getY(eo.n2));
			}
		}
		iter = editPath.getDeletions().iterator();
		while (iter.hasNext()) {
			EditOperation eo = iter.next();
			draw.drawOval(coord.getX(eo.n1)-coord.getR()/2, coord.getY(eo.n1)-coord.getR()/2, coord.getR(), coord.getR());
		}
		iter = editPath.getInsertions().iterator();
		while (iter.hasNext()) {
			EditOperation eo = iter.next();
			draw.drawOval(coord.getX(eo.n1)-coord.getR()/2, coord.getY(eo.n1)-coord.getR()/2, coord.getR(), coord.getR());
		}
	}
	
	private void drawIds(Graph graph, Graphics2D draw, Coordinates coord, Color color, boolean mapped) {
		draw.setFont(new Font("Arial",Font.BOLD, coord.getF()));
		draw.setColor(color);
		Iterator<Node> iter = graph.iterator();
		while (iter.hasNext()) {
			Node n = (Node) iter.next();
			String str = n.getNodeID().substring(1);
			if (mapped) {
				str += "'";
			}
		    draw.drawString(str, coord.getFx(n), coord.getFy(n));
		}
	}
	
	private void drawArrow(Graphics2D draw, Coordinates coord, int tipX0, int tipY0, int tailX0, int tailY0) {
		// shorten tip and tail
		double dy0 = tipY0 - tailY0;  
        double dx0 = tipX0 - tailX0;
		double len0 = Math.sqrt(Math.pow(dx0,2) + Math.pow(dy0,2));
        double frac0 = coord.getR() * 0.4 / len0;
        int tipX = (int)Math.round(tailX0+dx0*(1-frac0));
        int tipY = (int)Math.round(tailY0+dy0*(1-frac0));
        int tailX = (int)Math.round(tailX0+dx0*frac0);
        int tailY = (int)Math.round(tailY0+dy0*frac0);
		// setup arrow
		double phi = Math.toRadians(15);  
        int barb = (int)Math.round(coord.getR() * 0.4);
        double dy = tipY - tailY;  
        double dx = tipX - tailX;
        double theta = Math.atan2(dy, dx);
        // draw
        draw.drawLine(tipX, tipY, tailX, tailY);
        int x, y;
        double rho = theta + phi;
        for(int j = 0; j < 2; j++) {  
            x = (int)Math.round(tipX - barb * Math.cos(rho));  
            y = (int)Math.round(tipY - barb * Math.sin(rho));  
            draw.drawLine(tipX, tipY, x, y);  
            rho = theta - phi;  
        }
    }
	
	public class Coordinates {
		
		private int border;
		private int vizwidth;
		private int vizheight;
		private Float xmin;
		private Float xmax;
		private Float ymin;
		private Float ymax;
		
		public Coordinates(int width, int height) {
			border = (int) Math.round(width * 0.1);
			vizwidth = width-2*border;
			vizheight = height-2*border;
		}

		public void updateExtrema(Graph g) {
			Iterator<Node> iter = g.iterator();
			while (iter.hasNext()){
				Node n = (Node) iter.next();
				float x = n.getFloat("x");
				float y = n.getFloat("y");
				if (xmin == null || x < xmin) xmin = x;
				if (xmax == null || x > xmax) xmax = x;
				if (ymin == null || y < ymin) ymin = y;
				if (ymax == null || y > ymax) ymax = y;
			}
		}
		
		public int getX(Node n) {
			float x = n.getFloat("x");
			int xbar = border + Math.round(vizwidth * (x-xmin) / (xmax-xmin));
			return xbar;
		}
		
		public int getY(Node n) {
			float y = n.getFloat("y");
			int ybar = border + Math.round(vizheight * (ymax-y) / (ymax-ymin));
			return ybar;
		}
		
		public int getR() {
			return border/2;
		}
		
		public int getF() {
			return (int)Math.round(border/3.0);
		}
		
		public int getFx(Node n) {
			return getX(n) - (int)Math.round(border/9.0);
		}
		
		public int getFy(Node n) {
			return getY(n) + (int)Math.round(border/9.0);
		}
		
		public int getS() {
			return border/10;
		}
		
		public float getS2() {
			return border/20;
		}
		
	}
    
}
