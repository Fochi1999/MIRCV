package it.unipi.mrcv.query_processing;
import it.unipi.mrcv.data_structures.CollectionInfo;
import it.unipi.mrcv.data_structures.PostingList;
import it.unipi.mrcv.query_processing.document_score.DocumentScore;
import it.unipi.mrcv.query_processing.document_score.IncComparatorScore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import static it.unipi.mrcv.query_processing.QueryPreprocesser.plQueryTerm;

public class MaxScore {

    private static final double BM25_K1 = 1.2;
    private static final double BM25_B = 0.75;

    /**
     * Executes the DAAT (Document-At-A-Time) algorithm to retrieve the top-k documents with the highest scores.
     *
     * @param k The number of top documents to retrieve.
     * @return A priority queue containing the top-k documents with the smallest score.
     * @throws IOException If an I/O exception occurs.
     */
    public static PriorityQueue<DocumentScore> executeMaxScore(int k) throws IOException {
        PriorityQueue<DocumentScore> minHeap = new PriorityQueue<>(k, new IncComparatorScore());
        List<PostingList> plQueryTerm = getPostingLists();  // Replace with actual method to get posting lists

        double[] termUpperBounds = new double[plQueryTerm.size()];
        long[] docUpperBounds = new long[plQueryTerm.size()];
        long currentDocId = calculateMinimumDocID(termUpperBounds, docUpperBounds, plQueryTerm);

        while (true) {
            double score = calculateScoreForDocument(plQueryTerm, currentDocId, termUpperBounds, docUpperBounds);
            updateMinHeap(minHeap, k, currentDocId, score);

            long nextDocId = calculateMinimumDocID(termUpperBounds, docUpperBounds, plQueryTerm);
            if (currentDocId == nextDocId)
                break;

            currentDocId = nextDocId;
        }

        return minHeap;
    }

    /**
     * Calculates the score for the given document ID from different posting lists.
     *
     * @param plQueryTerm      The list of posting lists.
     * @param currentDocId     The current document ID.
     * @param termUpperBounds  The array of term upper bounds.
     * @param docUpperBounds   The array of document upper bounds.
     * @return The calculated score for the document.
     */
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

    /**
     * Calculates the score for a term from the given posting list.
     *
     * @param postingList The posting list for a term.
     * @return The calculated term score.
     */
    private static double calculateTermScore(PostingList postingList) {
        return Flags.isScoreMode() ?
                Score.BM25(postingList.getTerm(), postingList.getActualPosting(), BM25_K1, BM25_B) :
                Score.TFIDF(postingList.getTerm(), postingList.getActualPosting());
    }

    /**
     * Calculates the minimum document ID considering term and document upper bounds.
     *
     * @param termUpperBounds The array of term upper bounds.
     * @param docUpperBounds  The array of document upper bounds.
     * @param plQueryTerm     The list of posting lists.
     * @return The minimum document ID.
     */
    private static long calculateMinimumDocID(double[] termUpperBounds, long[] docUpperBounds,
                                              List<PostingList> plQueryTerm) {
        long minDocId = CollectionInfo.getDocid_counter() + 1;

        for (int i = 0; i < plQueryTerm.size(); i++) {
            PostingList postingList = plQueryTerm.get(i);

            if (postingList.getActualPosting() != null) {
                long minBound = (long) Math.min(termUpperBounds[i], docUpperBounds[i]);
                minDocId = Math.min(minDocId, minBound);
            }
        }

        return minDocId;
    }

    /**
     * Updates the min-heap with the current document and score, ensuring it maintains the top-k documents with the smallest score.
     *
     * @param minHeap      The min-heap to be updated.
     * @param k            The number of top documents to retrieve.
     * @param currentDocId The current document ID.
     * @param score        The score of the current document.
     */
    private static void updateMinHeap(PriorityQueue<DocumentScore> minHeap, int k, long currentDocId, double score) {
        if (minHeap.size() < k) {
            minHeap.add(new DocumentScore(currentDocId, score));
        } else {
            assert minHeap.peek() != null;
            if (minHeap.peek().getScore() < score) {
                minHeap.poll();
                minHeap.add(new DocumentScore(currentDocId, score));
            }
        }
    }

    /**
     * Retrieves the posting lists for the query terms.
     * Replace this with the actual method to get posting lists.
     *
     * @return The list of posting lists.
     */
    private static List<PostingList> getPostingLists() {
        // Replace this with the actual method to get posting lists
        return new ArrayList<>();
    }

    // Other methods remain the same...

}



