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

import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;


// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        /*fileUtils.deleteFiles();
        fileUtils.deleteFilesCompressed();
        fileUtils.deleteTempFiles();
        Global.indexing = true;
        Global.load();
        SPIMI.exeSPIMI("collection.tsv");
        Global.load();
        Merger.Merge();
        Global.indexing = false;*/
        Global.load();
        PostingList pl=new PostingList();


        long startTime = System.currentTimeMillis(); // Capture start time
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
        PriorityQueue<Document> queue =
        DAAT.executeDAAT(query, 10);
        if (queue != null) {
            while (!queue.isEmpty()) {
                Document d = queue.poll();
                System.out.printf("Docid: %d - Score: %f\n", d.getDocId(), d.getScore());
            }
        }
        long endTime = System.currentTimeMillis(); // Capture end time



        // Calculate the elapsed time and convert it to minutes
        long elapsedTimeMillis = endTime - startTime;
        // Print the execution time in minutes
        System.out.printf("Execution time: "+ elapsedTimeMillis+" milliseconds\n");

    }
}

