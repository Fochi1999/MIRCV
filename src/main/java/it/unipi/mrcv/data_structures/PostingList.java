package it.unipi.mrcv.data_structures;

import it.unipi.mrcv.compression.Unary;
import it.unipi.mrcv.compression.VariableByte;
import it.unipi.mrcv.global.Global;
import it.unipi.mrcv.index.fileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static it.unipi.mrcv.global.Global.compression;

public class PostingList {
    private String term;
    private final ArrayList<Posting> postings;
    public ArrayList<SkipElem> skipElems = null;
    public int currentBlock = -1;
    public int currentPosition = -1 ;

    public static long startTime1,startTime2,startTime3, endTime1,endTime2,endTime3;
    public static long totalTime1=0,totalTime2=0,totalTime3=0;
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

    public PostingList(DictionaryElem elem) throws IOException {
        this.term = elem.getTerm();
        this.postings = new ArrayList<>();
        currentPosition = 0;
        if(compression) {
            if (elem.getSkipLen() != 0) {
                this.skipElems = SkipElem.readSkipList(elem.getOffsetSkip(), elem.getSkipLen());
                loadBlock(skipElems.get(0).getOffsetDoc(),
                        skipElems.get(0).getOffsetFreq(),
                        skipElems.get(0).getDocBlockLen(),
                        skipElems.get(0).getFreqBlockLen());
            }
            else {
                loadBlock(elem.getOffsetDoc(), elem.getOffsetFreq(), elem.getLengthDocIds(), elem.getLengthFreq());
            }
            currentBlock++;
        }else{
            this.addPostings(fileUtils.readPosting(elem.getOffsetDoc(), elem.getLengthDocIds()));
        }
    }
    public void addPosting(Posting posting) {
        postings.add(posting);
    }
    public void addPostings(ArrayList<Posting> postings) {
        this.postings.addAll(postings);
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


    public void printPostingList() {
        System.out.println("Posting List:");
        for (Posting p : postings) {
            System.out.printf("Docid: %d - Freq: %d\n", p.getDocid(), p.getFrequency());
        }
    }

    private void loadBlock(long offsetDoc, long offsetFreq, int lengthDoc, int lenghtFreq) throws IOException {
        postings.clear();
        if(compression) {

            byte[] docsBytes = fileUtils.readCompressed(Global.docIdsChannel, offsetDoc, lengthDoc);
            byte[] freqsBytes = fileUtils.readCompressed(Global.frequenciesChannel, offsetFreq, lenghtFreq);


            ArrayList<Integer> docIds = VariableByte.fromByteToArrayInt(docsBytes);

            ArrayList<Integer> freqs = Unary.unaryToArrayInt(freqsBytes);

            for (int i = 0; i < docIds.size(); i++) {
                int docId = docIds.get(i);
                int freq = freqs.get(i);
                addPosting(new Posting(docId, freq));
            }

        }

    }
    public Posting getCurrent(){
        if(currentPosition==-1){
            return null;
        }
        if(currentPosition < postings.size()){
            return postings.get(currentPosition);
        }
        else{
            return null;
        }
    }
    public Posting next(){
        if(currentPosition+1<postings.size()){
            currentPosition++;
            return postings.get(currentPosition);
        }
        else{ //block finished
            if(skipElems!=null){
                currentBlock++;
                if(currentBlock<skipElems.size()){
                    currentPosition = 0;
                    try {
                        loadBlock(skipElems.get(currentBlock).getOffsetDoc(), skipElems.get(currentBlock).getOffsetFreq(), skipElems.get(currentBlock).getDocBlockLen(), skipElems.get(currentBlock).getFreqBlockLen());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return postings.get(currentPosition);
                }
                else{
                    currentPosition=-1; //finished the pl
                    return null;
                }
            }
            else{
                currentPosition=-1;
                return null;
            }
        }
    }

    // nextGEQ returns the first posting with docid greater or equal to docid
    public Posting nextGEQ(int docid) {
        // check the last docId of the current block
        //check that the currentId is not greater than the requested one
        if(currentBlock>=skipElems.size()){
            return null;
        }
        if(this.getCurrent()==null){
            return null;
        }
        if(this.getCurrent().getDocid()>=docid){
            return this.getCurrent();
        }

        //check which block is the one we are looking for (the first docId is greater or equal to the requested one)
        if (skipElems != null && skipElems.get(currentBlock).getDocID() <= docid) {

           //NON BINARY

            while (currentBlock < skipElems.size() && skipElems.get(currentBlock).getDocID() < docid) {
                currentBlock++;
            }

            //BINARY (doesn't work)
/*
            int low = currentBlock;
            int high = skipElems.size() - 1;
            while(high>low && currentBlock<skipElems.size()){
                currentBlock=(low+high)/2;
                if(skipElems.get(currentBlock).getDocID()<docid){
                    low=currentBlock+1;
                }
                else if(skipElems.get(currentBlock).getDocID()>docid){
                    if(skipElems.get(currentBlock-1).getDocID()<docid){
                        break;
                    }
                    high=currentBlock-1;
                }

            }*/

            if (currentBlock == skipElems.size()) {
                currentPosition=-1;
                return null;
            }
            currentPosition = 0;
            try {
                loadBlock(skipElems.get(currentBlock).getOffsetDoc(), skipElems.get(currentBlock).getOffsetFreq(), skipElems.get(currentBlock).getDocBlockLen(), skipElems.get(currentBlock).getFreqBlockLen());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else {
            if (postings.get(postings.size() - 1).getDocid() < docid) {
                return null;
            }
        }
        if (postings.isEmpty()) {
            return null;
        }
        // binary search of the docid
        int low = currentPosition;
        int high = postings.size() - 1;

        while (low <= high) {
            // bitwise shift to divide by 2
            int mid = (low + high) >>> 1;
            Posting midPosting = postings.get(mid);
            int midDocId = midPosting.getDocid();
            if (midDocId < docid) {
                low = mid + 1;
            } else if (midDocId > docid) {
                high = mid - 1;
            } else {
                currentPosition=mid;
                return midPosting;
            }
        }

        if (low < postings.size()) {
            currentPosition=low;
            return postings.get(low);
        } else {
            return null;
        }
    }



}
