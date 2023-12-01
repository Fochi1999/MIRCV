package it.unipi.mrcv.global;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;

public class Global {
    public static final String prefixDocFiles="doc_";
    public static final String prefixVocFiles="voc_";
    public static final String prefixFreqFiles="freq_";
    public static final String prefixDocIndex="docIndex";
    public static final String finalVoc="vocabulary";
    public static final String finalDoc="docIds";
    public static final String finalFreq="frequencies";
    public static final String skippingFile="skipping";
    public static final String finalVocCompressed="vocabularyCompressed";
    public static final String finalDocCompressed="docIdsCompressed";
    public static final String finalFreqCompressed="frequenciesCompressed";
    public static final String finalStopWordsFile="stopwords-en.txt";
    public static boolean compression=true;
    public static boolean stem=true;
    public static boolean stopWords=true;
    public static int collectionLength;
    public static double averageDocLength;
    public static HashSet<String> stopWordsSet;

    public static void load() {
        try {
            List<String> stopWordsList = Files.readAllLines(Paths.get(finalStopWordsFile));
            stopWordsSet = new HashSet<>(stopWordsList);
        } catch (IOException e) {
            System.out.println("Can't read Stopword file, flag stopwords set to false");
            stopWords = false;
        }
    }
}
