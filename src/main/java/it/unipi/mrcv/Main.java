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
import it.unipi.mrcv.index.SPIMI;

import static it.unipi.mrcv.index.SPIMI.dictionary;
import static it.unipi.mrcv.index.SPIMI.postingLists;


// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        long startTime = System.nanoTime();

        // SPIMI.exeSPIMI("collection.tsv");
        Merger.Merge();

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // Convert from nanoseconds to milliseconds
        System.out.println("Elapsed time in milliseconds: " + duration);



    }
}

