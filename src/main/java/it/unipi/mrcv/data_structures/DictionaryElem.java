package it.unipi.mrcv.data_structures;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.charset.StandardCharsets;
import static it.unipi.mrcv.index.fileUtils.collectionLength;

public class DictionaryElem {
    // term
    private String term;
    // document frequency (n. of documents containing the term)
    private int df;
    // collection frequency (n. of occurrences)
    private int cf;
    // offset of the posting list containing docIds
    private long offsetDoc;
    // offset of the posting list containing frequencies
    private long offsetFreq;

    // length of the posting list
    private int lengthDocIds;

  // length of the posting list (frequencies)
    private int lengthFreq;
    // max term frequency
    private int maxTF;

    // offset of the skipping information
    private long offsetSkip;

    // length of the skipping information
    private int skipLen;

    // inverse document frequency
    private double idf;

    /* Term upper bound for TF-IDF */
    private double maxTFIDF;

    /* Term upper bound for BM25 */
    private double maxBM25;

    // default constructor
    public DictionaryElem(){
    };

    public DictionaryElem(String term){
        this.term = term;
        this.df = 1;
        this.cf = 1;
        this.offsetDoc = 0;
        this.offsetFreq = 0;
        this.lengthDocIds = 0;
        this.lengthFreq = 0;
        this.maxTF = 0;
        this.offsetSkip = 0;
        this.skipLen = 0;
    };

    // set methods
    public void setDf(int df){
        this.df = df;
    };

    public void setCf(int cf){
        this.cf = cf;
    };

    public void setOffsetDoc(long offsetDoc){
        this.offsetDoc = offsetDoc;
    };

    public void setOffsetFreq(long offsetFreq){
        this.offsetFreq = offsetFreq;
    };

    public void setLengthDocIds(int lengthDocIds){
        this.lengthDocIds = lengthDocIds;
    };

    public void setLengthFreq(int lengthFreq){
        this.lengthFreq = lengthFreq;
    };

    public void setTerm(String term){
        this.term = term;
    };

    public void setMaxTF(int maxTF){
        this.maxTF = maxTF;
    };

    public void setOffsetSkip(long offsetSkip){
        this.offsetSkip = offsetSkip;
    };

    public void setSkipLen(int skipLen){
        this.skipLen = skipLen;
    };

    public void setIdf() {
        this.idf = Math.log10(collectionLength / (double)this.df);
    }

    public void setMaxTFIDF() {
        this.maxTFIDF = (1 + Math.log10(this.maxTF)) * this.idf;
    }

    public void setMaxBM25(double maxBM25) { this.maxBM25 = maxBM25; }

    public void updateMaxBM25(PostingList pl) {
        double current_BM25;

        for (Posting p: pl.getPostings()) {
            current_BM25 = (p.getFrequency() /
                    ((1 - 0.75) + 0.75 * (SPIMI.DocsLen.get((int) (p.getDocID()-1)) / SPIMI.avdl)
                            + p.getFrequency()))*this.idf;

            if (current_BM25 > this.getMaxBM25())
                this.setMaxBM25(current_BM25);
        }
    }

    // get methods
    public String getTerm(){
        return this.term;
    };

    public int getDf(){
        return this.df;
    };

    public int getCf(){
        return this.cf;
    };

    public long getOffsetDoc(){
        return this.offsetDoc;
    };

    public long getOffsetFreq(){
        return this.offsetFreq;
    };

    public int getLengthDocIds(){
        return this.lengthDocIds;
    };

    public int getLengthFreq(){
        return this.lengthFreq;
    };

    public int getMaxTF(){
        return this.maxTF;
    };

    public long getOffsetSkip(){
        return this.offsetSkip;
    };

    public int getSkipLen(){
        return this.skipLen;
    };

    public double getMaxBM25() { return maxBM25; }

    public static int SPIMIsize(){
        return 72; // size of everything needed for the SPIMI algorithm
    }

    public static int size(){
        return 88; // size of everything in the class
    }

    public void writeElemToDisk(MappedByteBuffer vocBuffer){
        CharBuffer charBuffer = CharBuffer.allocate(40);
        String term = this.term;
        for (int i = 0; i < term.length() && i < 40; i++)
            charBuffer.put(i, term.charAt(i));

        // Write the term into file
        ByteBuffer truncatedBuffer = ByteBuffer.allocate(40); // Allocate buffer for 40 bytes
        // Encode the CharBuffer into a ByteBuffer
        ByteBuffer encodedBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        // Ensure the buffer is at the start before reading from it
        encodedBuffer.rewind();
        // Transfer bytes to the new buffer
        for (int i = 0; i < 40; i++) {
            truncatedBuffer.put(encodedBuffer.get(i));
        }

        truncatedBuffer.rewind();
        vocBuffer.put(truncatedBuffer);

        // write statistics
        vocBuffer.putInt(df);
        vocBuffer.putInt(cf);
        vocBuffer.putLong(offsetDoc);
        vocBuffer.putLong(offsetFreq);
        vocBuffer.putInt(lengthDocIds);
        vocBuffer.putInt(lengthFreq);
        // to consider for buffersize
        vocBuffer.putInt(maxTF);
        vocBuffer.putLong(offsetSkip);
        vocBuffer.putInt(skipLen);
        vocBuffer.putDouble(idf);
        vocBuffer.putDouble(maxTFIDF);
        vocBuffer.putDouble(maxBM25);

    }

    public void writeElemToDiskSPIMI(MappedByteBuffer vocBuffer){
        CharBuffer charBuffer = CharBuffer.allocate(40);
        String term = this.term;
        for (int i = 0; i < term.length() && i < 40; i++)
            charBuffer.put(i, term.charAt(i));

        // Write the term into file
        ByteBuffer truncatedBuffer = ByteBuffer.allocate(40); // Allocate buffer for 40 bytes
        // Encode the CharBuffer into a ByteBuffer
        ByteBuffer encodedBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        // Ensure the buffer is at the start before reading from it
        encodedBuffer.rewind();
        // Transfer bytes to the new buffer
        for (int i = 0; i < 40; i++) {
            truncatedBuffer.put(encodedBuffer.get(i));
        }

        truncatedBuffer.rewind();
        vocBuffer.put(truncatedBuffer);

        // write statistics
        vocBuffer.putInt(df);
        vocBuffer.putInt(cf);
        vocBuffer.putLong(offsetDoc);
        vocBuffer.putLong(offsetFreq);
        vocBuffer.putInt(lengthDoc);
        vocBuffer.putInt(lengthFreq);

    }

    public void printDebug() {
        System.out.println("DEBUG:");
        System.out.println("term: "+term);
        System.out.println("df: "+df);
        System.out.println("docLength: "+ lengthDocIds);
        System.out.println("freqLength: "+lengthFreq);
    }
}
