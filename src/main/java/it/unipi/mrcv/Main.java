package it.unipi.mrcv;

import it.unipi.mrcv.data_structures.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.unipi.mrcv.index.Merger;
import it.unipi.mrcv.index.MergerThreadTry;
import it.unipi.mrcv.index.SPIMI;
import it.unipi.mrcv.index.fileUtils;

import static it.unipi.mrcv.index.SPIMI.dictionary;
import static it.unipi.mrcv.index.SPIMI.postingLists;


// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        long startTime = System.nanoTime();
        fileUtils.deleteTempFiles();
        fileUtils.deleteFiles();
        SPIMI.exeSPIMI("collection.tsv");

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // Convert from nanoseconds to milliseconds

        System.out.println("Elapsed time in milliseconds: " + duration);

        //SPIMI.readIndex("doc_0");
        Merger.Merge();
        SPIMI.readDictionary("vocabulary");
       /* RandomAccessFile p=new RandomAccessFile("voc_0","r");
        p.seek(68*800); //set the pointer to 0
        Merger.readLineFromDictionary(p,0);*/
        //System.out.println(SPIMI.debugCounter);
        //SPIMI.readIndex("doc_0");
        /*List<Integer> filesN=new ArrayList<>();
        for(int i=0;i<SPIMI.counterBlock;i++){
            filesN.add(i);
        }
        for(int i=0;i<SPIMI.counterBlock;i=i+2){
            MergerThreadTry mg=new MergerThreadTry(filesN.subList(i,i+1),i*10+(i+1));
            mg.start();
        }
        */
    }
}

