package properties;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Michael Stauffer
 * Org.: University of Applied Sciences and Arts Northwestern Switzerland
 * Date: 23/06/15
 * Time: 14:32
 */
public class GMPropertyCSV {

    private static int nrOfCores = 1;

    private static final String GROUND_TRUTH    = "test/kws_experiment/gw/words.txt";
    private static final String MATCHING        = "HED";
    private static final String TREC_EVAL       = "treceval/trec_eval";

    public static void main(String[] args) {

        String parameterFile    = "test/kws_experiment/gw/settings/Parameters.csv";
        String graphFile        = "test/kws_experiment/gw/settings/Graphs.csv";
        String resultPath       = "test/kws_experiment/gw/results/";
        String propertyPath     = "test/kws_experiment/gw/properties/";

        readCSVFiles(parameterFile, graphFile, resultPath, propertyPath);
    }

    private static void readCSVFiles(String parameterFile, String graphFile, String resultPath, String propertyPath){

        try {

            Reader graphReader                      = new FileReader(graphFile);
            Iterable<CSVRecord> graphRecords        = CSVFormat.EXCEL.withHeader().parse(graphReader);

            HashMap<String,String> graphPaths       = new HashMap<>();
            HashMap<String,String> inputPaths       = new HashMap<>();
            HashMap<String,String> fastPaths        = new HashMap<>();
            HashMap<String,String> sourcePaths      = new HashMap<>();
            HashMap<String,String> targetPaths      = new HashMap<>();
            HashMap<String,String> dtwInputPaths    = new HashMap<>();
            HashMap<String,String> dtwEmbedPaths    = new HashMap<>();

            for(CSVRecord graphRecord : graphRecords){

                String graphType        = graphRecord.get("Graph Type");
                String graphID          = graphRecord.get("ID");

                String graphPath        = graphRecord.get("Path");
                String inputPath        = getValue(graphRecord, "Input");
                String fastPath         = getValue(graphRecord, "Fast");
                String sourcePath       = getValue(graphRecord, "Source");
                String targetPath       = getValue(graphRecord, "Target");
                String dtwInput         = getValue(graphRecord, "DTW Input");
                String dtwEmbed         = getValue(graphRecord, "DTW Embedding Path");

                String id               = graphType + "_" + graphID;

                graphPaths.put(id, graphPath);

                if(inputPath != null){
                    inputPaths.put(id,inputPath);
                }

                if(fastPath != null){
                    fastPaths.put(id,fastPath);
                }

                if(sourcePath != null){
                    sourcePaths.put(id,sourcePath);
                }

                if(targetPath != null){
                    targetPaths.put(id,targetPath);
                }

                if(dtwInput != null){
                    dtwInputPaths.put(id,dtwInput);
                }

                if(dtwEmbed != null){
                    dtwEmbedPaths.put(id,dtwEmbed);
                }
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

                String graphPath        = graphPaths.get(id);

                String inputFile;
                if(inputPaths.containsKey(id)){
                    inputFile           = inputPaths.get(id);
                } else {
                    inputFile           = "";
                }

                String fastRejectionFile;
                if(fastPaths.containsKey(id)){
                    fastRejectionFile   = fastPaths.get(id);
                } else {
                    fastRejectionFile   = "";
                }

                String sourceFile;
                if(sourcePaths.containsKey(id)){
                    sourceFile          = sourcePaths.get(id);
                } else {
                    sourceFile          = graphPath + "source.cxl";
                }

                String targetFile;
                if(targetPaths.containsKey(id)){
                    targetFile          = targetPaths.get(id);
                } else {
                    targetFile          = graphPath + "target.cxl";
                }

                String dtwInputFile;
                if(dtwInputPaths.containsKey(id)){
                    dtwInputFile        = dtwInputPaths.get(id);
                } else {
                    dtwInputFile        = "";
                }

                String dtwEmbedFile;
                if(dtwEmbedPaths.containsKey(id)){
                    dtwEmbedFile        = dtwEmbedPaths.get(id);
                } else {
                    dtwEmbedFile        = "";
                }

                String costType         = getValue(parameterRecord, "Cost Type");
                String treeLevelDev     = getValue(parameterRecord, "Tree Level Deviation");
                String costSteepness    = getValue(parameterRecord, "Steepness");
                String costThreshold    = getValue(parameterRecord, "Threshold");
                String costLThreshold   = getValue(parameterRecord, "Lower Threshold");
                String costUThreshold   = getValue(parameterRecord, "Upper Threshold");
                String nodeCost         = getValue(parameterRecord, "Node Cost");
                String edgeCost         = getValue(parameterRecord, "Edge Cost");
                String costNorm         = getValue(parameterRecord, "Cost Normalisation");
                String postNormSize     = getValue(parameterRecord, "Post Norm Size");
                String postNormType     = getValue(parameterRecord, "Post Norm Type");
                String postNormLB       = getValue(parameterRecord, "Post Norm Lower Bound");

                String xImportance      = getValue(parameterRecord, "x");
                String yImportance      = getValue(parameterRecord, "y");
                String gridImportance   = getValue(parameterRecord, "grid");

                String alphaImportance  = getValue(parameterRecord, "Alpha");
                String betaImportance   = getValue(parameterRecord, "Beta");

                String maxLevelStr      = getValue(parameterRecord, "Max Level");
                String normPolarDist    = getValue(parameterRecord, "Normalise Polar Distance");
                String polarThreshold   = getValue(parameterRecord, "Polar Threshold");

                int maxLevel = maxLevelStr != null ? Integer.valueOf(maxLevelStr) : 1;

                HashMap<String,String> levelU = new HashMap<>();
                HashMap<String,String> levelV = new HashMap<>();

                for(int i=0; i<=maxLevel; i++){

                    String u = getValue(parameterRecord,"Level"+i+"_U");
                    String v = getValue(parameterRecord,"Level"+i+"_V");

                    levelU.put("level"+i+"_U",u);
                    levelV.put("level"+i+"_V",v);
                }

//                String level0_U         = getValue(parameterRecord, "Level0_U");
//                String level0_V         = getValue(parameterRecord, "Level0_V");
//                String level1_U         = getValue(parameterRecord, "Level1_U");
//                String level1_V         = getValue(parameterRecord, "Level1_V");

                String contextRadius    = getValue(parameterRecord, "Context Radius");

                String qBPLevel         = getValue(parameterRecord, "QBP Level");
                String qBPBorderOL      = getValue(parameterRecord, "QBP Border Overlapping");

                String dtwWindowSize    = getValue(parameterRecord, "DTW Window Size");
                String dtwWindowOver    = getValue(parameterRecord, "DTW Window Overlapping");
                String dtwSakoeChiba    = getValue(parameterRecord, "DTW Sakoe Chiba");
                String dtwSaveResults   = getValue(parameterRecord, "DTW Save Results");

                String embedNumber      = getValue(parameterRecord, "DTW Embedding Number");

                String mcsNumOfClasses  = getValue(parameterRecord, "MCS Classes");
                String mcsType          = getValue(parameterRecord, "MCS Type");
                String mcsAlpha         = getValue(parameterRecord, "MCS Alpha");
                String mcsBeta          = getValue(parameterRecord, "MCS Beta");

                HashMap<String,String> mcsClasses = new HashMap<>();
                if(mcsNumOfClasses != null){

                    int numOfMCSClasses = Integer.valueOf(mcsNumOfClasses);

                    for(int i=0; i<numOfMCSClasses; i++){

                        String mcsClassName = "MCS Path "+i;
                        String mcsClassPath = getValue(parameterRecord, mcsClassName);

                        if(mcsClassPath != null){
                            mcsClasses.put("mcsPath"+i,mcsClassPath);
                        }
                    }
                }

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

                GMPropertyFile.addValue(propertyValues, "source", sourceFile, "");
                GMPropertyFile.addValue(propertyValues, "target", targetFile, "");
                GMPropertyFile.addValue(propertyValues, "path", graphPath, "");
                GMPropertyFile.addValue(propertyValues, "result", resultSubpath, "");
                GMPropertyFile.addValue(propertyValues, "input", inputFile, "");
                GMPropertyFile.addValue(propertyValues, "fast", fastRejectionFile, "");

                GMPropertyFile.addValue(propertyValues, "matching", MATCHING, "Hungarian");
                GMPropertyFile.addValue(propertyValues, "s", "", "");
                GMPropertyFile.addValue(propertyValues, "adj", "bestWithGreedy", "");

                GMPropertyFile.addValue(propertyValues, "costType", costType, "Euclidean");
                GMPropertyFile.addValue(propertyValues, "treeLevelDeviation", treeLevelDev, "0");
                GMPropertyFile.addValue(propertyValues, "steepness", costSteepness, "0");
                GMPropertyFile.addValue(propertyValues, "threshold", costThreshold, "0");
                GMPropertyFile.addValue(propertyValues, "lowerThreshold", costLThreshold, "0");
                GMPropertyFile.addValue(propertyValues, "upperThreshold", costUThreshold, "0");

                GMPropertyFile.addValue(propertyValues, "node", nodeCost, "1");
                GMPropertyFile.addValue(propertyValues, "edge", edgeCost, "1");

                GMPropertyFile.addValue(propertyValues, "normalisationFunction", costNorm, "N1");
                GMPropertyFile.addValue(propertyValues, "postNormSize", postNormSize, "-1");
                GMPropertyFile.addValue(propertyValues, "postNormType", postNormType, "");
                GMPropertyFile.addValue(propertyValues, "postNormLowerBound", postNormLB, "");

                if(xImportance != null && yImportance != null && gridImportance == null){

                    GMPropertyFile.addValue(propertyValues, "numOfNodeAttr", "2", "");
                    GMPropertyFile.addValue(propertyValues, "nodeAttr0", "x", "");
                    GMPropertyFile.addValue(propertyValues, "nodeAttr1", "y", "");

                    GMPropertyFile.addValue(propertyValues, "nodeAttr0Importance", xImportance, "1.0");
                    GMPropertyFile.addValue(propertyValues, "nodeAttr1Importance", yImportance, "1.0");
                }
                else if(xImportance != null && yImportance != null && gridImportance != null){

                    GMPropertyFile.addValue(propertyValues, "numOfNodeAttr", "3", "");
                    GMPropertyFile.addValue(propertyValues, "nodeAttr0", "x", "");
                    GMPropertyFile.addValue(propertyValues, "nodeAttr1", "y", "");
                    GMPropertyFile.addValue(propertyValues, "nodeAttr2", "grid", "");

                    GMPropertyFile.addValue(propertyValues, "nodeAttr0Importance", xImportance, "1.0");
                    GMPropertyFile.addValue(propertyValues, "nodeAttr1Importance", yImportance, "1.0");
                    GMPropertyFile.addValue(propertyValues, "nodeAttr2Importance", gridImportance, "1.0");
                }
                else {
                    GMPropertyFile.addValue(propertyValues, "numOfNodeAttr", "2", "");
                    GMPropertyFile.addValue(propertyValues, "nodeAttr0", "x", "");
                    GMPropertyFile.addValue(propertyValues, "nodeAttr1", "y", "");

                    GMPropertyFile.addValue(propertyValues, "nodeAttr0Importance", "1.0", "");
                    GMPropertyFile.addValue(propertyValues, "nodeAttr1Importance", "1.0", "");
                }

                GMPropertyFile.addValue(propertyValues, "undirected", "1", "");

                GMPropertyFile.addValue(propertyValues, "numOfEdgeAttr", "0", "");

                GMPropertyFile.addValue(propertyValues, "alpha", alphaImportance, "0.5");

                GMPropertyFile.addValue(propertyValues, "outputGraphs", "0", "");
                GMPropertyFile.addValue(propertyValues, "outputEditpath", "0", "");
                GMPropertyFile.addValue(propertyValues, "outputCostMatrix", "0", "");
                GMPropertyFile.addValue(propertyValues, "outputMatching", "0", "");

                GMPropertyFile.addValue(propertyValues, "simKernel", "0", "");

                GMPropertyFile.addValue(propertyValues, "groundtruth", GROUND_TRUTH, "");
                GMPropertyFile.addValue(propertyValues, "treceval", TREC_EVAL, "");

//                GMPropertyFile.addValue(propertyValues, "level0_U", level0_U, "");
//                GMPropertyFile.addValue(propertyValues, "level0_V", level0_V, "");
//                GMPropertyFile.addValue(propertyValues, "level1_U", level1_U, "");
//                GMPropertyFile.addValue(propertyValues, "level1_V", level1_V, "");

                GMPropertyFile.addValue(propertyValues, "maxLevel", maxLevelStr, "");
                GMPropertyFile.addValue(propertyValues, "normPolarDistance", normPolarDist, "");
                GMPropertyFile.addValue(propertyValues, "polarThreshold", polarThreshold, "");

                for(Map.Entry<String,String> entry : levelU.entrySet()){
                    GMPropertyFile.addValue(propertyValues, entry.getKey(), entry.getValue(), "");
                }

                for(Map.Entry<String,String> entry : levelV.entrySet()){
                    GMPropertyFile.addValue(propertyValues, entry.getKey(), entry.getValue(), "");
                }

                GMPropertyFile.addValue(propertyValues, "hedContextRadius", contextRadius, "1");

                GMPropertyFile.addValue(propertyValues, "qBPLevel", qBPLevel, "");
                GMPropertyFile.addValue(propertyValues, "qBPBorderOverlapping", qBPBorderOL, "");

                GMPropertyFile.addValue(propertyValues, "dtwWindowSize", dtwWindowSize, "");
                GMPropertyFile.addValue(propertyValues, "dtwWindowOverlapping", dtwWindowOver, "");
                GMPropertyFile.addValue(propertyValues, "dtwSakoeChibaBand", dtwSakoeChiba, "");
                GMPropertyFile.addValue(propertyValues, "dtwSaveResults", dtwSaveResults, "false");
                GMPropertyFile.addValue(propertyValues, "dtwResult", resultSubpath, "");
                GMPropertyFile.addValue(propertyValues, "dtwInput", dtwInputFile, "");

                GMPropertyFile.addValue(propertyValues, "dtwEmbeddingNrOfSubgraphs", embedNumber, "");
                GMPropertyFile.addValue(propertyValues, "dtwEmbeddingSubgraphPath", dtwEmbedFile, "");

                GMPropertyFile.addValue(propertyValues, "mcsNrOfClasses", mcsNumOfClasses, "");
                GMPropertyFile.addValue(propertyValues, "mcsType", mcsType, "");
                GMPropertyFile.addValue(propertyValues, "mcsAlpha", mcsAlpha, "");
                GMPropertyFile.addValue(propertyValues, "mcsBeta", mcsBeta, "");

                for(Map.Entry<String,String> entry : mcsClasses.entrySet()){
                    GMPropertyFile.addValue(propertyValues, entry.getKey(), entry.getValue(), "");
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