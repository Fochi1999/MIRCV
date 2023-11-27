package it.unipi.mrcv.data_structures;

import java.util.ArrayList;
import java.util.List;

public class PostingList {
    private String term;
    private final ArrayList<Posting> postings;

    /* Term Upper Bound for TF-IDF */
    private double maxTFIDF;

    /* Term Upper Bound for BM25 */
    private double maxBM25;


    public PostingList() {
        this.term = " ";
        this.postings = new ArrayList<>();
        this.actualPosting = null;
    }

    public PostingList(String term) {
        this.term = term;
        this.postings = new ArrayList<>();
    }

    public PostingList(String term, Posting p) {
        this.term = term;
        this.postings = new ArrayList<>();
        this.postings.add(p);
    }

    public void addPosting(Posting posting) {
        postings.add(posting);
    }

    public List<Posting> getPostings() {
        return postings;
    }

    public int size() {
        return postings.size();
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getTerm() {
        return term;
    }

    public ArrayList<Posting> getPl() {
        return postings;
    }

    public void printPostingList() {
        System.out.println("Posting List:");
        for (Posting p : this.getPl()) {
            System.out.printf("Docid: %d - Freq: %d\n", Posting.getDocid(), Posting.getFrequency());
        }
    }

    // You can add more methods as needed for manipulating the posting list
}
