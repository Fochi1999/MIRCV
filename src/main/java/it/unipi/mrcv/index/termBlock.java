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
        this.term = new DictionaryElem();
        this.numBlock = 0;
    }
    public String getTerm(){
        return this.term.getTerm();
    }
    public int getNumBlock(){
        return this.numBlock;
    }
    public DictionaryElem getDictionaryElem(){
        return this.term;
    }

    public void setNumBlock(int numBlock) {
        this.numBlock = numBlock;
    }

    public void copyBlock(termBlock t){
        this.term.setDf(t.getDictionaryElem().getDf());
        this.term.setCf(t.getDictionaryElem().getCf());
        this.term.setOffsetDoc(t.getDictionaryElem().getOffsetDoc());
        this.term.setOffsetFreq(t.getDictionaryElem().getOffsetFreq());
        this.term.setLength(t.getDictionaryElem().getLength());
        this.numBlock=t.getNumBlock();
    }
}
