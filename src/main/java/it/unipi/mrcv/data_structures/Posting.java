package it.unipi.mrcv.data_structures;

public class Posting {

    private int docid;
    private int frequency;

    // default constructor
    public Posting() {
    }

    // constructor that takes the docid and frequency as input
    public Posting(int docid, int frequency) {
        this.docid = docid;
        this.frequency = frequency;
    }

    // utility methods
    public static int getDocid() {
        return docid;
    }

    public void setDocid(int docid) {
        this.docid = docid;
    }

    public static int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return "Posting{docid=" + docid + ", frequency=" + frequency + '}';
    }
}