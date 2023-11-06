package it.unipi.mrcv.index;

import it.unipi.mrcv.data_structures.*;
import it.unipi.mrcv.data_structures.Dictionary;
import it.unipi.mrcv.preprocess.preprocess;

import java.io.*;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class SPIMI {
    public static int counterBlock=0;
    public static Dictionary dictionary= new Dictionary();
    /* Posting list of a term in memory */
    public static InvertedIndex postingLists = new InvertedIndex();
    //TODO: informazioni riguardo il documento

    public static void exeSPIMI(String path) throws IOException, InterruptedException {
        //TODO: per un tot di memoria leggere x file
        /* Setting the total used memory to 80% */
        long MaxUsableMemory = Runtime.getRuntime().maxMemory() * 80 / 100;
        int docId=0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
        String line = reader.readLine();
        int numPosting=0;
        while(line!=null){ //read a full collection

            docId=docId+1;
            List<String> tokens= preprocess.all(line);
            //System.out.println(tokens);
            //Thread.sleep(1000);
            for(String term:tokens){
                DictionaryElem entryDictionary=dictionary.getElem(term);
                if(entryDictionary==null){
                    dictionary.insertElem(new DictionaryElem(term));
                    postingLists.addPosting(term,new Posting(docId,1));
                    numPosting++;

                }
                else{
                    entryDictionary.setCf(entryDictionary.getCf()+1);
                    PostingList list=postingLists.getPostings(term);
                    List<Posting> Postings=list.getPostings();
                    Posting lastPosting=Postings.get(Postings.size()-1);
                    if(lastPosting.getDocid()==docId){
                        lastPosting.setFrequency(lastPosting.getFrequency()+1);
                    }
                    else{
                        entryDictionary.setDf(entryDictionary.getDf()+1);
                        Postings.add(new Posting(docId,1));
                        numPosting++;
                    }
                }

                //System.out.println("Processato il termine "+term+" con docId:"+docId+". Memory usage: "+Runtime.getRuntime().totalMemory()+"/"+MaxUsableMemory);
                //System.out.println("dictionary:"+dictionary.toString());
                //System.out.println("Postinglist:"+postingLists.toString());
                //Thread.sleep(1);
                //System.out.println("Memory usage: "+Runtime.getRuntime().totalMemory()+"/"+MaxUsableMemory);
                //TODO: Usare inverted index al posto di TreeMap



            }
            if (Runtime.getRuntime().totalMemory() > MaxUsableMemory) {
                System.out.printf("(INFO) MAXIMUM PERMITTED USE OF MEMORY ACHIEVED: WRITING BLOCK '%d' ON CURRENT DISC.\n", counterBlock);
                //write to disk and reset dictionary and Posting Lists
                //Serializzazione e scrittura su file poi si vede dove metterla, la metteremo in inverted index perchè si
                ArrayList<RandomAccessFile> raf_array = new ArrayList<>();

                /* Create Dictionary file and get the corresponding FileChannel */
                raf_array.add(new RandomAccessFile(new File("PATH_TO_PARTIAL_DOCID" + counterBlock), "rw"));

                /* Create docIDs posting list file and get the corresponding FileChannel */
                raf_array.add(new RandomAccessFile(new File("PATH_TO_PARTIAL_FREQUENCIES" + counterBlock), "rw"));

                /* Create frequencies file and get the corresponding FileChannel */
                raf_array.add(new RandomAccessFile(new File("PATH_TO_PARTIAL_VOCABULARY" + counterBlock), "rw"));

                //RandomAccessFile_map.put(counterBlock, raf_array);

                try (
                        FileChannel docsFchan = (FileChannel) Files.newByteChannel(Paths.get("PATH_TO_PARTIAL_DOCID" + "_" + counterBlock),
                                StandardOpenOption.WRITE,
                                StandardOpenOption.READ,
                                StandardOpenOption.CREATE
                        );
                        FileChannel freqsFchan = (FileChannel) Files.newByteChannel(Paths.get("PATH_TO_PARTIAL_FREQUENCIES" + "_" + counterBlock),
                                StandardOpenOption.WRITE,
                                StandardOpenOption.READ,
                                StandardOpenOption.CREATE);
                        FileChannel vocabularyFchan = (FileChannel) Files.newByteChannel(Paths.get("PATH_TO_PARTIAL_VOCABULARY" + "_" + counterBlock),
                                StandardOpenOption.WRITE,
                                StandardOpenOption.READ,
                                StandardOpenOption.CREATE)
                ){
                    // instantiation of MappedByteBuffer for integer list of docids
                    MappedByteBuffer docsBuffer = docsFchan.map(FileChannel.MapMode.READ_WRITE, 0, numPosting*4);

                    // instantiation of MappedByteBuffer for integer list of freqs
                    MappedByteBuffer freqsBuffer = freqsFchan.map(FileChannel.MapMode.READ_WRITE, 0, numPosting* 4);
                    //TODO: allocare la memoria giusta per il vocabulary
                    MappedByteBuffer vocBuffer = vocabularyFchan.map(FileChannel.MapMode.READ_WRITE,0, dictionary.size()*20);
                    long vocOffset = 0;
                    // check if MappedByteBuffers are correctly instantiated
                    for (Map.Entry<String, PostingList>
                            entry : postingLists.getTree().entrySet()){
                        DictionaryElem dictionaryElem=dictionary.getElem(entry.getKey());
                        dictionaryElem.setOffsetDoc(docsBuffer.position());
                        dictionaryElem.setOffsetFreq(freqsBuffer.position());
                        int counter=0;
                        for(Posting posting:entry.getValue().getPostings()){
                            docsBuffer.putInt(posting.getDocid());
                            freqsBuffer.putInt(posting.getFrequency());
                            counter++;
                        }
                        dictionaryElem.setLength(counter);
                        //allocate char buffer to write term
                        CharBuffer charBuffer = CharBuffer.allocate(40); //TODO: verificare la size
                        String term=dictionaryElem.getTerm();
                        //populate char buffer char by char
                        for (int i = 0; i < term.length(); i++)
                            charBuffer.put(i, term.charAt(i));

                        // Write the term into file
                        vocBuffer.put(StandardCharsets.UTF_8.encode(charBuffer));

                        // write statistics
                        vocBuffer.putInt(dictionaryElem.getDf());
                        vocBuffer.putInt(dictionaryElem.getCf());
                        vocBuffer.putLong(dictionaryElem.getOffsetDoc());
                        vocBuffer.putLong(dictionaryElem.getOffsetFreq());
                        vocBuffer.putInt(dictionaryElem.getLength());
                    }
                }
                counterBlock++;
                dictionary=new Dictionary();
                postingLists=new InvertedIndex();
                Thread.sleep(1000);
            }

            line= reader.readLine();
        }
    }
}

