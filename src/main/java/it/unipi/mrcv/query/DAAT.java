package it.unipi.mrcv.query;

import it.unipi.mrcv.data_structures.DecComparatorDocument;
import it.unipi.mrcv.data_structures.Document;
import it.unipi.mrcv.data_structures.IncComparatorDocument;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class DAAT {
    public static PriorityQueue<Document> executeDAAT(ArrayList<String> queryTerms) {
        PriorityQueue<Document> incQueue = new PriorityQueue<>(new IncComparatorDocument());
        PriorityQueue<Document> decQueue = new PriorityQueue<>(new DecComparatorDocument());

        return null;
        // TODO
    }
}
