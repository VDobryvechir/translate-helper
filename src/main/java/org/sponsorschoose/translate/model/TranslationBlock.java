package org.sponsorschoose.translate.model;

import java.util.*;

public class TranslationBlock {
    
    private String id;
    private List<String> words;
    private String text;

    public TranslationBlock() {
    }

    public TranslationBlock(String id,List<String> words, String text) {
       this.id = id;
       this.words = words;
       this.text = text; 
    }

    public String getId() {
        return id;
    }

    public List<String> getWords() {
        return words;
    }

    public String getText() {
        return text;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }

    public void setText(String text) {
        this.text = text;
    }

}