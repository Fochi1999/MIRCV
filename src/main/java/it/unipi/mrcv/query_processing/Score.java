package it.unipi.mrcv.query_processing;

import it.unipi.mrcv.data_structures.CollectionInfo;
import it.unipi.mrcv.data_structures.Posting;

public class Score {
    /**
     * Compute the BM25 score
     * @param term term that allows to access the dictionary element
     * @param p the posting to score
     * @param k parameter of BM25 algorithm
     * @param b  parameter of BM25 algorithm
     * @return a score
     */
    public static double BM25(String term, Posting p, double k, double b) {


        /* Retrieve term frequency inside the current docID */
        int tf = p.getFrequency();

        /* Retrieve the IDF */
        double idf = UploadDataStructures.Dictionary.get(term).getIdf();

        /* Compute average document length */
        double avdl = (double) CollectionInfo.getTotal_doc_len()/CollectionInfo.getDocid_counter();

        return (tf/(k*((1-b) + (b*(UploadDataStructures.Document_Index.get(p.getDocID()).getLength()/avdl))) + tf))*idf;
    }

    /**
     * Compute the TF-IDF score
     * @param term term that allows to access the dictionary element
     * @param p the posting to score
     * @return a score
     */
    public static double TFIDF(String term, Posting p) {

        /* Compute term frequency weight */
        double weight_tf = 1 + Math.log10(p.getFrequency());

        /* Retrieve IDF */
        double idf = UploadDataStructures.Dictionary.get(term).getIdf();

        return weight_tf*idf;
    }
}
