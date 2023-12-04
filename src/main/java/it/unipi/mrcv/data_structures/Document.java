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
        double k1 = 1.2;
        double b = 0.75;
        double numerator = tf * (k1 + 1);
        double denominator = tf + k1 * (1 - b + b * (docLength / averageDocLength));
        this.score += idf * numerator / denominator;
    }

    public void calculateScoreTFIDF(double idf, int tf) {

        this.score += idf * (1 + Math.log10(tf));
    }

    public double getScore() {
        return score;
    }


}
