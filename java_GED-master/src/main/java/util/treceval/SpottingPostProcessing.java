package util.treceval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Michael Stauffer
 * Org.: University of Applied Sciences and Arts Northwestern Switzerland
 * Date: 24.05.17
 * Time: 13:14
 */
public class SpottingPostProcessing {

    public ArrayList<SpottingResult> postProcess(ArrayList<SpottingResult> spottingResults){

        HashMap<String,HashMap<String,ArrayList<SpottingResult>>> keywordResultMap = new HashMap<>();

        // Sort spotting results...
        for(SpottingResult spottingResult : spottingResults) {

            String keywordClass = spottingResult.getKeywordClass();
            String keywordID    = spottingResult.getKeywordID();

            // ...by keyword class
            HashMap<String,ArrayList<SpottingResult>> keywordSpottingResults;

            if(!keywordResultMap.containsKey(keywordClass)){
                keywordSpottingResults = new HashMap<>();
                keywordResultMap.put(keywordClass,keywordSpottingResults);
            } else {
                keywordSpottingResults = keywordResultMap.get(keywordClass);
            }

            // ...by keyword ID
            ArrayList<SpottingResult> keywordIDSpottingResults;

            if(!keywordSpottingResults.containsKey(keywordID)){
                keywordIDSpottingResults = new ArrayList<>();
                keywordSpottingResults.put(keywordID,keywordIDSpottingResults);
            } else {
                keywordIDSpottingResults = keywordSpottingResults.get(keywordID);
            }

            keywordIDSpottingResults.add(spottingResult);
        }

        ArrayList<SpottingResult> reducedSpottingResults = new ArrayList<>();

        for(Map.Entry<String,HashMap<String,ArrayList<SpottingResult>>> keywordClassEntry : keywordResultMap.entrySet()){

            String keywordClass = keywordClassEntry.getKey();
            HashMap<String,ArrayList<SpottingResult>> keywordSpottingResults = keywordClassEntry.getValue();

            HashMap<String,ArrayList<SpottingResult>> wordSpottingResults = new HashMap<>();

            // Sort by word ID
            for(Map.Entry<String,ArrayList<SpottingResult>> keywordIDEntry : keywordSpottingResults.entrySet()){

                ArrayList<SpottingResult> keywordIDResults = keywordIDEntry.getValue();

                for(SpottingResult spottingResult : keywordIDResults){

                    String wordID = spottingResult.getWordID();
                    ArrayList<SpottingResult> wordClassSpottingResults;

                    if(!wordSpottingResults.containsKey(wordID)){
                        wordClassSpottingResults = new ArrayList<>();
                        wordSpottingResults.put(wordID,wordClassSpottingResults);
                    } else {
                        wordClassSpottingResults = wordSpottingResults.get(wordID);
                    }

                    wordClassSpottingResults.add(spottingResult);
                }
            }

            // Get best result per word ID
            for(Map.Entry<String,ArrayList<SpottingResult>> entry : wordSpottingResults.entrySet()){

                String wordID = entry.getKey();
                ArrayList<SpottingResult> wordIDSpottingResults = entry.getValue();

                SpottingResult bestSpottingResult = wordIDSpottingResults.stream().min((o1, o2) -> Double.compare(o1.getSpottingResult(),o2.getSpottingResult())).get();
                reducedSpottingResults.add(bestSpottingResult);
            }
        }

        return reducedSpottingResults;
    }
}
