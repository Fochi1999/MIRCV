package it.unipi.mrcv.data_structures.Comparators;

import it.unipi.mrcv.data_structures.Document;

public class IncComparatorDocument implements java.util.Comparator<Document> {
    @Override
    public int compare(Document o1, Document o2) {
        if (o1.getScore() < o2.getScore()) {
            return -1;
        } else if (o1.getScore() > o2.getScore()) {
            return 1;
        } else {
            if(o1.getDocId()<o2.getDocId())
                return -1;
            else
                return 1;
        }
    }
}
