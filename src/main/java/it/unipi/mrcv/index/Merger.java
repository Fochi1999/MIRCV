package it.unipi.mrcv.index;

import it.unipi.mrcv.compression.Unary;
import it.unipi.mrcv.compression.VariableByte;
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
    public static boolean compression=true;

    public static void Merge() throws IOException {
        termBlock temporaryElem = new termBlock();
        termBlock currentBlock;
        termBlock previousBlock;
        byte[] termBytes = new byte[40];
        long docOff = 0;
        long freqOff = 0;
        long prevDocOff = 0;
        long prevFreqOff = 0;
        long termNumber = 0;
        List<Integer> temporaryDocIds = new ArrayList<>();
        List<Integer> temporaryFreqs = new ArrayList<>();
        List<termBlock> pQueueElems = new ArrayList<>();
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
                RandomAccessFile p = new RandomAccessFile("voc_" + i, "r");
                p.seek(0); //set the pointer to 0
                vocPointers.add(p);
                RandomAccessFile d = new RandomAccessFile("doc_" + i, "r");
                p.seek(0); //set the pointer to 0
                docPointers.add(d);
                RandomAccessFile q = new RandomAccessFile("freq_" + i, "r");
                p.seek(0); //set the pointer to 0
                freqPointers.add(q);

                pQueueElems.add(new termBlock());

                readLineFromDictionary(p, i, pQueueElems.get(i) , termBytes);
                pQueue.add(pQueueElems.get(i));


            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        //ora codice
        try (
                FileChannel docsFchan = (FileChannel) Files.newByteChannel(Paths.get("docIds"),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE);
                FileChannel freqsFchan = (FileChannel) Files.newByteChannel(Paths.get("frequencies"),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE);
                FileChannel vocabularyFchan = (FileChannel) Files.newByteChannel(Paths.get("vocabulary"),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ,
                        StandardOpenOption.CREATE)) {

            while(!pQueue.isEmpty()) {

                previousBlock = pQueue.poll();
                temporaryElem.copyBlock(previousBlock);
                String term = previousBlock.getTerm();

                prevFreqOff=freqOff;
                prevDocOff=docOff;

                readLineFromDocId(docPointers.get(previousBlock.getNumBlock()), previousBlock.getDictionaryElem().getLength(), temporaryDocIds);
                readLineFromFreq(freqPointers.get(previousBlock.getNumBlock()), previousBlock.getDictionaryElem().getLength(), temporaryFreqs);

                if (!isEndOfFile(vocPointers.get(previousBlock.getNumBlock()))) {
                    readLineFromDictionary(vocPointers.get(previousBlock.getNumBlock()), previousBlock.getNumBlock(), pQueueElems.get(previousBlock.getNumBlock()) , termBytes);
                    pQueue.add(pQueueElems.get(previousBlock.getNumBlock()));
                }

                while (!pQueue.isEmpty() && term.equals(pQueue.peek().getTerm())) {
                    currentBlock = pQueue.poll();
                    int blockNumber = currentBlock.getNumBlock();
                    temporaryElem.getDictionaryElem().setDf(temporaryElem.getDictionaryElem().getDf() + currentBlock.getDictionaryElem().getDf());
                    temporaryElem.getDictionaryElem().setCf(temporaryElem.getDictionaryElem().getCf() + currentBlock.getDictionaryElem().getCf());
                    temporaryElem.getDictionaryElem().setLength(temporaryElem.getDictionaryElem().getLength() + currentBlock.getDictionaryElem().getLength());
                    readLineFromDocId(docPointers.get(blockNumber), currentBlock.getDictionaryElem().getLength(), temporaryDocIds);
                    readLineFromFreq(freqPointers.get(blockNumber), currentBlock.getDictionaryElem().getLength(), temporaryFreqs);

                    if (isEndOfFile(vocPointers.get(blockNumber))) {
                        continue;
                    }
                    readLineFromDictionary(vocPointers.get(blockNumber), blockNumber, pQueueElems.get(blockNumber) , termBytes);
                    pQueue.add(pQueueElems.get(blockNumber));
                }



                if(compression==true){

                    byte[] temporaryDocIdsBytes= VariableByte.fromArrayIntToVarByte((ArrayList<Integer>) temporaryDocIds);
                    byte[] temporaryFreqsBytes= Unary.ArrayIntToUnary((ArrayList<Integer>) temporaryFreqs);
                    docOff += temporaryDocIdsBytes.length;
                    freqOff += temporaryFreqsBytes.length;
                    /*System.out.println("\nSTO SCRIVENDO COMPRESSO LA LISTA: ");
                    for(int x: temporaryDocIds){
                        System.out.print(x+" ");
                    }
                    System.out.println("");
                    System.out.println("lunghezza:"+temporaryDocIdsBytes.length+"\nCOMPRESSA:");
                    for(long x:VariableByte.fromByteToArrayLong(temporaryDocIdsBytes)){
                        System.out.print(x+" ");
                    }

                    System.out.println("\nSTO SCRIVENDO COMPRESSO LA LISTA: ");
                    for(int x: temporaryFreqs){
                        System.out.print(x+" ");
                    }
                    System.out.println("");
                    System.out.println("lunghezza:"+temporaryFreqsBytes.length+"\nCOMPRESSA:");
                    for(int x:Unary.unaryToArrayInt(temporaryFreqsBytes)){
                        System.out.print(x+" ");
                    }*/
                    docsBuffer = docsFchan.map(FileChannel.MapMode.READ_WRITE, prevDocOff, temporaryDocIdsBytes.length);
                    freqsBuffer = freqsFchan.map(FileChannel.MapMode.READ_WRITE, prevFreqOff, temporaryFreqsBytes.length);
                    docsBuffer.put(temporaryDocIdsBytes);
                    freqsBuffer.put(temporaryFreqsBytes);
                    temporaryElem.getDictionaryElem().setLength(temporaryDocIdsBytes.length); //TODO cambiare lenght in più lenght

                }
                else {
                    docOff += temporaryElem.getDictionaryElem().getLength() * 4;
                    freqOff += temporaryElem.getDictionaryElem().getLength() * 4;
                    //write temporaryElem.getDictionaryElem() in DefiniteDictionary
                    docsBuffer = docsFchan.map(FileChannel.MapMode.READ_WRITE, prevDocOff, temporaryDocIds.size() * 4);
                    freqsBuffer = freqsFchan.map(FileChannel.MapMode.READ_WRITE, prevFreqOff, temporaryFreqs.size() * 4);
                    for (int i = 0; i < temporaryElem.getDictionaryElem().getLength(); i++) {
                        docsBuffer.putInt(temporaryDocIds.get(i));
                        freqsBuffer.putInt(temporaryFreqs.get(i));
                    }
                }
                vocBuffer = vocabularyFchan.map(FileChannel.MapMode.READ_WRITE, termNumber * 68, DictionaryElem.size());
                termNumber++;

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
                vocBuffer.putInt(temporaryElem.getDictionaryElem().getDf());
                vocBuffer.putInt(temporaryElem.getDictionaryElem().getCf());
                vocBuffer.putLong(prevDocOff);
                vocBuffer.putLong(prevFreqOff);
                vocBuffer.putInt(temporaryElem.getDictionaryElem().getLength());
                //svuotare termblocklist e dictionaryElem
                temporaryDocIds.clear();
                temporaryFreqs.clear();

            } //fine while
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void readLineFromDictionary(RandomAccessFile raf, int n, termBlock readBlock, byte[] termBytes) throws IOException {
        // Allocate a buffer for the first 40 bytes
        ByteBuffer vocBuffer = ByteBuffer.allocate(DictionaryElem.size());
        while(vocBuffer.hasRemaining()) {
            raf.getChannel().read(vocBuffer);
        }
        vocBuffer.rewind();
        vocBuffer.get(termBytes, 0, 40);

        // Read next 4 bytes into an int
        int int1 = vocBuffer.getInt();

        // Read next 4 bytes into an int
        int int2 = vocBuffer.getInt();

        // Read next 8 bytes into a long
        long long1 = vocBuffer.getLong();

        // Read next 8 bytes into a long
        long long2 = vocBuffer.getLong();

        // Read next 4 bytes into an int
        int int3 = vocBuffer.getInt();

        // For demonstration purposes: print out the values
/*        System.out.println("ByteBuffer contents: " + SPIMI.decodeTerm(termBytes));
        System.out.println("First int: " + int1);
        System.out.println("Second int: " + int2);
        System.out.println("First long: " + long1);
        System.out.println("Second long: " + long2);
        System.out.println("Third int: " + int3);*/

        readBlock.setNumBlock(n);
        readBlock.getDictionaryElem().setTerm(SPIMI.decodeTerm(termBytes));
        readBlock.getDictionaryElem().setDf(int1);
        readBlock.getDictionaryElem().setCf(int2);
        readBlock.getDictionaryElem().setOffsetDoc(long1);
        readBlock.getDictionaryElem().setOffsetFreq(long2);
        readBlock.getDictionaryElem().setLength(int3);
    }

    public static void readLineFromDocId(RandomAccessFile raf, int length, List<Integer> ids) throws IOException {
        ByteBuffer docsIdsBuffer = ByteBuffer.allocate(length * 4);
        while(docsIdsBuffer.hasRemaining()) {
            raf.getChannel().read(docsIdsBuffer);
        }
        docsIdsBuffer.rewind();
        for(int i=0;i<length;i++){
            ids.add(docsIdsBuffer.getInt());
        }
    }

    public static void readLineFromFreq(RandomAccessFile raf, int length, List<Integer> freqs) throws IOException {
        ByteBuffer freqsBuffer = ByteBuffer.allocate(length * 4);
        while(freqsBuffer.hasRemaining()) {
            raf.getChannel().read(freqsBuffer);
        }
        freqsBuffer.rewind();
        for(int i=0;i<length;i++){
            freqs.add(freqsBuffer.getInt());
        }
    }

    public static boolean isEndOfFile(RandomAccessFile raf) throws IOException {
        return raf.getFilePointer() == raf.length();
    }


}
