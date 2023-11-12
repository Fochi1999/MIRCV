package it.unipi.mrcv.index;

import it.unipi.mrcv.data_structures.DictionaryElem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Merger {
    //java class that merges the partial indexes composed of the indexes with the docids and the indexes with the frequencies
    //apri tutti i file voc_x e tieni un puntatore per ogni file, leggi l'elemento in ordine alfabetico che viene prima
   public static int num_blocks=SPIMI.counterBlock;


   public static void Merge() {
       List<RandomAccessFile> pointers=new ArrayList<>();

       PriorityQueue<termBlock> pQueue = new PriorityQueue<termBlock>(num_blocks,new ComparatorTerm());

       for(int i=0;i<num_blocks;i++){
           //leggi il primo risultato di ogni blocco
           try {
               RandomAccessFile p=new RandomAccessFile("voc_"+i,"r");
               p.seek(0); //set the pointer to 0
               pointers.add(p);

               pQueue.add(readLineFromDictionary(p,i));


           } catch (FileNotFoundException e) {
               throw new RuntimeException(e);
           } catch (IOException e) {
               throw new RuntimeException(e);
           }
       }
       //ora codice
       termBlock termBlock=pQueue.peek();
       String term=termBlock.getTerm();


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
       System.out.println("ByteBuffer contents: " + SPIMI.decodeTerm(termBytes));
       System.out.println("First int: " + int1);
       System.out.println("Second int: " + int2);
       System.out.println("First long: " + long1);
       System.out.println("Second long: " + long2);
       System.out.println("Third int: " + int3);
       raf.seek(raf.getFilePointer()+68);

       return new termBlock(SPIMI.decodeTerm(termBytes),n);
   }
    public static boolean isEndOfFile(RandomAccessFile raf) throws IOException {
        return raf.getFilePointer() == raf.length();
    }



}
