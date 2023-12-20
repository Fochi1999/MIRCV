package it.unipi.mrcv.Test;

import it.unipi.mrcv.data_structures.DictionaryElem;
import it.unipi.mrcv.data_structures.Document;
import it.unipi.mrcv.data_structures.PostingList;
import it.unipi.mrcv.global.Global;
import it.unipi.mrcv.preprocess.preprocess;
import it.unipi.mrcv.query.ConjunctiveQuery;
import it.unipi.mrcv.query.DAAT;
import it.unipi.mrcv.query.MaxScore;

import javax.print.Doc;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class QueryTest {
    // static method that reads x documents (the docids are the input), submit it as a query and check if it's present in the results
    public static void test(ArrayList<Integer> docIds) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("collection.tsv"), StandardCharsets.UTF_8));
            String line = reader.readLine();
            int docId = 0;
            PriorityQueue<Document> daatResults = new PriorityQueue<>();
            PriorityQueue<Document> maxScoreResults = new PriorityQueue<>();
            PriorityQueue<Document> conjunctiveResults = new PriorityQueue<>();
            int numOfDocs = 10;
            while (line != null) {
                if (docIds.indexOf(docId) != -1) {
                    String[] parts = line.split("\t", 2);
                    ArrayList<String> tokens = preprocess.all(parts[1]);
                    daatResults = DAAT.executeDAAT(tokens, numOfDocs);
                    maxScoreResults = MaxScore.executeMaxScore(tokens, numOfDocs);
                    conjunctiveResults = ConjunctiveQuery.executeConjunctiveQuery(tokens, numOfDocs);
                    assert docIdInResults(daatResults, docId) : "DAAT: docId " + docId + " not in results";
                    assert docIdInResults(maxScoreResults, docId) : "MaxScore: docId " + docId + " not in results";
                    assert docIdInResults(conjunctiveResults, docId) : "Conjunctive: docId " + docId + " not in results";
                }
                docId++;
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean docIdInResults(PriorityQueue<Document> results, int docId) {
        for (Document doc : results) {
            if (doc.getDocId() == docId) {
                return true;
            }
        }
        return false;
    }
    public static void main(String[] args) throws IOException {
        Global.load();
        ArrayList<Integer> docIds = new ArrayList<>();
        //add random docIds
        for (int i = 0; i < 100; i++) {
            docIds.add((int) (Math.random() * 100000));
        }
        QueryTest.test(docIds);
        System.out.println("Test passed");
    }
}
