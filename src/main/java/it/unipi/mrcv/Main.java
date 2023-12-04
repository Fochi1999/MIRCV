package it.unipi.mrcv;

import it.unipi.mrcv.compression.Unary;
import it.unipi.mrcv.compression.VariableByte;
import it.unipi.mrcv.data_structures.Document;
import it.unipi.mrcv.global.Global;
import it.unipi.mrcv.index.Merger;
import it.unipi.mrcv.index.SPIMI;
import it.unipi.mrcv.index.fileUtils;
import it.unipi.mrcv.query.DAAT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;


// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        /*SPIMI.exeSPIMI("collection.tsv");
        Merger.Merge();*/
        Global.load();
        ArrayList<String> query = new ArrayList<>();
        query.add("test");
        query.add("result");

        long startTime = System.currentTimeMillis(); // Capture start time


        PriorityQueue<Document> result = DAAT.executeDAAT(query, 10);
        for (int i = 0; i < 10; i++) {
            Document doc = result.poll();
            System.out.println(doc.getDocId() + " " + doc.getScore());
        }
        long endTime = System.currentTimeMillis(); // Capture end time



        // Calculate the elapsed time and convert it to minutes
        long elapsedTimeMillis = endTime - startTime;
        // Print the execution time in minutes
        System.out.printf("Execution time: "+ elapsedTimeMillis+" milliseconds\n");

    }
}

