package it.unipi.mrcv.data_structures;

import it.unipi.mrcv.global.Global;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

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

    // return the size of the skipping element
    public static int size() {
        return Integer.BYTES + Long.BYTES + Integer.BYTES + Long.BYTES + Integer.BYTES;
    }

    // write the skip element to the file using the mapped byte buffer
    public void writeToFile(MappedByteBuffer buffer) {
        buffer.putInt(docID);
        buffer.putLong(offsetDoc);
        buffer.putInt(docBlockLen);
        buffer.putLong(offsetFreq);
        buffer.putInt(freqBlockLen);
    }

    public void readFromFile(MappedByteBuffer buffer) {
        docID = buffer.getInt();
        offsetDoc = buffer.getLong();
        docBlockLen = buffer.getInt();
        offsetFreq = buffer.getLong();
        freqBlockLen = buffer.getInt();
    }

    public static ArrayList<SkipElem> readMultipleFromFile(long offset, int n) throws IOException {
        ArrayList<SkipElem> skipElems = new ArrayList<>(n);
        MappedByteBuffer mbbSkipping = Global.skippingChannel.map(FileChannel.MapMode.READ_ONLY,offset,SkipElem.size()*n).load();
        for (int i = 0; i < n; i++) {
            SkipElem skipElem = new SkipElem();
            skipElem.readFromFile(mbbSkipping);
            skipElems.add(skipElem);
        }
        return skipElems;
    }

    public void printDebug() {
        System.out.printf("DocID: %d - OffsetDoc: %d - DocBlockLen: %d - OffsetFreq: %d - FreqBlockLen: %d\n",
                docID, offsetDoc, docBlockLen, offsetFreq, freqBlockLen);
    }
}
