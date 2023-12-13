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
        if(compression == true) {
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
                    return null;
                }
            }
            else{
                return null;
            }
        }
    }

    // nextGEQ returns the first posting with docid greater or equal to docid
    public Posting nextGEQ(int docid) {
        // check the last docId of the current block
        if (skipElems != null) {

            while (skipElems.get(currentBlock).getDocID() < docid && currentBlock < skipElems.size()) {

                currentBlock++;
            }
            if (currentBlock == skipElems.size()) {
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
        int low = 0;
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
                return midPosting;
            }
        }

        if (low < postings.size()) {
            return postings.get(low);
        } else {
            return null;
        }
    }



}
