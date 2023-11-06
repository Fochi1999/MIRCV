package it.unipi.mrcv.data_structures;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.List;

public class InvertedIndex {

    private TreeMap<String, PostingList> index;

    public InvertedIndex() {
        index = new TreeMap<>();
    }

    public void addPosting(String term, Posting posting) {
        // Check if the term is already in the index
        if (index.containsKey(term)) {
            PostingList postingList = index.get(term);
            postingList.addPosting(posting);
        } else {
            PostingList postingList = new PostingList();
            postingList.addPosting(posting);
            index.put(term, postingList);
        }
    }

    public PostingList getPostings(String term) {
        return index.getOrDefault(term, new PostingList());
    }

    public List<String> getTerms() {
        return new ArrayList<>(index.keySet());
    }


    public void writeToFile(String filename) {
        // You can implement the file writing logic here
        // Open the file, iterate through the index, and write it to the file
        // You may want to use try-with-resources to handle file I/O
    }

}