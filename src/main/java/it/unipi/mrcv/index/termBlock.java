package it.unipi.mrcv.index;

import it.unipi.mrcv.data_structures.DictionaryElem;

public class termBlock{
    private DictionaryElem term;
    private int numBlock;
    public termBlock(String t,int n){
        this.term=new DictionaryElem(t);
        this.numBlock=n;
    }
    public termBlock(String t,int df,int cf,long offsetDoc,long offsetFreq,int length,int numBlock){
        this.term=new DictionaryElem(t);
        term.setDf(df);
        term.setCf(cf);
        term.setOffsetDoc(offsetDoc);
        term.setOffsetFreq(offsetFreq);
        term.setLength(length);
        this.numBlock=numBlock;
    }
    public termBlock(){

    }
    public String getTerm(){
        return this.term.getTerm();
    }
    public int getNumBlock(){
        return this.numBlock;
    }
}
