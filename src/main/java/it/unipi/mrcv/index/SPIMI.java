package it.unipi.mrcv.index;

import it.unipi.mrcv.compression.Unary;
import it.unipi.mrcv.compression.VariableByte;
import it.unipi.mrcv.data_structures.*;
import it.unipi.mrcv.data_structures.Dictionary;
import it.unipi.mrcv.global.Global;
import it.unipi.mrcv.preprocess.preprocess;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;


public class SPIMI {
    // counter for the block
    public static int counterBlock = 0;
    // counter for the postings in the block
    public static int numPosting = 0;
    // Dictionary in memory
    public static Dictionary dictionary = new Dictionary();
    // Posting Lists in memory
    public static InvertedIndex postingLists = new InvertedIndex();
    // allocate memory for the docIndex file; this is a magic number, it is more than enough for one block worth of docIdex
    public static ByteBuffer docIndexBuffer = ByteBuffer.allocateDirect(1024 * 1024 * 100);

    public static void exeSPIMI(String path) throws IOException, InterruptedException {
        // Max memory usable by the JVM
        long MaxUsableMemory = Runtime.getRuntime().maxMemory() * 80 / 100;
        // docIds start from 0
        int docId = 0;
        // variable that stores the current docNumber
        int documentNumber = 0;
        // read the document collection line by line
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
        // read the first line
        String line = reader.readLine();

        // read the document collection line by line and execute the SPIMI algorithm
        while (line != null) {

            //split the line in two parts: the first is the document number, the second is the text
            String[] parts = line.split("\t", 2);
            try {
                documentNumber = Integer.parseInt(parts[0]);
            } catch (NumberFormatException e) {
                System.err.println("The first part is not an integer. Exiting.");
                System.exit(1);
            }

            // preprocess the line and obtain the tokens
            List<String> tokens = preprocess.all(parts[1]);
            // write the docId, the document number and the document length in the docIndex buffer
            docIndexBuffer.putInt(docId);
            docIndexBuffer.putInt(documentNumber);
            docIndexBuffer.putInt(tokens.size());

            // for each term in the line create/update posting lists and dictionary
            for (String term : tokens) {
                if (term.length() == 0) {
                    continue;
                }
                DictionaryElem entryDictionary = dictionary.getElem(term);
                // if the term is not in the dictionary we create a new entry
                if (entryDictionary == null) {
                    dictionary.insertElem(new DictionaryElem(term));
                    postingLists.addPosting(term, new Posting(docId, 1));
                    numPosting++;
                // if the term is in the dictionary we update the entry
                } else {
                    entryDictionary.setCf(entryDictionary.getCf() + 1);
                    PostingList list = postingLists.getPostings(term);
                    List<Posting> Postings = list.getPostings();
                    Posting lastPosting = Postings.get(Postings.size() - 1);
                    // if the last posting has the same docId we update the frequency
                    if (lastPosting.getDocid() == docId) {
                        lastPosting.setFrequency(lastPosting.getFrequency() + 1);
                    // if the last posting has a different docId we create a new posting and update the document frequency
                    } else {
                        entryDictionary.setDf(entryDictionary.getDf() + 1);
                        Postings.add(new Posting(docId, 1));
                        numPosting++;
                    }
                }
            }
            // increase the docId
            docId++;
            // check if the memory is full, in which case the block is written on disk
            if (Runtime.getRuntime().totalMemory() > MaxUsableMemory) {
                System.out.printf("(INFO) MAXIMUM PERMITTED USE OF MEMORY ACHIEVED: WRITING BLOCK '%d' ON CURRENT DISC.\n", counterBlock);
                writeToDisk();
            }
            // read the next line if memory is not full
            line = reader.readLine();
        }
        // write the last block on disk
        writeToDisk();
    }

