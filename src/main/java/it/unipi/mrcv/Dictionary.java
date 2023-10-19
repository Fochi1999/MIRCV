package src.main.java.org;

import java.util.HashMap;

public class Dictionary {
    private HashMap<String, DictionaryElem> dictionary;

    // default constructor
    public Dictionary(){
        dictionary = new HashMap<>();
    };

    // insert element in the dictionary
    public void insertElem(DictionaryElem elem){
        dictionary.put(elem.getTerm(), elem);
    };

    // return element from the dictionary
    public DictionaryElem getElem(String term){
        return  dictionary.get(term);
    }

    // TODO: save to file
}
