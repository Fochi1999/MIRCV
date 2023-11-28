package it.unipi.mrcv.data_structures;

import java.io.IOException;
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
            System.out.printf("Docid: %d - Freq: %d\n", p.getDocid(), p.getFrequency());
        }
    }

    public void obtainPostingList(String term) throws IOException {
        // Placeholder: Replace with actual implementation based on your data source

        // For example, if you have a method to retrieve posting lists from a database:
        // this.pl = Database.retrievePostingList(term);

        // If posting lists are stored in memory, you might have a data structure like a map:
        // this.pl = InMemoryCache.getPostingList(term);

        // Adjust the logic based on your specific data retrieval mechanism
    }

    public Double getMaxBM25() {
        return maxBM25;
    }

    public Double getMaxTFIDF() {
        return maxTFIDF;
    }



    // You can add more methods as needed for manipulating the posting list
}
