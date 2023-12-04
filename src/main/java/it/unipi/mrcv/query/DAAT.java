package it.unipi.mrcv.query;

import it.unipi.mrcv.data_structures.*;
import it.unipi.mrcv.global.Global;
import it.unipi.mrcv.index.fileUtils;

import java.util.ArrayList;
import java.util.PriorityQueue;

import static it.unipi.mrcv.global.Global.docLengths;

public class DAAT {
    public static PriorityQueue<Document> executeDAAT(ArrayList<String> queryTerms, int k) {
        // Create a priority queue of documents; this will be used to order documents from lowest to highest score
        PriorityQueue<Document> incQueue = new PriorityQueue<>(new IncComparatorDocument());
        // Create a priority queue of documents; this will be used to order documents from highest to lowest score and will be the output
        PriorityQueue<Document> decQueue = new PriorityQueue<>(new DecComparatorDocument());
        ArrayList<Document> documents = new ArrayList<>();
        // Create a list of dictionary elements

        // Create a list of posting lists; this will be used to iterate through the posting lists of each query term
        ArrayList<PostingList> postingLists = new ArrayList<>();
        // Create a list of documents; this list is connected to the priority queues
        ArrayList<DictionaryElem> dictionaryElems = new ArrayList<>();


        // For each query term, get the posting list and add it to the list of posting lists
        for (String term : queryTerms) {
            // get the dictionaryElem of the term using the binary search and set the posting list
            PostingList pl = new PostingList();
            DictionaryElem elem = fileUtils.binarySearchOnFile("vocabularyCompressed", term, pl);
            // If the term is not in the dictionary, skip it
            if (elem == null) {
                continue;
            }
            // If the term is in the dictionary, add the posting list to the list of posting lists
            postingLists.add(pl);
            // Add the dictionary element to the list of dictionary elements
            dictionaryElems.add(elem);
        }

        // If the list of posting lists is empty, return null
        if (postingLists.isEmpty()) {
            return null;
        }


        while (true) {
            // For each posting list get the first posting and find the minimum docId among all the first postings
            int minDocId = Integer.MAX_VALUE;
            for (PostingList pl : postingLists) {
                Posting p = pl.getPostings().get(0);
                if (p.getDocid() < minDocId) {
                    minDocId = p.getDocid();
                }
            }

            // Create a new document; its score will be computed, and it will be added to the priority queue
            Document d = new Document(minDocId, 0);

            // For each term that has the minimum docId, calculate the score and add the document to the priority queue
            for (int i = 0; i < postingLists.size(); i++) {
                PostingList pl = postingLists.get(i);
                Posting p = pl.getPostings().get(0);
                if (p.getDocid() == minDocId) {
                    // Check the flag to see if we are using BM25 or TF-IDF
                    if (Global.isBM25) {
                        d.calculateScoreBM25(p.getFrequency(), dictionaryElems.get(i).getDf(), docLengths.get(minDocId));
                    } else {
                        // Calculate the score using TF-IDF
                        d.calculateScoreTFIDF(dictionaryElems.get(i).getIdf(), p.getFrequency());
                    }

                    // Remove the first posting from the posting list
                    pl.getPostings().remove(0);
                    // If the posting list is empty, remove it from the list of posting lists
                    if (pl.getPostings().isEmpty()) {
                        postingLists.remove(pl);
                    }

                    // if document list size is less than k, add the document to the list of documents and to the queues
                    if (documents.size() < k) {
                        documents.add(d);
                        incQueue.add(d);
                        decQueue.add(d);
                    }
                    // if document list size is equal to k, compare its score with the lowest score in the queue
                    else {
                        // if the score is higher, remove the lowest score from the queue and add the document
                        if (d.getScore() > incQueue.peek().getScore()) {
                            Document q = incQueue.poll();
                            decQueue.remove(q);
                            documents.remove(q);
                            // add the document to the list of documents and to the queues
                            documents.add(d);
                            incQueue.add(d);
                            decQueue.add(d);
                        }
                    }
                }
            }

            // If the list of posting lists is empty, break the loop
            if (postingLists.isEmpty()) {
                break;
            }
        }

        // Return the priority queue of documents
        return decQueue;
    }
}
