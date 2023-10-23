package it.unipi.mrcv.index;

import it.unipi.mrcv.data_structures.Dictionary;
import it.unipi.mrcv.data_structures.DictionaryElem;
import it.unipi.mrcv.data_structures.Posting;
import it.unipi.mrcv.data_structures.PostingList;
import it.unipi.mrcv.preprocess.preprocess;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

public class SPIMI {
    public static int counterBlock=0;
    public static Dictionary dictionary= new Dictionary();
    /* Posting list of a term in memory */
    public static HashMap<String, PostingList> postingLists = new HashMap<>();
    //TODO: informazioni riguardo il documento

    public static void exeSPIMI(String path) throws IOException, InterruptedException {
        //TODO: per un tot di memoria leggere x file
        /* Setting the total used memory to 50% */
        long MaxUsableMemory = Runtime.getRuntime().maxMemory() * 15 / 100;
        int docId=0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
        String line = reader.readLine();
        while(line!=null){ //read a full collection

            docId=docId+1;
            List<String> tokens= preprocess.all(line);
            //System.out.println(tokens);
            //Thread.sleep(1000);
            for(String term:tokens){
                DictionaryElem entryDictionary=dictionary.getElem(term);
                if(entryDictionary==null){
                    dictionary.insertElem(new DictionaryElem(term));
                    PostingList List=new PostingList();
                    List.addPosting(new Posting(docId,1));
                    postingLists.put(term,List);
                }
                else{
                    entryDictionary.setCf(entryDictionary.getCf()+1);
                    PostingList List=postingLists.get(term);
                    List<Posting> Postings=List.getPostings();
                    Posting lastPosting=Postings.get(Postings.size()-1);
                    if(lastPosting.getDocid()==docId){
                        lastPosting.setFrequency(lastPosting.getFrequency()+1);
                    }
                    else{
                        entryDictionary.setDf(entryDictionary.getDf()+1);
                        Postings.add(new Posting(docId,1));
                    }
                }

                //System.out.println("Processato il termine "+term+" con docId:"+docId+". Memory usage: "+Runtime.getRuntime().totalMemory()+"/"+MaxUsableMemory);
                //System.out.println("dictionary:"+dictionary.toString());
                //System.out.println("Postinglist:"+postingLists.toString());
                //Thread.sleep(1);
                //System.out.println("Memory usage: "+Runtime.getRuntime().totalMemory()+"/"+MaxUsableMemory);
                if (Runtime.getRuntime().totalMemory() > MaxUsableMemory) {
                    System.out.printf("(INFO) MAXIMUM PERMITTED USE OF MEMORY ACHIEVED: WRITING BLOCK '%d' ON CURRENT DISC.\n", counterBlock);
                    //write to disk and reset dictionary and Posting Lists

                    counterBlock++;
                    dictionary=new Dictionary();
                    postingLists=new HashMap<>();
                    Thread.sleep(1000);
                }



            }
            line= reader.readLine();
        }
    }
}
