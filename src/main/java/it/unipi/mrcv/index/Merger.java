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

//java class that merges the partial indexes composed of the indexes with the docids and the indexes with the frequencies
public class Merger {
    // number of blocks produced by the SPIMI algorithm
    public static int num_blocks = SPIMI.counterBlock;


    public static void Merge() throws IOException {
        // temporaryElem contains the final vocabulary entry that is being build
        termBlock temporaryElem = new termBlock();
        // currentBlock contains the current vocabulary entry that is being read from a partial vocabulary
        termBlock currentBlock;
        // termBytes is a buffer used to read the term from the vocabulary
        byte[] termBytes = new byte[40];
        // docOff and freqOff are used to store the increasing offset at each new docId or frequency
        long docOff = 0;
        long freqOff = 0;
        // latestDocOff and latestFreqOff are used to store the offset of the latest vocabulary entry
        long latestDocOff;
        long latestFreqOff;
        // termNumber is used to store the number of the vocabulary entry, used to write the vocabulary at the correct position
        long termNumber = 0;
        // temporaryDocIds and temporaryFreqs are used to store the docIds and frequencies of the current vocabulary entry
        List<Integer> temporaryDocIds = new ArrayList<>();
        List<Integer> temporaryFreqs = new ArrayList<>();
        // pQueueElems is used to store the first vocabulary entry of each partial vocabulary, all added to the priority queue
        List<termBlock> pQueueElems = new ArrayList<>();
        // docsBuffer, freqsBuffer and vocBuffer are used to write the docIds, frequencies and vocabulary entries in the final files
        MappedByteBuffer docsBuffer;
        MappedByteBuffer freqsBuffer;
        MappedByteBuffer vocBuffer;
        // docPointers, freqPointers and vocPointers are used to store the pointers to the partial vocabularies
        List<RandomAccessFile> docPointers = new ArrayList<>();
        List<RandomAccessFile> freqPointers = new ArrayList<>();
        List<RandomAccessFile> vocPointers = new ArrayList<>();
        // pQueue is the priority queue used to store the vocabulary entries
        PriorityQueue<termBlock> pQueue = new PriorityQueue<termBlock>(num_blocks, new ComparatorTerm());

        // initialize the pointers to the partial vocabularies and the priority queue
        for (int i = 0; i < num_blocks; i++) {
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

                readEntryFromDictionary(p, i, pQueueElems.get(i), termBytes);
                pQueue.add(pQueueElems.get(i));

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        // initialize the final files
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

            // the whole merge is done in this while loop
            while (!pQueue.isEmpty()) {
                // currentBlock is the vocabulary entry that is being read from the priority queue
                currentBlock = pQueue.poll();
                temporaryElem.copyBlock(currentBlock);
                String term = currentBlock.getTerm();
                // the offsets of the docIds and frequencies are updated to the starting offset of the current term
                latestFreqOff = freqOff;
                latestDocOff = docOff;
                // the docIds and frequencies of the current term are read from the block where the term was found and added to the temporary lists
                readLineFromDocId(docPointers.get(temporaryElem.getNumBlock()), temporaryElem.getDictionaryElem().getLength(), temporaryDocIds);
                readLineFromFreq(freqPointers.get(temporaryElem.getNumBlock()), temporaryElem.getDictionaryElem().getLength(), temporaryFreqs);
                // if the block is not finished, the next vocabulary entry is read and added to the priority queue
                if (!isEndOfFile(vocPointers.get(currentBlock.getNumBlock()))) {
                    readEntryFromDictionary(vocPointers.get(currentBlock.getNumBlock()), currentBlock.getNumBlock(), pQueueElems.get(currentBlock.getNumBlock()), termBytes);
                    pQueue.add(pQueueElems.get(currentBlock.getNumBlock()));
                }

                // the priority queue is checked to see if there are other vocabulary entries with the same term
                while (!pQueue.isEmpty() && term.equals(pQueue.peek().getTerm())) {
                    // the read from the queue every element with the same term and update the statistics of the temporaryElem
                    currentBlock = pQueue.poll();
                    int blockNumber = currentBlock.getNumBlock();
                    temporaryElem.getDictionaryElem().setDf(temporaryElem.getDictionaryElem().getDf() + currentBlock.getDictionaryElem().getDf());
                    temporaryElem.getDictionaryElem().setCf(temporaryElem.getDictionaryElem().getCf() + currentBlock.getDictionaryElem().getCf());
                    temporaryElem.getDictionaryElem().setLength(temporaryElem.getDictionaryElem().getLength() + currentBlock.getDictionaryElem().getLength());
                    readLineFromDocId(docPointers.get(blockNumber), currentBlock.getDictionaryElem().getLength(), temporaryDocIds);
                    readLineFromFreq(freqPointers.get(blockNumber), currentBlock.getDictionaryElem().getLength(), temporaryFreqs);
                    // if the block is not finished, the next vocabulary entry is read and added to the priority queue
                    if (isEndOfFile(vocPointers.get(blockNumber))) {
                        continue;
                    }
                    readEntryFromDictionary(vocPointers.get(blockNumber), blockNumber, pQueueElems.get(blockNumber), termBytes);
                    pQueue.add(pQueueElems.get(blockNumber));
                }

                // the offsets are updated to the starting offset of the next term
                docOff += temporaryElem.getDictionaryElem().getLength() * 4;
                freqOff += temporaryElem.getDictionaryElem().getLength() * 4;


                // the buffers are mapped to the correct position in the final files
                docsBuffer = docsFchan.map(FileChannel.MapMode.READ_WRITE, latestDocOff, temporaryDocIds.size() * 4);
                freqsBuffer = freqsFchan.map(FileChannel.MapMode.READ_WRITE, latestFreqOff, temporaryFreqs.size() * 4);
                vocBuffer = vocabularyFchan.map(FileChannel.MapMode.READ_WRITE, termNumber * 68, DictionaryElem.size());
                termNumber++;
                // write docIds and frequencies in the final index files
                for (int i = 0; i < temporaryElem.getDictionaryElem().getLength(); i++) {
                    docsBuffer.putInt(temporaryDocIds.get(i));
                    freqsBuffer.putInt(temporaryFreqs.get(i));
                }

                // write term in the final vocabulary file
                CharBuffer charBuffer = CharBuffer.allocate(40);
                for (int i = 0; i < term.length() && i < 40; i++)
                    charBuffer.put(i, term.charAt(i));
                ByteBuffer truncatedBuffer = ByteBuffer.allocate(40); // Allocate buffer for 40 bytes
                ByteBuffer encodedBuffer = StandardCharsets.UTF_8.encode(charBuffer);
                encodedBuffer.rewind();
                for (int i = 0; i < 40; i++) {
                    truncatedBuffer.put(encodedBuffer.get(i));
                }
                truncatedBuffer.rewind();
                vocBuffer.put(truncatedBuffer);
                // write statistics into the final vocabulary file
                vocBuffer.putInt(temporaryElem.getDictionaryElem().getDf());
                vocBuffer.putInt(temporaryElem.getDictionaryElem().getCf());
                vocBuffer.putLong(latestDocOff);
                vocBuffer.putLong(latestFreqOff);
                vocBuffer.putInt(temporaryElem.getDictionaryElem().getLength());

                // clear the temporary lists
                temporaryDocIds.clear();
                temporaryFreqs.clear();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    // method to read an entry from a vocabulary using the input array for the term and writing the entry in the input termBlock
    public static void readEntryFromDictionary(RandomAccessFile raf, int n, termBlock readBlock, byte[] termBytes) throws IOException {
        // Read 40 bytes into a byte array
        ByteBuffer vocBuffer = ByteBuffer.allocate(DictionaryElem.size());
        while (vocBuffer.hasRemaining()) {
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

        // Set the values of the termBlock
        readBlock.setNumBlock(n);
        readBlock.getDictionaryElem().setTerm(SPIMI.decodeTerm(termBytes));
        readBlock.getDictionaryElem().setDf(int1);
        readBlock.getDictionaryElem().setCf(int2);
        readBlock.getDictionaryElem().setOffsetDoc(long1);
        readBlock.getDictionaryElem().setOffsetFreq(long2);
        readBlock.getDictionaryElem().setLength(int3);
    }

    // method to read a line of docIds from a file and write it in the input list
    public static void readLineFromDocId(RandomAccessFile raf, int length, List<Integer> ids) throws IOException {
        ByteBuffer docsIdsBuffer = ByteBuffer.allocate(length * 4);
        while (docsIdsBuffer.hasRemaining()) {
            raf.getChannel().read(docsIdsBuffer);
        }
        docsIdsBuffer.rewind();
        for (int i = 0; i < length; i++) {
            ids.add(docsIdsBuffer.getInt());
        }
    }

    // method to read a line of frequencies from a file and write it in the input list
    public static void readLineFromFreq(RandomAccessFile raf, int length, List<Integer> freqs) throws IOException {
        ByteBuffer freqsBuffer = ByteBuffer.allocate(length * 4);
        while (freqsBuffer.hasRemaining()) {
            raf.getChannel().read(freqsBuffer);
        }
        freqsBuffer.rewind();
        for (int i = 0; i < length; i++) {
            freqs.add(freqsBuffer.getInt());
        }
    }

    // method to check if the pointer of a file is at the end of the file
    public static boolean isEndOfFile(RandomAccessFile raf) throws IOException {
        return raf.getFilePointer() == raf.length();
    }


}
