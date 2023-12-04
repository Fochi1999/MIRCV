package it.unipi.mrcv.query;

import it.unipi.mrcv.data_structures.*;
import it.unipi.mrcv.global.Global;
import it.unipi.mrcv.index.fileUtils;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class DAAT {
    public static PriorityQueue<Document> executeDAAT(ArrayList<String> queryTerms, int k) {
        // Create a priority queue of documents; this will be used to order documents from lowest to highest score
        PriorityQueue<Document> incQueue = new PriorityQueue<>(new IncComparatorDocument());
        // Create a priority queue of documents; this will be used to order documents from highest to lowest score and will be the output
        PriorityQueue<Document> decQueue = new PriorityQueue<>(new DecComparatorDocument());
        // Create a list of posting lists; this will be used to iterate through the posting lists of each query term
        ArrayList<PostingList> postingLists = new ArrayList<>();
        // Create a list of documents; this list is connected to the priority queues
        ArrayList<Document> documents = new ArrayList<>();
        // Create a list of dictionary elements
        ArrayList<DictionaryElem> dictionaryElems = new ArrayList<>();

        // For each query term, get the posting list and add it to the list of posting lists
        for (String term : queryTerms) {
            // get the dictionaryElem of the term using the binary search and set the posting list
            PostingList pl = new PostingList();
            DictionaryElem elem = fileUtils.binarySearchOnFile(term, "vocabularyCompressed", pl);
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

        // Create an array of integers that will be used to iterate through the posting lists
        int[] pointers = new int[postingLists.size()];
        // Create an array of integers that will be used to store the document ids of the posting lists
        int[] docIds = new int[postingLists.size()];
        // Create an array of integers that will be used to store the term frequencies of the posting lists
        int[] tfs = new int[postingLists.size()];
        // Create an array of doubles that will be used to store the inverse document frequencies of the posting lists
        double[] idfs = new double[postingLists.size()];
        // Create an array of integers that will be used to store the document lengths of the posting lists
        int[] docLengths = new int[postingLists.size()];
        // Create an array of doubles that will be used to store the scores of the documents
        double[] scores = new double[postingLists.size()];

        // For each posting list, get the first posting and store the document id, the term frequency, the inverse document frequency, the document length and the score
        for (int i = 0; i < postingLists.size(); i++) {
            Posting posting = postingLists.get(i).getPostings().get(0);
            docIds[i] = posting.getDocid();
            tfs[i] = posting.getFrequency();
            idfs[i] = dictionaryElems.get(i).getIdf();
            docLengths[i] = dictionaryElems.get(i).getDocLength();
            scores[i] = idfs[i] * (tfs[i] * (1.0 + 1.2) / (tfs[i] + 1.2 * (0.25 + 0.75 * docLengths[i] / Global.averageDocLength)));
        }





        return null;
        // TODO
    }
}
