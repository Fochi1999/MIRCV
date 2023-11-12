package it.unipi.mrcv.index;

import it.unipi.mrcv.data_structures.*;
import it.unipi.mrcv.data_structures.Dictionary;
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
    public static int counterBlock = 0;
    public static int numPosting = 0;
    public static Dictionary dictionary = new Dictionary();
    /* Posting list of a term in memory */
    public static InvertedIndex postingLists = new InvertedIndex();
    //TODO: informazioni riguardo il documento
    public static int debugCounter=0; //TODO ELIMINARE
    public static void exeSPIMI(String path) throws IOException, InterruptedException {
        //TODO: per un tot di memoria leggere x file
        /* Setting the total used memory to 80% */
        long MaxUsableMemory = Runtime.getRuntime().maxMemory() * 80 / 100;
        int docId = 0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
        String line = reader.readLine();

        while (line != null) { //read a full collection

            docId = docId + 1;
            List<String> tokens = preprocess.all(line);
            for (String term : tokens) {
                if(term.length()==0){
                    continue;
                }
                DictionaryElem entryDictionary = dictionary.getElem(term);
                if (entryDictionary == null) {
                    dictionary.insertElem(new DictionaryElem(term));
                    postingLists.addPosting(term, new Posting(docId, 1));
                    numPosting++;

                } else {
                    entryDictionary.setCf(entryDictionary.getCf() + 1);
                    PostingList list = postingLists.getPostings(term);
                    List<Posting> Postings = list.getPostings();
                    Posting lastPosting = Postings.get(Postings.size() - 1);
                    if (lastPosting.getDocid() == docId) {
                        lastPosting.setFrequency(lastPosting.getFrequency() + 1);
                    } else {
                        entryDictionary.setDf(entryDictionary.getDf() + 1);
                        Postings.add(new Posting(docId, 1));
                        numPosting++;
                    }
                }





            }
            if (Runtime.getRuntime().totalMemory() > MaxUsableMemory) {
                System.out.printf("(INFO) MAXIMUM PERMITTED USE OF MEMORY ACHIEVED: WRITING BLOCK '%d' ON CURRENT DISC.\n", counterBlock);
                //write to disk and reset dictionary and Posting Lists
                //Serializzazione e scrittura su file poi si vede dove metterla, la metteremo in inverted index perchè si
                writeToDisk();
            }

            line = reader.readLine();
        }
        writeToDisk();
    }

    private static void writeToDisk() throws IOException, InterruptedException {
        //Serializzazione e scrittura su file poi si vede dove metterla, la metteremo in inverted index perchè si

        try (
                FileChannel docsFchan = (FileChannel) Files.newByteChannel(Paths.get("doc" + "_" + counterBlock),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE
                );
                FileChannel freqsFchan = (FileChannel) Files.newByteChannel(Paths.get("freq" + "_" + counterBlock),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE);
                FileChannel vocabularyFchan = (FileChannel) Files.newByteChannel(Paths.get("voc" + "_" + counterBlock),
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
            long vocOffset = 0;
            // check if MappedByteBuffers are correctly instantiated
            for (Map.Entry<String, PostingList>
                    entry : postingLists.getTree().entrySet()) {

                DictionaryElem dictionaryElem = dictionary.getElem(entry.getKey());
                dictionaryElem.setOffsetDoc(docsBuffer.position());
                dictionaryElem.setOffsetFreq(freqsBuffer.position());
                int counter = 0;
                for (Posting posting : entry.getValue().getPostings()) {
                    docsBuffer.putInt(posting.getDocid());
                    freqsBuffer.putInt(posting.getFrequency());
                    counter++;
                }
                dictionaryElem.setLength(counter);
                //allocate char buffer to write term
                CharBuffer charBuffer = CharBuffer.allocate(40); //TODO: verificare la size
                String term = dictionaryElem.getTerm();
                //populate char buffer char by char
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

                int lengthInBytes = truncatedBuffer.limit();
                truncatedBuffer.rewind();
                vocBuffer.put(truncatedBuffer);

                // write statistics
                vocBuffer.putInt(dictionaryElem.getDf());
                vocBuffer.putInt(dictionaryElem.getCf());
                vocBuffer.putLong(dictionaryElem.getOffsetDoc());
                vocBuffer.putLong(dictionaryElem.getOffsetFreq());
                vocBuffer.putInt(dictionaryElem.getLength());

            }
        }
        counterBlock++;
        postingLists.clear();
        dictionary.clear();
        dictionary = new Dictionary();
        postingLists = new InvertedIndex();
        numPosting = 0;
        System.gc();
        Thread.sleep(1500);

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



    public static void readDictionary(String path) {
        try (FileChannel vocFchan = (FileChannel) Files.newByteChannel(Paths.get(path),
                StandardOpenOption.READ)) {

            // Size for each term in bytes
            int termSize = 40;
            // Sizes for the other integers and longs
            int intSize = Integer.BYTES; // 4 bytes
            int longSize = Long.BYTES;   // 8 bytes
            // Total size of one dictionary entry
            int entrySize = termSize + 2 * intSize + 2 * longSize + intSize;

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
                    int length = buffer.getInt();

                    // Print the details
                    System.out.println("Term: " + term);
                    System.out.println("Document Frequency (df): " + df);
                    System.out.println("Collection Frequency (cf): " + cf);
                    System.out.println("Offset Doc: " + offsetDoc);
                    System.out.println("Offset Freq: " + offsetFreq);
                    System.out.println("Length: " + length);
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

    public static String decodeTerm(byte[] termBytes) {
        // Create a ByteBuffer from the byte array
        ByteBuffer termBuffer = ByteBuffer.wrap(termBytes);
        // Decode the ByteBuffer into a CharBuffer
        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(termBuffer);
        // Convert CharBuffer to String
        return charBuffer.toString().trim(); // Trim the string in case there are any zero padding bytes
    }


}

