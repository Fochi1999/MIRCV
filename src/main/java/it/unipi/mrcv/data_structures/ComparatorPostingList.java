package it.unipi.mrcv.data_structures;

import java.util.Comparator;

public class ComparatorPostingList implements Comparator<PostingList> {
    @Override
    // compare two posting lists and return the one with the smallest first docid
    public int compare(PostingList o1, PostingList o2) {
        if (o1.getPostings().get(0).getDocid() < o2.getPostings().get(0).getDocid()) {
            return -1;
        } else if (o1.getPostings().get(0).getDocid() > o2.getPostings().get(0).getDocid()) {
            return 1;
        } else {
            return 0;
        }
    }
}
