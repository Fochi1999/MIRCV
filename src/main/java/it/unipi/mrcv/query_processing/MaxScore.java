package it.unipi.mrcv.query_processing;
import it.unipi.mrcv.data_structures.CollectionInfo;
import it.unipi.mrcv.data_structures.PostingList;
import it.unipi.mrcv.query_processing.document_score.DocumentScore;
import it.unipi.mrcv.query_processing.document_score.IncComparatorScore;

import java.io.IOException;
import java.util.PriorityQueue;

import static it.unipi.mrcv.query_processing.QueryPreprocesser.plQueryTerm;

public class MaxScore {

        // Constants for readability
        private static final double BM25_K1 = 1.2;
        private static final double BM25_B = 0.75;

        public static PriorityQueue<DocumentScore> executeMaxScore(int k) throws IOException {
            // Min-heap to maintain the top-k documents with the smallest score
            PriorityQueue<DocumentScore> minHeap = new PriorityQueue<>(k, new IncComparatorScore());

            // Initialize term upper bounds and document upper bounds
            double[] termUpperBounds = new double[plQueryTerm.size()];
            long[] docUpperBounds = new long[plQueryTerm.size()];

            // Initialize current document ID
            long currentDocId = minimumDocID(termUpperBounds, docUpperBounds);

            // Loop until all documents are processed
            while (true) {
                double score = calculateScoreForDocument(plQueryTerm, currentDocId, termUpperBounds, docUpperBounds);

                updateMinHeap(minHeap, k, currentDocId, score);

                long nextDocId = minimumDocID(termUpperBounds, docUpperBounds);
                if (currentDocId == nextDocId)
                    break;

                currentDocId = nextDocId;
            }

            return minHeap;

        }
    private static long MinimumDocID(double[] termUpperBounds, long[] docUpperBounds) {
        // Initialize with a large value
        long minDocId = Long.MAX_VALUE;

        for (int i = 0; i < termUpperBounds.length; i++) {
            // Calculate the minimum of term and document upper bounds
            long minBound = (long) Math.min(termUpperBounds[i], docUpperBounds[i]);

            // Update the minimum document ID
            minDocId = Math.min(minDocId, minBound);
        }

        return minDocId;

        private static double calculateScoreForDocument(List<PostingList> plQueryTerm, long currentDocId,
                                                        double[] termUpperBounds, long[] docUpperBounds) {
            double score = 0;

            for (int i = 0; i < plQueryTerm.size(); i++) {
                PostingList postingList = plQueryTerm.get(i);

                if (postingList.getActualPosting() == null)
                    continue;

                if (postingList.getActualPosting().getDocID() == currentDocId) {
                    score += calculateTermScore(postingList);
                    postingList.nextPosting();
                }

                if (postingList.getActualPosting() == null)
                    continue;

                termUpperBounds[i] = calculateTermUpperBound(postingList, termUpperBounds[i]);
                docUpperBounds[i] = calculateDocUpperBound(postingList, docUpperBounds[i]);

                score += Math.min(termUpperBounds[i], docUpperBounds[i]);
            }

            return score;
        }

        private static double calculateTermScore(PostingList postingList) {
            if (Flags.isScoreMode())
                return Score.BM25(postingList.getTerm(), postingList.getActualPosting(), BM25_K1, BM25_B);
            else
                return Score.TFIDF(postingList.getTerm(), postingList.getActualPosting());
        }

        private static void updateMinHeap(PriorityQueue<DocumentScore> minHeap, int k, long currentDocId, double score) {
            if (minHeap.size() < k) {
                minHeap.add(new DocumentScore(currentDocId, score));
            } else {
                if (minHeap.peek().getScore() < score) {
                    minHeap.poll();
                    minHeap.add(new DocumentScore(currentDocId, score));
                }
            }
        }

        // Other methods remain the same...

    }




