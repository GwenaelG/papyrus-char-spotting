package gwenael;


import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;


public class GroundtruthPage {

	private String pageID;

	private ArrayList<GroundtruthLine> lines;

	public GroundtruthPage(String pageID){
		this.pageID = pageID;
		this.lines	= new ArrayList<>();
	}

	public String getPageID() {
		return pageID;
	}

	public void setPageID(String pageID) {
		this.pageID = pageID;
	}

	public ArrayList<GroundtruthLine> getLines() {
		return lines;
	}

	public void setLines(ArrayList<GroundtruthLine> lines) {
		this.lines = lines;
	}

	public void extractGroundtruthLines(Path textFilePath) throws IOException {
		// System.out.println("extract "+textFilePath.toString());
		if (Files.isRegularFile(textFilePath)) {
			File textFile = new File(String.valueOf(textFilePath));
			FileReader fr = new FileReader(textFile);
			BufferedReader br = new BufferedReader(fr);
			String textLine;
			while((textLine=br.readLine()) != null){
				String[] lineSplit = textLine.split(" ");
				// extract coords of line polygon
				String[] coords = lineSplit[0].split("\\|");
				int numPoints = coords.length;
				int[] xCoords = new int[numPoints];
				int[] yCoords = new int[numPoints];
				for (int i = 0; i < numPoints; i++) {
					xCoords[i] = Integer.parseInt(coords[i].split(",")[0]);
					yCoords[i] = Integer.parseInt(coords[i].split(",")[1]);
				}
				Polygon pol = new Polygon(xCoords, yCoords, numPoints);
				//extract line GT
				String[] groundtruthText = lineSplit[1].split(("\\|"));
				GroundtruthLine gtLine = new GroundtruthLine(pol, groundtruthText);
				lines.add(gtLine);
			}
		}
	}
}
