package it.unipi.mrcv.data_structures;

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
    private int length;

    // default constructor
    public DictionaryElem(){
    };

    public DictionaryElem(String term){
        this.term = term;
        this.df = 1;
        this.cf = 1;
        this.offsetDoc = 0;
        this.offsetFreq = 0;
        this.length = 0;
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

    public void setLength(int length){
        this.length = length;
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

    public int getLength(){
        return this.length;
    };

    public static int size(){
        return 68; //size of everything in the class
    }

}
