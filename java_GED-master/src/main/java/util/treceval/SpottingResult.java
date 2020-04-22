package util.treceval;

/**
 * User: Michael Stauffer
 * Org.: University of Applied Sciences and Arts Northwestern Switzerland
 * Date: 24.05.17
 * Time: 13:13
 */
public class SpottingResult {

    private String keywordID;
    private String keywordClass;

    private String wordID;
    private String wordClass;

    private double spottingResult;

    public SpottingResult(String keywordID, String keywordClass, String wordID, String wordClass, double spottingResult) {
        this.keywordID      = keywordID;
        this.keywordClass   = keywordClass;
        this.wordID         = wordID;
        this.wordClass      = wordClass;
        this.spottingResult = spottingResult;
    }

    public void setSpottingResult(double spottingResult){
        this.spottingResult = spottingResult;
    }

    public String getKeywordID() {
        return keywordID;
    }

    public String getKeywordClass() {
        return keywordClass;
    }

    public String getWordID() {
        return wordID;
    }

    public String getWordClass() {
        return wordClass;
    }

    public double getSpottingResult() {
        return spottingResult;
    }
}