    // function that writes the block on disk
    private static void writeToDisk() throws IOException, InterruptedException {
        // instantiate the fileUtils class
        try (
                FileChannel docIndexFchan = FileChannel.open(Paths.get("docIndex"),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );
                FileChannel docsFchan = (FileChannel) Files.newByteChannel(Paths.get(Global.prefixDocFiles + counterBlock),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE
                );
                FileChannel freqsFchan = (FileChannel) Files.newByteChannel(Paths.get(Global.prefixFreqFiles + counterBlock),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE);
                FileChannel vocabularyFchan = (FileChannel) Files.newByteChannel(Paths.get(Global.prefixVocFiles + counterBlock),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE)
        ) {
            // writing the docIndex buffer to file
            docIndexBuffer.flip();
            // Write the contents of the buffer to the file
            while (docIndexBuffer.hasRemaining()) {
                docIndexFchan.write(docIndexBuffer);
            }

            // instantiation of MappedByteBuffer for integer list of docids
            MappedByteBuffer docsBuffer = docsFchan.map(FileChannel.MapMode.READ_WRITE, 0, numPosting * 4);
            // instantiation of MappedByteBuffer for integer list of freqs
            MappedByteBuffer freqsBuffer = freqsFchan.map(FileChannel.MapMode.READ_WRITE, 0, numPosting * 4);
            // instantiation of MappedByteBuffer for vocabulary
            MappedByteBuffer vocBuffer = vocabularyFchan.map(FileChannel.MapMode.READ_WRITE, 0, dictionary.size());

            // for each term in the term-postingList treemap write everything to file
            for (Map.Entry<String, PostingList>
                    entry : postingLists.getTree().entrySet()) {
                // update the offset of the term in the dictionary
                DictionaryElem dictionaryElem = dictionary.getElem(entry.getKey());
                dictionaryElem.setOffsetDoc(docsBuffer.position());
                dictionaryElem.setOffsetFreq(freqsBuffer.position());
                int counter = 0;
                // write the postings in the respective docIds and frequencies files
                for (Posting posting : entry.getValue().getPostings()) {
                    docsBuffer.putInt(posting.getDocid());
                    freqsBuffer.putInt(posting.getFrequency());
                    counter++;
                }

                // update the length of the posting list in the dictionary
                dictionaryElem.setLengthDocIds(counter);
                dictionaryElem.setLengthFreq(counter);
                dictionaryElem.writeElemToDisk(vocBuffer);


            }
        }

        // increase the counter of the block
        counterBlock++;
        // clear the memory
        postingLists.clear();
        dictionary.clear();
        docIndexBuffer.clear();
        // hope that the garbage collector will free the memory
        System.gc();
        Thread.sleep(1500);
        // reset data structures
        dictionary = new Dictionary();
        postingLists = new InvertedIndex();
        // reset the number of postings
        numPosting = 0;
    }
    private static void writeToDiskCompressed() throws IOException, InterruptedException {
        try (
                FileChannel docsFchan = (FileChannel) Files.newByteChannel(Paths.get(Global.prefixDocFiles + counterBlock),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE
                );
                FileChannel freqsFchan = (FileChannel) Files.newByteChannel(Paths.get(Global.prefixFreqFiles + counterBlock),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE);
                FileChannel vocabularyFchan = (FileChannel) Files.newByteChannel(Paths.get(Global.prefixVocFiles + counterBlock),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE)
        ) {
            // instantiation of MappedByteBuffer for integer list of docids
            MappedByteBuffer docsBuffer = docsFchan.map(FileChannel.MapMode.READ_WRITE, 0, numPosting * 4);
            // instantiation of MappedByteBuffer for integer list of freqs
            MappedByteBuffer freqsBuffer = freqsFchan.map(FileChannel.MapMode.READ_WRITE, 0, numPosting * 4);
            //TODO: allocare la memoria giusta per il vocabulary

            MappedByteBuffer vocBuffer = vocabularyFchan.map(FileChannel.MapMode.READ_WRITE, 0, dictionary.size());
        }

    }

