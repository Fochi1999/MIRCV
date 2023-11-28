package it.unipi.mrcv.query_processing;

import it.unipi.mrcv.data_structures.Flags;
import it.unipi.mrcv.data_structures.PostingList;
import it.unipi.mrcv.query_processing.document_score.DocumentScore;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class QueryPreprocesser {
    public static ArrayList<PostingList> plQueryTerm = new ArrayList<>();
    public static ArrayList<PostingList> orderedPlQueryTerm = new ArrayList<>();
    public static HashMap<Integer, Double> hm_PosScore = new HashMap<>();
    public static ArrayList<Double> orderedMaxScore;
    public static HashMap<Integer, Integer> hm_PosLen = new HashMap<>();

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {

        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());
        Map<K, V> result = new LinkedHashMap<>();

        for (Map.Entry<K, V> entry : list)
            result.put(entry.getKey(), entry.getValue());

        return result;
    }


    public class executeQueryPreprocesser {

        private static ArrayList<PostingList> plQueryTerm = new ArrayList<>();
        private static ArrayList<PostingList> orderedPlQueryTerm = new ArrayList<>();
        private static HashMap<Integer, Double> hm_PosScore = new HashMap<>();
        private static ArrayList<Double> orderedMaxScore;
        private static HashMap<Integer, Integer> hm_PosLen = new HashMap<>();}}
        /**
         * Function that allows executing the processing of the query
         *
         * @param tokens the tokens of the query
         * @throws IOException if the channel is not found
         */
/*        public static void executeQueryProcesser(ArrayList<String> tokens, int k) throws IOException {
            int pos = 0;

            for (String t : tokens) {
                PostingList pl = new PostingList();
                pl.getPl().clear();
                pl.setTerm(t);
                pl.obtainPostingList(t);

                if (!pl.getPl().isEmpty()) {
                    plQueryTerm.add(pl);

                    if (Flags.isMaxScore_flag()) {
                        hm_PosScore.put(pos, Flags.isScoreMode() ? pl.getMaxBM25() : pl.getMaxTFIDF());
                    }

                    hm_PosLen.put(pos, pl.getPl().size());
                    pos++;
                }
            }

            if (plQueryTerm.isEmpty()) {
                System.out.println("(INFO) All the query words are not present in the Dictionary");
                return;
            }

            PriorityQueue<DocumentScore> pQueueResult = Flags.isQueryMode()
                    ? ConjunctiveQuery.executeConjunctiveQuery(k)
                    : (Flags.isMaxScore_flag() ? executeMaxScore(k) : executeDAAT(k));

            displayTopDocuments(pQueueResult, k);

            clearDataStructures();
        }

        private static void displayTopDocuments(PriorityQueue<DocumentScore> pQueueResult, int k) {
            int rank = 1;

            System.out.println("\n*** TOP " + k + " DOCUMENTS RETRIEVED ***\n");

            String leftAlignFormat = "\t| %-15d | %-4s |%n";

            System.out.format("\t+-----------------+-------+%n");
            System.out.format("\t|  Document       | Score |%n");
            System.out.format("\t+-----------------+-------+%n");

            while (!pQueueResult.isEmpty() && rank != (k + 1)) {
                DocumentScore d = pQueueResult.poll();
                if (d.getScore() != 0) {
                    System.out.format(leftAlignFormat, d.getDocid(), String.format("%.3f", d.getScore()));
                }
                rank++;
            }

            System.out.format("\t+-----------------+-------+%n");
        }

        private static void clearDataStructures() {
            orderedMaxScore = null;
            orderedPlQueryTerm = null;
            plQueryTerm = null;
            hm_PosScore = null;
            hm_PosLen = null;
        }

        private static PriorityQueue<DocumentScore> executeMaxScore(int k) {
            orderedPlQueryTerm = plQueryTerm.stream()
                    .sorted(Comparator.comparingDouble(pl -> hm_PosScore.get(plQueryTerm.indexOf(pl))))
                    .collect(Collectors.toCollection(ArrayList::new));

            orderedMaxScore = new ArrayList<>(hm_PosScore.values());

            return executeMaxScoreAlgorithm(k);
        }

        private static PriorityQueue<DocumentScore> executeMaxScoreAlgorithm(int k) {
            PriorityQueue<DocumentScore> maxHeap = new PriorityQueue<>(k, Collections.reverseOrder());

            // Implement the MaxScore algorithm here

            return maxHeap;
        }


    }

}
*/