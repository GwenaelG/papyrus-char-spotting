package properties;

import jdk.nashorn.internal.ir.SwitchNode;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import properties.GMPropertyFile;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Michael Stauffer
 * Org.: University of Applied Sciences and Arts Northwestern Switzerland
 * Date: 23/06/15
 * Time: 14:32
 */
public class GMPropertyCSVSimple {

    private static int nrOfCores = 1;

    private static final String GROUND_TRUTH    = "test/papyrus/groundtruth.txt";
    private static final String MATCHING        = "HED";
    private static final String TREC_EVAL       = "treceval/trec_eval";

    public static void main(String[] args) {

        String parameterFile    = "test/papyrus/settings/Parameters.csv";
        String graphFile        = "test/papyrus/settings/Graphs.csv";
        String resultPath       = "test/papyrus/results/";
        String propertyPath     = "test/papyrus/properties/";
        String imagesPath       = "/Users/Gwenael/Desktop/MT/graphs-gwenael/papyrus/original_bin/pages/";
        String distVisPath      = "/Users/Gwenael/Desktop/MT/graphs-gwenael/papyrus/hotmap/";
        String[] windowSizes    = new String[]{"0.5", "0.75", "1", "1.5", "2"};
        String[] steepnessValues = new String[]{"0.1","0.25","0.5","1","2","5","10"};
        String[] thresholdValues = new String[]{"-4", "-3", "-2", "-1", "0"};

        readCSVFiles(parameterFile, graphFile, resultPath, propertyPath, imagesPath, distVisPath, windowSizes, steepnessValues, thresholdValues);
    }

