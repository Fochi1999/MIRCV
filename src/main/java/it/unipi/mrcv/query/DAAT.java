package it.unipi.mrcv.query;

import it.unipi.mrcv.data_structures.*;
import it.unipi.mrcv.global.Global;
import it.unipi.mrcv.index.fileUtils;

import java.util.ArrayList;
import java.util.PriorityQueue;

import static it.unipi.mrcv.global.Global.docLengths;

public class DAAT {
    public static PriorityQueue<Document> executeDAAT(ArrayList<String> queryTerms, int k) {
        PriorityQueue<Document> incQueue = new PriorityQueue<>(new IncComparatorDocument());
        PriorityQueue<Document> decQueue = new PriorityQueue<>(new DecComparatorDocument());
        ArrayList<Document> documents = new ArrayList<>();

        ArrayList<PostingList> postingLists = new ArrayList<>();
        ArrayList<DictionaryElem> dictionaryElems = new ArrayList<>();
        ArrayList<Integer> currentPositions = new ArrayList<>();

        for (String term : queryTerms) {
            PostingList pl = new PostingList();
            DictionaryElem elem = fileUtils.binarySearchOnFile("vocabularyCompressed", term, pl);
            if (elem != null) {
                postingLists.add(pl);
                dictionaryElems.add(elem);
                currentPositions.add(0); // Initialize current position for each posting list
            }
        }

        if (postingLists.isEmpty()) {
            return null;
        }

        while (!postingLists.isEmpty()) {
            int minDocId = Integer.MAX_VALUE;
            for (int i = 0; i < postingLists.size(); i++) {
                Posting p = postingLists.get(i).getPostings().get(currentPositions.get(i));
                if (p.getDocid() < minDocId) {
                    minDocId = p.getDocid();
                }
            }

            Document d = new Document(minDocId, 0);

            for (int i = 0; i < postingLists.size(); i++) {
                PostingList pl = postingLists.get(i);
                int currentIndex = currentPositions.get(i);
                Posting p = pl.getPostings().get(currentIndex);
                if (p.getDocid() == minDocId) {
                    if (Global.isBM25) {
                        d.calculateScoreBM25(p.getFrequency(), dictionaryElems.get(i).getDf(), docLengths.get(minDocId));
                    } else {
                        d.calculateScoreTFIDF(dictionaryElems.get(i).getIdf(), p.getFrequency());
                    }

                    // Move to next posting in the list
                    currentPositions.set(i, currentIndex + 1);

                    // If the posting list is exhausted, remove it
                    if (currentPositions.get(i) >= pl.getPostings().size()) {
                        postingLists.remove(i);
                        dictionaryElems.remove(i);
                        currentPositions.remove(i);
                        i--; // Adjust index due to removal
                    }
                }
            }

            if (!documents.contains(d)) {
                if (documents.size() < k) {
                    documents.add(d);
                    incQueue.add(d);
                    decQueue.add(d);
                } else if (d.getScore() > incQueue.peek().getScore()) {
                    Document lowestScoreDoc = incQueue.poll();
                    decQueue.remove(lowestScoreDoc);
                    documents.remove(lowestScoreDoc);
                    documents.add(d);
                    incQueue.add(d);
                    decQueue.add(d);
                }
            }
        }

        return decQueue;
    }
}
