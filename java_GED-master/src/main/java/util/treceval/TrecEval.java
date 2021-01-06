package util.treceval;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * User: Michael Stauffer
 * Org.: University of Applied Sciences and Arts Northwestern Switzerland
 * Date: 24.05.17
 * Time: 13:14
 */
public class TrecEval {

    private Path trecEvalFile;
    private Path resultPath;
    private int clusterTestCase;

    private TreeMap<String, String> wordList = new TreeMap<>();

    public TrecEval(Path resultPath, Path trecEvalFile, int clusterTestCase){
        this.resultPath         = resultPath;
        this.trecEvalFile       = trecEvalFile;
        this.clusterTestCase    = clusterTestCase;
    }

    public void exportSpottingResults(AbstractList<SpottingResult> spottingResults){

        try {
            // Create relevance file
            Path localRelevanceFile = resultPath.resolve("local_relevance");
            List<String> localRelevanceOutput = new ArrayList<>();

            Path globalRelevanceFile = resultPath.resolve("global_relevance");
            List<String> globalRelevanceOutput = new ArrayList<>();

            // Create top file
            Path localTopFile = resultPath.resolve("local_top");
            List<String> localTopOutput = new ArrayList<>();

            Path globalTopFile = resultPath.resolve("global_top");
            List<String> globalTopOutput = new ArrayList<>();

            // Keywords file
            Path keywordsFile = resultPath.resolve("keywords");

            HashMap<String,String> keywordNames = new HashMap<>();
            List<String> keywordOutput = new ArrayList<>();

            for(SpottingResult spottingResult : spottingResults){

                String keywordID    = spottingResult.getKeywordID();
                String keywordClass = spottingResult.getKeywordClass();

                String keywordName;
                // Regular usage
                if(clusterTestCase == -1) {
                    if (!keywordNames.containsKey(keywordClass)) {
                        keywordName = "kw" + (keywordNames.size() + 1);

                        keywordOutput.add(keywordName + " " + keywordClass);
                        keywordNames.put(keywordClass, keywordName);
                    } else {
                        keywordName = keywordNames.get(keywordClass);
                    }
                }
                // Cluster usage
                else {
                    if (!keywordNames.containsKey(keywordClass)) {
                        keywordName = "kw_" + clusterTestCase + "_" + (keywordNames.size() + 1);

                        keywordOutput.add(keywordName + " " + keywordClass);
                        keywordNames.put(keywordClass, keywordName);
                    } else {
                        keywordName = keywordNames.get(keywordClass);
                    }
                }

                String wordID       = spottingResult.getWordID();
                String wordClass    = spottingResult.getWordClass();

                double score        = spottingResult.getSpottingResult() * -1;

                if(keywordClass.equals(wordClass)){
                    localRelevanceOutput.add(keywordName + " 0 " + wordID + " 1");
                    globalRelevanceOutput.add("key 0 " + keywordName + "_" + wordID + " 1");
                } else {
                    localRelevanceOutput.add(keywordName + " 0 " + wordID + " 0");
                    globalRelevanceOutput.add("key 0 " + keywordName + "_" + wordID + " 0");
                }

                localTopOutput.add(keywordName + " Q0 " + wordID + " 0 " + score + " kws");
                globalTopOutput.add("key Q0 " + keywordName + "_" + wordID + " 0 " + score + " kws");

            }

            Files.write(keywordsFile, keywordOutput, StandardCharsets.UTF_8);

            Path localRelevanceFilePath = Files.write(localRelevanceFile, localRelevanceOutput, StandardCharsets.UTF_8);
            Path globalRelevanceFilePath = Files.write(globalRelevanceFile, globalRelevanceOutput, StandardCharsets.UTF_8);

            Path localTopFilePath = Files.write(localTopFile, localTopOutput, StandardCharsets.UTF_8);
            Path globalTopFilePath = Files.write(globalTopFile, globalTopOutput, StandardCharsets.UTF_8);

            // Evaluate via trec_eval
            Path localTrecFilePath = this.resultPath.resolve("trec_local");
            Path globalTrecFilePath = this.resultPath.resolve("trec_global");

            String trecLocal =
                    getPath(trecEvalFile.toFile().getAbsolutePath()) + " -q "
                            + getPath(localRelevanceFilePath.toAbsolutePath().toString()) + " "
                            + getPath(localTopFilePath.toAbsolutePath().toString()) + " > "
                            + getPath(localTrecFilePath.toAbsolutePath().toString());

            String trecGlobal =
                    getPath(trecEvalFile.toFile().getAbsolutePath()) + " -q "
                            + getPath(globalRelevanceFilePath.toAbsolutePath().toString()) + " "
                            + getPath(globalTopFilePath.toAbsolutePath().toString()) + " > "
                            + getPath(globalTrecFilePath.toAbsolutePath().toString());

            this.executeCommand(trecLocal);
            this.executeCommand(trecGlobal);

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private String getPath(String path){
        return "\"" + path + "\"";
    }

    private void executeCommand(String cmd) throws IOException {
        // Windows version
        String[] commandAndArgs = new String[]{ "powershell", "-command", cmd };

        //original version
//      String[] commandAndArgs = new String[]{ "/bin/sh", "-c", cmd };

        Runtime.getRuntime().exec(commandAndArgs);
    }
}
