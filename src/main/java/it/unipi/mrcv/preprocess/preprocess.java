package it.unipi.mrcv.preprocess;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.SimpleTokenizer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class preprocess {
    public static List<String> all(String text){
        text=removePuntuaction(text);
        text=lowercase(text);
        text=text.replaceAll("\\s+", " "); //remove extra whitespaces

        text=removeUnicode(text);
        //if flag allora tokenStem else tokeniza e basta
        //return stem(text);
        return tokenize(text);
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

    public static List<String> tokenize(String text){

        return Stream.of(text.toLowerCase().split(" "))
                .collect(Collectors.toCollection(ArrayList<String>::new));
    }
    public static String lowercase(String text){
        return text.toLowerCase();
    }

    public static String removePuntuaction(String text){
        String result = text.replaceAll("\\p{Punct}", " ");
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

    public static String removeUnicode(String text){
        String str;
        byte[] strBytes = text.getBytes(StandardCharsets.UTF_8);

        str = new String(strBytes, StandardCharsets.UTF_8);

        Pattern unicodeOutliers = Pattern.compile("[^\\x00-\\x7F]",
                Pattern.UNICODE_CASE | Pattern.CANON_EQ
                        | Pattern.CASE_INSENSITIVE);

        Matcher unicodeOutlierMatcher = unicodeOutliers.matcher(str);
        str = unicodeOutlierMatcher.replaceAll(" ");

        return str;
    }
}
