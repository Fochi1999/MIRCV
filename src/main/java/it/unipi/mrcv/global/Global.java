package it.unipi.mrcv.global;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Global {
    public static final String prefixDocFiles="doc_";
    public static final String prefixVocFiles="voc_";
    public static final String prefixFreqFiles="freq_";
    public static final String finalVoc="vocabulary";
    public static final String finalDoc="docIds";
    public static final String finalFreq="frequencies";
    public static final String finalVocCompressed="vocabularyCompressed";
    public static final String finalDocCompressed="docIdsCompressed";
    public static final String finalFreqCompressed="frequenciesCompressed";
    public static final String finalStopWordsFile="stopwords-en.txt";
    public static boolean compression=true;
    public static boolean stem=false;
    public static boolean stopWords=false;

    public static List<String> stopWordsList;

    public static void load(){
        try {
            stopWordsList=Files.readAllLines(Paths.get(finalStopWordsFile));

        } catch (IOException e) {
            System.out.println("can't read Stopword file, flag stopwords set to false");
            stopWords=false;
        }
    }
}