package it.unipi.mrcv.query;

import it.unipi.mrcv.data_structures.Document;
import it.unipi.mrcv.global.Global;
import it.unipi.mrcv.preprocess.preprocess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.PriorityQueue;


public class QueryTester {
    // Method used to produce the results for the TREC 2020 competition in order to compute trec_eval metrics
    public static void processQueries(String inputFilePath, String outputFilePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             PrintWriter writer = new PrintWriter(outputFilePath)) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+", 2);
                if (parts.length < 2) {
                    continue; // Skip lines that don't have both parts
                }
                String queryId = parts[0];
                String query = parts[1];

                PriorityQueue<Document> queue = DAAT.executeDAAT(preprocess.all(query), 20);
                int position = 1;
                while (!queue.isEmpty()) {
                    Document doc = queue.poll();
                    writer.println(queryId + " Q0 " + doc.getDocId() + " " + position + " 0.0 0");
                    position++;
                }
            }
        }
    }

    // Method used to analyze the query time for MaxScore and DAAT using the TREC 2020 queries
    public static void analyzeQueryTime(String inputFilePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            String line;

            long totalDAATTime = 0;
            long totalMaxScoreTime = 0;
            int queryCount = 0;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+", 2);
                if (parts.length < 2) {
                    continue; // Skip lines that don't have both parts
                }
                String queryId = parts[0];
                String query = parts[1];

                // MaxSCORE
                long startTime = System.currentTimeMillis();
                PriorityQueue<Document> queue = MaxScore.executeMaxScore(preprocess.all(query), 20);
                while (!queue.isEmpty()) {
                    Document doc = queue.poll();
                    System.out.println("MaxSCORE: " + doc.getDocId() + " " + doc.getScore());
                }
                long endTime = System.currentTimeMillis();
                long maxScoreTime = endTime - startTime;
                totalMaxScoreTime += maxScoreTime;
                System.out.println("MaxScore time for " + queryId + ": " + maxScoreTime + " ms");

                // DAAT
                startTime = System.currentTimeMillis();
                queue = DAAT.executeDAAT(preprocess.all(query), 20);
                while (!queue.isEmpty()) {
                    Document doc = queue.poll();
                    System.out.println("DAAT: " + doc.getDocId() + " " + doc.getScore());
                }
                endTime = System.currentTimeMillis();
                long daatTime = endTime - startTime;
                totalDAATTime += daatTime;
                System.out.println("DAAT time for " + queryId + ": " + daatTime + " ms");

                queryCount++;
            }

            if (queryCount > 0) {
                System.out.println("Average MaxScore Time: " + (totalMaxScoreTime / queryCount) + " ms");
                System.out.println("Average DAAT Time: " + (totalDAATTime / queryCount) + " ms");
            }
        }
    }



    public static void main(String[] args) {
        Global.indexing = false;
        Global.load();

        // uncomment to produce the results file for the TREC 2020 competition
/*        try {
            processQueries("msmarco-test2020-queries.tsv", "2020queryResults.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        // uncomment to analyze the query time for MaxScore and DAAT using the TREC 2020 queries
        try {
            analyzeQueryTime("msmarco-test2020-queries.tsv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
