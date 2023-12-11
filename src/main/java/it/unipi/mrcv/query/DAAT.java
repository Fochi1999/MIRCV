package it.unipi.mrcv.query;

import it.unipi.mrcv.data_structures.*;
import it.unipi.mrcv.data_structures.Comparators.DecComparatorDocument;
import it.unipi.mrcv.data_structures.Comparators.IncComparatorDocument;
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
        ArrayList<Integer> blocksNumber = new ArrayList<>();

        for (String term : queryTerms) {
            PostingList pl = new PostingList();
            DictionaryElem elem = fileUtils.binarySearchOnDictionaryBlock( term, pl,0);
            if (elem != null) {
                postingLists.add(pl);
                dictionaryElems.add(elem);
                currentPositions.add(0); // Initialize current position for each posting list
                blocksNumber.add(0);
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

                    // If the posting list is exhausted, check if there are other blocks to read
                    if (currentPositions.get(i) >= pl.getPostings().size()) {
                        blocksNumber.set(i, blocksNumber.get(i) + 1);
                        if (blocksNumber.get(i)<dictionaryElems.get(i).getSkipLen()) //there is another block to read
                        {
                            PostingList pl2 = new PostingList();
                            DictionaryElem elem = fileUtils.binarySearchOnDictionaryBlock( dictionaryElems.get(i).getTerm(), pl2,blocksNumber.get(i));
                            if (elem != null) {
                                postingLists.set(i, pl2);
                                currentPositions.set(i, 0); // Initialize current position for each posting list
                            }
                        }
                        else //no more blocks to read
                        {
                            postingLists.remove(i);
                            dictionaryElems.remove(i);
                            currentPositions.remove(i);
                            i--; // Adjust index due to removal
                        }

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
