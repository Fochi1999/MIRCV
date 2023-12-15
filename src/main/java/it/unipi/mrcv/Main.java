package it.unipi.mrcv;

import it.unipi.mrcv.compression.Unary;
import it.unipi.mrcv.compression.VariableByte;
import it.unipi.mrcv.data_structures.Document;
import it.unipi.mrcv.data_structures.PostingList;
import it.unipi.mrcv.global.Global;
import it.unipi.mrcv.index.Merger;
import it.unipi.mrcv.index.SPIMI;
import it.unipi.mrcv.index.fileUtils;
import it.unipi.mrcv.query.DAAT;
import it.unipi.mrcv.query.MaxScore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;


// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        /*Global.indexing = true;
        Global.load();
        SPIMI.exeSPIMI("collection.tsv");
        Global.load();
        Merger.Merge();
        System.out.printf("End");
        SPIMI.readDictionaryToFile(Global.finalVocCompressed,"voz.txt");*/
        Global.indexing = false;
        Global.load();
        ArrayList<String> query = new ArrayList<>();
        query.add("appl");
        query.add("cat");
        query.add("dog");
        query.add("eat");
        query.add("food");
        query.add("war");
        query.add("phone");
        query.add("econom");
        query.add("tree");
        query.add("president");


        long startTime = System.currentTimeMillis(); // Capture start time

        PriorityQueue<Document> queue = DAAT.executeDAAT(query, 10);

        long endTime = System.currentTimeMillis(); // Capture end time

        if (queue != null) {
            while (!queue.isEmpty()) {
                Document d = queue.poll();
                System.out.printf("Docid: %d - Score: %f\n", d.getDocId(), d.getScore());
            }
        }



        // Calculate the elapsed time and convert it to minutes
        long elapsedTimeMillis = endTime - startTime;
        // Print the execution time in minutes
        System.out.printf("Execution time: "+ elapsedTimeMillis+" milliseconds\n");




        long startTime2 = System.currentTimeMillis(); // Capture start time

        PriorityQueue<Document> queue2 = MaxScore.executeMaxScore(query, 10);

        long endTime2 = System.currentTimeMillis(); // Capture end time

        if (queue2 != null) {
            while (!queue2.isEmpty()) {
                Document d = queue2.poll();
                System.out.printf("Docid: %d - Score: %f\n", d.getDocId(), d.getScore());
            }
        }



        // Calculate the elapsed time and convert it to minutes
        long elapsedTimeMillis2 = endTime2 - startTime2;
        // Print the execution time in minutes
        System.out.printf("Execution time: "+ elapsedTimeMillis2+" milliseconds\n");

    }
}