    // function that reads the docIds file OR the freqs file and prints them
    public static void readIndex(String path) {
        try (FileInputStream fis = new FileInputStream(path);
             FileChannel fileChannel = fis.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);

            while (fileChannel.read(buffer) != -1) {
                buffer.flip(); // Prepare the buffer for reading

                if (buffer.remaining() >= Integer.BYTES) {
                    int frequency = buffer.getInt();
                    System.out.println(frequency); // Print the integer
                } else {
                    // Not enough bytes for a full integer, handle partial read or end of file
                    System.err.println("Partial read or end of file reached. Exiting.");
                    break;
                }

                buffer.clear(); // Clear the buffer for the next read
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void readCompressedDic(String path) {

            readCompressedDic(path,null,null,false,false);

    }

    public static void readCompressedDic(String path,String path2, boolean docids,boolean freq) {
        if(docids && freq){
            System.out.println("INSERT ALL VARIABLES");
            return;
        }
        if(docids){
            readCompressedDic(path,path2,null,true,false);
        }
        if(freq){
            readCompressedDic(path,null,path2,false,true);
        }
    }

    public static void readCompressedDic(String path,String path2,String path3, boolean docids,boolean freq){
        try (FileChannel vocFchan = (FileChannel) Files.newByteChannel(Paths.get(path),
                StandardOpenOption.READ)) {

            // Size for each term in bytes
            int termSize = 40;
            // Sizes for the other integers and longs
            int intSize = Integer.BYTES; // 4 bytes
            int longSize = Long.BYTES;   // 8 bytes
            // Total size of one dictionary entry
            int entrySize = termSize + 4 * intSize + 2 * longSize;

            ByteBuffer buffer = ByteBuffer.allocate(entrySize);

            while (vocFchan.read(buffer) != -1) {
                buffer.flip(); // Prepare the buffer for reading

                if (buffer.remaining() >= entrySize) {
                    // Read term
                    byte[] termBytes = new byte[termSize];
                    buffer.get(termBytes);
                    String term = decodeTerm(termBytes);

                    // Read statistics
                    int df = buffer.getInt();
                    int cf = buffer.getInt();
                    long offsetDoc = buffer.getLong();
                    long offsetFreq = buffer.getLong();
                    int lengthDoc = buffer.getInt();
                    int lengthFreq = buffer.getInt();

                    // Print the details
                    System.out.println("Term: " + term);
                    System.out.println("Document Frequency (df): " + df);
                    System.out.println("Collection Frequency (cf): " + cf);
                    System.out.println("Offset Doc: " + offsetDoc);
                    System.out.println("Offset Freq: " + offsetFreq);
                    System.out.println("LengthDoc: " + lengthDoc);
                    System.out.println("LengthFreq: " + lengthFreq);
                    if(docids) {
                        System.out.print("DocIds: ");
                        readFromCompressedDocIds(path2, lengthDoc,  offsetDoc);
                        System.out.println("");
                    }
                    if(freq){
                        System.out.print("Frequencies: ");
                        readFromCompressedFrequencies(path3,lengthFreq,offsetFreq);
                    }

                    System.out.println("-------------------------");
                } else {
                    // Not enough data for a full dictionary entry, handle partial read or end of file
                    System.err.println("Partial read or end of file reached. Exiting.");
                    break;
                }

                buffer.clear(); // Clear the buffer for the next read
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void readFromCompressedFrequencies(String path3, int lengthFreq, long offsetFreq) {
        try{
            RandomAccessFile raf=new RandomAccessFile(new File(path3),"r");
            byte[] b=new byte[lengthFreq];
            raf.seek(offsetFreq);
            raf.read(b);
            ArrayList<Integer> ret= Unary.unaryToArrayInt(b);
            for(int x: ret){
                System.out.print(x+" ");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readDictionary(String path) {
        try (FileChannel vocFchan = (FileChannel) Files.newByteChannel(Paths.get(path),
                StandardOpenOption.READ)) {

            // Size for each term in bytes
            int termSize = 40;
            // Sizes for the other integers and longs
            int intSize = Integer.BYTES; // 4 bytes
            int longSize = Long.BYTES;   // 8 bytes
            // Total size of one dictionary entry
            int entrySize = termSize + 4 * intSize + 2 * longSize;

            ByteBuffer buffer = ByteBuffer.allocate(entrySize);

            while (vocFchan.read(buffer) != -1) {
                buffer.flip(); // Prepare the buffer for reading

                if (buffer.remaining() >= entrySize) {
                    // Read term
                    byte[] termBytes = new byte[termSize];
                    buffer.get(termBytes);
                    String term = decodeTerm(termBytes);

                    // Read statistics
                    int df = buffer.getInt();
                    int cf = buffer.getInt();
                    long offsetDoc = buffer.getLong();
                    long offsetFreq = buffer.getLong();
                    int lengthDoc = buffer.getInt();
                    int lengthFreq = buffer.getInt();

                    // Print the details
                    System.out.println("Term: " + term);
                    System.out.println("Document Frequency (df): " + df);
                    System.out.println("Collection Frequency (cf): " + cf);
                    System.out.println("Offset Doc: " + offsetDoc);
                    System.out.println("Offset Freq: " + offsetFreq);
                    System.out.println("Length: " + lengthDoc);
                    System.out.println("LengthFreq: " + lengthFreq);
                    System.out.println("-------------------------");
                } else {
                    // Not enough data for a full dictionary entry, handle partial read or end of file
                    System.err.println("Partial read or end of file reached. Exiting.");
                    break;
                }

                buffer.clear(); // Clear the buffer for the next read
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void readFromCompressedDocIds(String path,int length,long offset){
        try{
            RandomAccessFile raf=new RandomAccessFile(new File(path),"r");
            byte[] b=new byte[length];
            raf.seek(offset);
            raf.read(b);
            ArrayList<Integer> ret=VariableByte.fromByteToArrayInt(b);
            for(int x: ret){
                System.out.print(x+" ");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // function that decodes the term from the byte array
    public static String decodeTerm(byte[] termBytes) {
        // Create a ByteBuffer from the byte array
        ByteBuffer termBuffer = ByteBuffer.wrap(termBytes);
        // Decode the ByteBuffer into a CharBuffer
        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(termBuffer);
        // Convert CharBuffer to String
        return charBuffer.toString().trim(); // Trim the string in case there are any zero padding bytes
    }

}

