package it.unipi.mrcv.data_structures;

import java.util.TreeMap;

public class Dictionary {
    private TreeMap<String, DictionaryElem> dictionary;

    // default constructor
    public Dictionary(){
        dictionary = new TreeMap<>();
    };

    // insert element in the dictionary
    public void insertElem(DictionaryElem elem){
        dictionary.put(elem.getTerm(), elem);
    };

    // return element from the dictionary
    public DictionaryElem getElem(String term){
        return  dictionary.get(term);
    }

    public long size() {
        //TODO: DA FARE
        return dictionary.size();

    }

    // TODO: save to file
}
