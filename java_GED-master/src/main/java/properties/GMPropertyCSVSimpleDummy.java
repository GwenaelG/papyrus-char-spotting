package properties;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

/**
 * User: Michael Stauffer
 * Org.: University of Applied Sciences and Arts Northwestern Switzerland
 * Date: 23/06/15
 * Time: 14:32
 */
public class GMPropertyCSVSimpleDummy {

    private static int nrOfCores = 1;

    private static final String GROUND_TRUTH    = "test/papyrus/groundtruth.txt";
    private static final String MATCHING        = "HED";
    private static final String TREC_EVAL       = "treceval/trec_eval.exe";

    public static void main(String[] args) {

        String parameterFile        = "test/papyrus/settings/Parameters_dummy.csv";
        String graphFile            = "test/papyrus/settings/Graphs_dummy.csv";
        String resultPath           = "test/papyrus/results/dummy/";
        String propertyPath         = "test/papyrus/properties/dummy/";
        String sourceImagesPath     = "C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/files/original_bin/dummy/";
        String targetImagesPath     = "C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/files/original_bin/dummy/";
        String distVisFolder        = "C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/files/vis/hotmap/";
        String charVisFolder        = "C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/files/vis/dummy/";
        String[] windowSizes        = new String[]{"1"};
        String[] thresholdValues    = new String[]{"-2", "-1.8", "-1.6", "-1.4", "-1.2", "-1", "-0.8", "-0.6", "-0.4", "-0.2"};
        String nodeRatio            = "2";
        String iouRatio             = "0.9";

        readCSVFiles(parameterFile, graphFile, resultPath, propertyPath, sourceImagesPath, targetImagesPath, distVisFolder,
                charVisFolder, windowSizes, thresholdValues, nodeRatio, iouRatio);
    }

    private static void readCSVFiles(String parameterFile, String graphFile, String resultPath, String propertyPath,
                                     String sourceImagesPath, String targetImagesPath, String distVisFolder, String charVisFolder,
                                     String[] windowSizes, String[] thresholdValues, String nodeRatio, String iouRatio){

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

                String stepX            = getValue(parameterRecord, "Step X");
                String stepY            = getValue(parameterRecord, "Step Y");

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
                GMPropertyFile.addValue(propertyValues, "sourceImagesPath", sourceImagesPath, "");
                GMPropertyFile.addValue(propertyValues, "targetImagesPath", targetImagesPath, "");
                GMPropertyFile.addValue(propertyValues, "editDistVis", distVisFolder, "");
                GMPropertyFile.addValue(propertyValues, "charVisFolder", charVisFolder, "");

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

                GMPropertyFile.addValue(propertyValues, "numOfThresholdVal", Integer.toString(thresholdValues.length),"");
                for (int i = 0; i < thresholdValues.length; i++) {
                    GMPropertyFile.addValue(propertyValues, "threshold"+i, thresholdValues[i],"");
                }

                GMPropertyFile.addValue(propertyValues, "stepX", stepX, "5");
                GMPropertyFile.addValue(propertyValues, "stepY", stepY, "5");

                GMPropertyFile.addValue(propertyValues, "nodeRatio", nodeRatio,"10");

                GMPropertyFile.addValue(propertyValues, "iouRatio", iouRatio, "0.9");

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