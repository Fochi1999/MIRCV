package src.main.java.org;

import java.util.ArrayList;
import java.util.List;

public class PostingList {
    private List<Posting> postings;

    public PostingList() {
        postings = new ArrayList<>();
    }

    public void addPosting(Posting posting) {
        postings.add(posting);
    }

    public List<Posting> getPostings() {
        return postings;
    }

    // You can add more methods as needed for manipulating the posting list
}
