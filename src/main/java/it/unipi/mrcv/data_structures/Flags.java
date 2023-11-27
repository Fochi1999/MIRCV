package it.unipi.mrcv.data_structures;

public class Flags {

    private static boolean maxScore_flag;
    /* TRUE: BM25  FALSE: TFIDF */
    private static boolean scoreMode = true;

    /* TRUE: Conjunctive Query  FALSE: Disjunctive Query */
    private static boolean queryMode;
    public static void setMaxScore_flag(boolean maxScore_flag) {
        Flags.maxScore_flag = maxScore_flag;
    }
    public static void setScoreMode(boolean scoreMode) { Flags.scoreMode = scoreMode;}

    public static boolean isScoreMode() {return scoreMode;}
    public static boolean isMaxScore_flag() {
        return maxScore_flag;
    }

    public static boolean isQueryMode() {
        return queryMode;
    }
}
