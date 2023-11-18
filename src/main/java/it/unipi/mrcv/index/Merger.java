package it.unipi.mrcv.index;

import it.unipi.mrcv.data_structures.DictionaryElem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Merger {
    //java class that merges the partial indexes composed of the indexes with the docids and the indexes with the frequencies
    //apri tutti i file voc_x e tieni un puntatore per ogni file, leggi l'elemento in ordine alfabetico che viene prima
    public static int num_blocks = 8;
    // public static int num_blocks = SPIMI.counterBlock;


    public static void Merge() throws IOException {
        DictionaryElem temporaryElem;
        long docOff = 0;
        long freqOff = 0;
        long prevDocOff = 0;
        long prevFreqOff = 0;
        long termNumber = 0;
        RandomAccessFile docPointer;
        RandomAccessFile frePointer;
        MappedByteBuffer docsBuffer;
        MappedByteBuffer freqsBuffer;
        MappedByteBuffer vocBuffer;
        List<RandomAccessFile> docPointers = new ArrayList<>();
        List<RandomAccessFile> freqPointers = new ArrayList<>();
        List<RandomAccessFile> vocPointers = new ArrayList<>();
        PriorityQueue<termBlock> pQueue = new PriorityQueue<termBlock>(num_blocks, new ComparatorTerm());

        //inizializzazione
        for (int i = 0; i < num_blocks; i++) {
            //leggi il primo risultato di ogni blocco
            try {
                RandomAccessFile p = new RandomAccessFile(fileUtils.prefixVocFiles + i, "r");
                p.seek(0); //set the pointer to 0
                vocPointers.add(p);
                RandomAccessFile d = new RandomAccessFile("doc_" + i, "r");
                p.seek(0); //set the pointer to 0
                docPointers.add(d);
                RandomAccessFile q = new RandomAccessFile("freq_" + i, "r");
                p.seek(0); //set the pointer to 0
                freqPointers.add(q);


                pQueue.add(readLineFromDictionary(p, i));


            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        //ora codice
        try (
                FileChannel docsFchan = (FileChannel) Files.newByteChannel(Paths.get(fileUtils.finalDoc),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE);
                FileChannel freqsFchan = (FileChannel) Files.newByteChannel(Paths.get(fileUtils.finalFreq),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE);
                FileChannel vocabularyFchan = (FileChannel) Files.newByteChannel(Paths.get(fileUtils.finalVoc),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE)) {
            while(!pQueue.isEmpty()) {
                List<Integer> temporaryDocIds = new ArrayList<>();
                List<Integer> temporaryFreqs = new ArrayList<>();
                List<termBlock> termBlockList = new ArrayList<>();

                termBlockList.add(pQueue.poll());
                String term = termBlockList.get(0).getTerm();
                while (!pQueue.isEmpty() && term.equals(pQueue.peek().getTerm())) {
                    termBlockList.add(pQueue.poll());
                }

                temporaryElem = new DictionaryElem(term);
                //set to 0 all attributes
                temporaryElem.setDf(0);
                temporaryElem.setCf(0);
                temporaryElem.setOffsetDoc(docOff);
                temporaryElem.setOffsetFreq(freqOff);
                //ripienare la pque con i nuovi termini, calcolare le statistiche e scrivere su un file
                prevFreqOff=freqOff;
                prevDocOff=docOff;
                for (termBlock tb : termBlockList) {
                    int blockNumber = tb.getNumBlock();
                    docPointer = new RandomAccessFile(fileUtils.prefixDocFiles + blockNumber, "r");
                    frePointer = new RandomAccessFile(fileUtils.prefixFreqFiles + blockNumber, "r");
                    temporaryElem.setDf(temporaryElem.getDf() + tb.getDictionaryElem().getDf());
                    temporaryElem.setCf(temporaryElem.getCf() + tb.getDictionaryElem().getCf());
                    temporaryElem.setLength(temporaryElem.getLength() + tb.getDictionaryElem().getLength());
                    temporaryDocIds.addAll(readLineFromDocId(docPointers.get(blockNumber), tb.getDictionaryElem().getLength()));
                    temporaryFreqs.addAll(readLineFromFreq(freqPointers.get(blockNumber), tb.getDictionaryElem().getLength()));
                    //scrittura su file

                    docOff += tb.getDictionaryElem().getLength() * 4;
                    freqOff += tb.getDictionaryElem().getLength() * 4;
                    if (isEndOfFile(vocPointers.get(blockNumber))) {
                        continue;
                    }
                    pQueue.add(readLineFromDictionary(vocPointers.get(blockNumber), blockNumber));

                }
                //write temporaryElem in DefiniteDictionary
                docsBuffer = docsFchan.map(FileChannel.MapMode.READ_WRITE, prevDocOff, temporaryDocIds.size() * 4);
                freqsBuffer = freqsFchan.map(FileChannel.MapMode.READ_WRITE, prevFreqOff, temporaryFreqs.size() * 4);
                vocBuffer = vocabularyFchan.map(FileChannel.MapMode.READ_WRITE, termNumber*68, DictionaryElem.size());
                termNumber++;
                for(int i=0;i<temporaryElem.getLength();i++){
                    docsBuffer.putInt(temporaryDocIds.get(i));
                    freqsBuffer.putInt(temporaryFreqs.get(i));
                }


                CharBuffer charBuffer = CharBuffer.allocate(40);
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
                truncatedBuffer.rewind();
                vocBuffer.put(truncatedBuffer);

                // write statistics
                vocBuffer.putInt(temporaryElem.getDf());
                vocBuffer.putInt(temporaryElem.getCf());
                vocBuffer.putLong(temporaryElem.getOffsetDoc());
                vocBuffer.putLong(temporaryElem.getOffsetFreq());
                vocBuffer.putInt(temporaryElem.getLength());
                //svuotare termblocklist e dictionaryElem
                temporaryDocIds.clear();
                temporaryFreqs.clear();
                
            } //fine while
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static termBlock readLineFromDictionary(RandomAccessFile raf, int n) throws IOException {
        // Allocate a buffer for the first 40 bytes

        byte[] termBytes = new byte[40];
        raf.readFully(termBytes);

        // Read next 4 bytes into an int
        int int1 = raf.readInt();

        // Read next 4 bytes into an int
        int int2 = raf.readInt();

        // Read next 8 bytes into a long
        long long1 = raf.readLong();

        // Read next 8 bytes into a long
        long long2 = raf.readLong();

        // Read next 4 bytes into an int
        int int3 = raf.readInt();

        // For demonstration purposes: print out the values
/*        System.out.println("ByteBuffer contents: " + SPIMI.decodeTerm(termBytes));
        System.out.println("First int: " + int1);
        System.out.println("Second int: " + int2);
        System.out.println("First long: " + long1);
        System.out.println("Second long: " + long2);
        System.out.println("Third int: " + int3);*/


        return new termBlock(SPIMI.decodeTerm(termBytes), int1, int2, long1, long2, int3, n);
    }

    public static List<Integer> readLineFromDocId(RandomAccessFile raf, int length) throws IOException {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            ids.add(raf.readInt());
        }
        return ids;
    }

    public static List<Integer> readLineFromFreq(RandomAccessFile raf, int length) throws IOException {
        List<Integer> freqs = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            freqs.add(raf.readInt());
        }
        return freqs;
    }

    public static boolean isEndOfFile(RandomAccessFile raf) throws IOException {
        return raf.getFilePointer() == raf.length();
    }


}
