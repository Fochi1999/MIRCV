package it.unipi.mrcv.data_structures;

public class SkipElem {
    // Last docID in the block
    private int docID;

    // offset of the block relative to the docIds
    private long offsetDoc;

    // docId block length
    private int docBlockLen;

    // offset of the block relative to the frequencies
    private long offsetFreq;

    // frequency block length
    private int freqBlockLen;

    // default constructor
    public SkipElem() {
        this.docID = 0;
        this.offsetDoc = 0;
        this.docBlockLen = 0;
        this.offsetFreq = 0;
        this.freqBlockLen = 0;
    }

    // set methods
    public void setDocID(int docID) {
        this.docID = docID;
    }

    public void setOffsetDoc(long offsetDoc) {
        this.offsetDoc = offsetDoc;
    }

    public void setDocBlockLen(int docBlockLen) {
        this.docBlockLen = docBlockLen;
    }

    public void setOffsetFreq(long offsetFreq) {
        this.offsetFreq = offsetFreq;
    }

    public void setFreqBlockLen(int freqBlockLen) {
        this.freqBlockLen = freqBlockLen;
    }

    // get methods
    public int getDocID() {
        return docID;
    }

    public long getOffsetDoc() {
        return offsetDoc;
    }

    public int getDocBlockLen() {
        return docBlockLen;
    }

    public long getOffsetFreq() {
        return offsetFreq;
    }

    public int getFreqBlockLen() {
        return freqBlockLen;
    }

}
