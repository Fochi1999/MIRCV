package it.unipi.mrcv.data_structures;

import static it.unipi.mrcv.global.Global.averageDocLength;

public class Document {
    int docId;
    double score;

    public Document(int docId, double score) {
            this.docId = docId;
            this.score = score;
        }

    public int getDocId() {
        return docId;
    }
    public void calculateScoreBM25(double idf, int tf, int docLength) {
        score += idf * (tf * (1.0 + 1.2) / (tf + 1.2 * (0.25 + 0.75 * docLength / averageDocLength))); //TODO copy from dictionaryElem
    }
    public void calculateScoreTFIDF(double idf, int tf) {

        score += idf * (1 + Math.log10(tf));
    }

    public double getScore() {
        return score;
    }
}
