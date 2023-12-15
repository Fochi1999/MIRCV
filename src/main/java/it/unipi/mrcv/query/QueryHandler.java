package it.unipi.mrcv.query;

import it.unipi.mrcv.data_structures.Document;
import it.unipi.mrcv.preprocess.preprocess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class QueryHandler {
    //method that receives a string with a query, execute the preprocessing and returns the list of terms
    public static void query(String query,String method,int k) {
        ArrayList<String> queryTerms = preprocess.all(query);
        PriorityQueue<Document> results = new PriorityQueue<>();
        //execute the query
        if(method.equals("DAAT")) {
            try {
                results=DAAT.executeDAAT(queryTerms, k);
            } catch (IOException e) {
                System.out.println("Error in DAAT");
            }
        }
        if(method.equals("MaxScore")) {
            try {
                results=MaxScore.executeMaxScore(queryTerms, k);
            } catch (IOException e) {
                System.out.println("Error in MaxScore");
            }
        }
        //print the results
        if (results != null) {
            while (!results.isEmpty()) {
                Document d = results.poll();
                System.out.printf("Docid: %d - Score: %f\n", d.getDocId(), d.getScore());
            }
        }
    }
}