    private static void readCSVFiles(String parameterFile, String graphFile, String resultPath, String propertyPath,
                                     String imagesPath, String distVisPath, String[] windowSizes, String[] steepnessValues,
                                     String[] thresholdValues){

        try {

            Reader graphReader                      = new FileReader(graphFile);
            Iterable<CSVRecord> graphRecords        = CSVFormat.EXCEL.withHeader().parse(graphReader);

            HashMap<String,String> graphSourcePaths       = new HashMap<>();
            HashMap<String,String> graphTargetPaths       = new HashMap<>();
            HashMap<String,String> sourcePaths            = new HashMap<>();
            HashMap<String,String> targetPaths            = new HashMap<>();
            HashMap<String,String> boundingBoxesPaths     = new HashMap<>();

            for(CSVRecord graphRecord : graphRecords){

                String graphType        = graphRecord.get("Graph Type");
                String graphID          = graphRecord.get("ID");

                String graphSourcePath        = graphRecord.get("Source Path");
                String graphTargetPath        = graphRecord.get("Target Path");
                String source       = getValue(graphRecord, "Source File");
                String target       = getValue(graphRecord, "Target File");
                String boundingBoxes = graphRecord.get("Bounding Boxes");

                String id               = graphType + "_" + graphID;

                graphSourcePaths.put(id, graphSourcePath);
                graphTargetPaths.put(id, graphTargetPath);

                if(source != null){
                    sourcePaths.put(id,source);
                }

                if(target != null){
                    targetPaths.put(id,target);
                }

                boundingBoxesPaths.put(id, boundingBoxes);

            }

            Reader parameterReader                  = new FileReader(parameterFile);
            Iterable<CSVRecord> parameterRecords    = CSVFormat.EXCEL.withHeader().parse(parameterReader);

            int coreNr = 1;

            for (CSVRecord parameterRecord : parameterRecords) {

                // Get values
                String testCase         = getValue(parameterRecord, "Test Case");

                String graphType        = getValue(parameterRecord, "Graph Type");
                String graphID          = getValue(parameterRecord, "ID");

                String id               = graphType + "_" + graphID;

                String graphSourcePath  = graphSourcePaths.get(id);
                String graphTargetPath  = graphTargetPaths.get(id);

                String sourceFile;
                if(sourcePaths.containsKey(id)){
                    sourceFile          = sourcePaths.get(id);
                } else {
                    sourceFile          = graphSourcePath + "source.cxl";
                }

                String targetFile;
                if(targetPaths.containsKey(id)){
                    targetFile          = targetPaths.get(id);
                } else {
                    targetFile          = graphSourcePath + "target.cxl";
                }

                String boundingBoxes = boundingBoxesPaths.get(id);

                String costType         = getValue(parameterRecord, "Cost Type");
                String nodeCost         = getValue(parameterRecord, "Node Cost");
                String edgeCost         = getValue(parameterRecord, "Edge Cost");
                String costNorm         = getValue(parameterRecord, "Cost Normalisation");

                String xImportance      = getValue(parameterRecord, "x");
                String yImportance      = getValue(parameterRecord, "y");

                String alphaImportance  = getValue(parameterRecord, "Alpha");
                String betaImportance   = getValue(parameterRecord, "Beta");

                // Convert beta to x- and y-importance
                if(betaImportance != null){

                    double beta = Double.valueOf(betaImportance);
                    double x = beta;
                    double y = 1 - beta;

                    xImportance = String.valueOf(x);
                    yImportance = String.valueOf(y);
                }

                String propertyFile     = propertyPath + "/" + "core" + coreNr + "/" + testCase + ".prop";
                String resultSubpath    = resultPath  + testCase + "/";

                // Add values
                HashMap<String,String> propertyValues = new HashMap<>();

                GMPropertyFile.addValue(propertyValues, "sourceFile", sourceFile, "");
                GMPropertyFile.addValue(propertyValues, "targetFile", targetFile, "");
                GMPropertyFile.addValue(propertyValues, "sourcePath", graphSourcePath, "");
                GMPropertyFile.addValue(propertyValues, "targetPath", graphTargetPath, "");
                GMPropertyFile.addValue(propertyValues, "result", resultSubpath, "");
                GMPropertyFile.addValue(propertyValues, "imagesPath", imagesPath, "");
                GMPropertyFile.addValue(propertyValues, "editDistVis", distVisPath, "");
                GMPropertyFile.addValue(propertyValues, "boundingBoxesFolder", boundingBoxes, "");


                GMPropertyFile.addValue(propertyValues, "matching", MATCHING, "HED");
                GMPropertyFile.addValue(propertyValues, "adj", "bestWithGreedy", "");

                GMPropertyFile.addValue(propertyValues, "costType", costType, "Euclidean Scaling");

                GMPropertyFile.addValue(propertyValues, "node", nodeCost, "1");
                GMPropertyFile.addValue(propertyValues, "edge", edgeCost, "1");

                GMPropertyFile.addValue(propertyValues, "normalisationFunction", costNorm, "N1");

                if(xImportance != null && yImportance != null ){

                    GMPropertyFile.addValue(propertyValues, "numOfNodeAttr", "2", "");
                    GMPropertyFile.addValue(propertyValues, "nodeAttr0", "x", "");
                    GMPropertyFile.addValue(propertyValues, "nodeAttr1", "y", "");

                    GMPropertyFile.addValue(propertyValues, "nodeAttr0Importance", xImportance, "1.0");
                    GMPropertyFile.addValue(propertyValues, "nodeAttr1Importance", yImportance, "1.0");

                } else {

                    GMPropertyFile.addValue(propertyValues, "numOfNodeAttr", "2", "");
                    GMPropertyFile.addValue(propertyValues, "nodeAttr0", "x", "");
                    GMPropertyFile.addValue(propertyValues, "nodeAttr1", "y", "");

                    GMPropertyFile.addValue(propertyValues, "nodeAttr0Importance", "1.0", "");
                    GMPropertyFile.addValue(propertyValues, "nodeAttr1Importance", "1.0", "");
                }


                GMPropertyFile.addValue(propertyValues, "multiplyNodeCosts", "0","");

                GMPropertyFile.addValue(propertyValues, "undirected", "1", "");

                GMPropertyFile.addValue(propertyValues, "numOfEdgeAttr", "0", "");
                GMPropertyFile.addValue(propertyValues, "multiplyEdgeCosts", "0","");

                GMPropertyFile.addValue(propertyValues, "alpha", alphaImportance, "0.5");

                GMPropertyFile.addValue(propertyValues, "outputGraphs", "0", "");
                GMPropertyFile.addValue(propertyValues, "outputEditPath", "0", "");
                GMPropertyFile.addValue(propertyValues, "outputCostMatrix", "0", "");
                GMPropertyFile.addValue(propertyValues, "outputMatching", "0", "");

                GMPropertyFile.addValue(propertyValues, "simKernel", "0", "");

                GMPropertyFile.addValue(propertyValues, "pNode", "1","");
                GMPropertyFile.addValue(propertyValues, "pEdge", "1","");

                GMPropertyFile.addValue(propertyValues, "groundtruth", GROUND_TRUTH, "");
                GMPropertyFile.addValue(propertyValues, "treceval", TREC_EVAL, "");

                GMPropertyFile.addValue(propertyValues, "numOfWindowSizes", Integer.toString(windowSizes.length),"");
                for (int i = 0; i < windowSizes.length; i++) {
                    GMPropertyFile.addValue(propertyValues, "windowSize"+i, windowSizes[i],"");
                }

               GMPropertyFile.addValue(propertyValues, "numOfSteepnessVal", Integer.toString(steepnessValues.length), "");
                for (int i = 0; i < steepnessValues.length; i++) {
                    GMPropertyFile.addValue(propertyValues, "steepnessVal"+i, steepnessValues[i], "");
                }

                GMPropertyFile.addValue(propertyValues, "numOfThresholdVal", Integer.toString(thresholdValues.length),"");
                for (int i = 0; i < thresholdValues.length; i++) {
                    GMPropertyFile.addValue(propertyValues, "threshold"+i, thresholdValues[i],"");
                }

                // Export values
                GMPropertyFile.export(propertyFile, propertyValues);

                coreNr++;

                if(coreNr > nrOfCores){
                    coreNr = 1;
                }
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getValue(CSVRecord csvRecord, String name){
        try{
            return csvRecord.get(name);
        } catch (Exception ex){
            return null;
        }
    }
}