package it.unipi.mrcv;

import it.unipi.mrcv.compression.Unary;
import it.unipi.mrcv.compression.VariableByte;
import it.unipi.mrcv.index.Merger;
import it.unipi.mrcv.index.SPIMI;

import java.io.IOException;
import java.util.ArrayList;


// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        long startTime = System.currentTimeMillis(); // Capture start time

        SPIMI.readDictionaryToFile("vocabularyCompressed", "vocz.txt");

        long endTime = System.currentTimeMillis(); // Capture end time

        // Calculate the elapsed time and convert it to minutes
        long elapsedTimeMillis = endTime - startTime;
        double elapsedTimeMinutes = elapsedTimeMillis / 1000.0 / 60.0;

        // Print the execution time in minutes
        System.out.printf("Execution time: %.2f minutes\n", elapsedTimeMinutes);
        //SPIMI.readCompressedDic("vocabulary","docids");
        //SPIMI.readIndex("frequencies");
    }
}

