package it.unipi.mrcv.data_structures;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.charset.StandardCharsets;

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
    private int lengthDoc;

    private int lengthFreq;

    // default constructor
    public DictionaryElem(){
    };

    public DictionaryElem(String term){
        this.term = term;
        this.df = 1;
        this.cf = 1;
        this.offsetDoc = 0;
        this.offsetFreq = 0;
        this.lengthDoc = 0;
        this.lengthFreq = 0;
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

    public void setLengthDoc(int lengthDoc){
        this.lengthDoc = lengthDoc;
    };

    public void setLengthFreq(int lengthFreq){
        this.lengthFreq = lengthFreq;
    };

    public void setTerm(String term){
        this.term = term;
    };

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

    public int getLengthDoc(){
        return this.lengthDoc;
    };
    public int getLengthFreq(){
        return this.lengthFreq;
    };

    public static int size(){
        return 72; //size of everything in the class
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
        vocBuffer.putInt(lengthDoc);
        vocBuffer.putInt(lengthFreq);

    }

    public void printDebug() {
        System.out.println("DEBUG:");
        System.out.println("term: "+term);
        System.out.println("df: "+df);
        System.out.println("docLength: "+lengthDoc);
        System.out.println("freqLength: "+lengthFreq);
    }
}
