package it.unipi.mrcv.preprocess;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.SimpleTokenizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class preprocess {
    public static List<String> all(String text){
        text=removePuntuaction(text);
        text=lowercase(text);
        return stem(text);
    }
    public static List<String> stem(String text){
        String[] tokens = SimpleTokenizer.INSTANCE.tokenize(text);
        PorterStemmer porterStemmer = new PorterStemmer();
        List<String> ret=new ArrayList<>();
        for (String token : tokens) {
            String stem = porterStemmer.stem(token);
            //System.out.println("Token: " + token + " - Stem: " + stem);
            ret.add(stem);
        }
        return ret;
    }
    public static String lowercase(String text){
        return text.toLowerCase();
    }

    public static String removePuntuaction(String text){
        String result = text.replaceAll("\\p{Punct}", "");
        return result;
    }

    public static String StopWordRemoval(List<String> stopwordsList, List<String> words){
        HashSet<String> stopwords = new HashSet<>(stopwordsList);
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!stopwords.contains(word)) {
                result.append(word).append(" ");
            }
        }
        return result.toString().trim();
    }
}
