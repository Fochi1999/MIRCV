package it.unipi.mrcv.data_structures;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PostingList {
    private String term;
    private final ArrayList<Posting> postings;



    public PostingList() {
        this.term = " ";
        this.postings = new ArrayList<>();

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
    public void addPostings(ArrayList<Posting> postings) {
        this.postings.addAll(postings);
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


    public void printPostingList() {
        System.out.println("Posting List:");
        for (Posting p : postings) {
            System.out.printf("Docid: %d - Freq: %d\n", p.getDocid(), p.getFrequency());
        }
    }


}
