package edu.yu.cs.com1320.project.stage5.impl;

import com.google.gson.annotations.Expose;
import edu.yu.cs.com1320.project.stage5.Document;

import java.net.URI;
import java.util.*;
import java.net.URI;
import java.sql.SQLOutput;
import java.util.Set;

public class DocumentImpl implements Document {
    @Expose
    private final String text;
    @Expose
    private final byte[] binaryData;
    @Expose
    private final URI key;
    @Expose
    private Map<String,Integer> wordCounter;
    @Expose
    private String[] words;
    private long lastUsedTime;

    public DocumentImpl(URI uri, String txt)throws IllegalArgumentException{
        if(uri == null || uri.toString().equals("") || txt == null || txt.equals("")){
            throw new IllegalArgumentException("bad input");
        }
        this.text = txt;
        this.key = uri;
        this.binaryData = null;
        this.wordCounter = new HashMap<>();
        String alphaNumAndSpace = this.text.replaceAll("[^a-zA-Z0-9\\s+]","").toLowerCase();
        this.words = alphaNumAndSpace.split("\\s+");
        for(String s: this.words){
            if (wordCounter.containsKey(s)) {
                wordCounter.put(s,wordCounter.get(s) + 1);
            }else{
                wordCounter.put(s,1);
            }
        }
        this.lastUsedTime = 0;
    }
    public DocumentImpl(URI uri, byte[] binaryData)throws IllegalArgumentException{
        if(uri == null ||uri.toString().equals("")|| binaryData == null || binaryData.length <= 0){
            throw new IllegalArgumentException("bad input");
        }
        this.key= uri;
        this.binaryData= binaryData;
        this.text = null;
        this.lastUsedTime = 0;
    }

    public String getDocumentTxt() {
        return this.text;
    }


    public byte[] getDocumentBinaryData() {
        return this.binaryData;
    }

    public URI getKey() {
        return this.key;
    }
    @Override
    public boolean equals(Object other){
        if(other.getClass() != DocumentImpl.class){
            return false;
        }
        return this.hashCode() == other.hashCode();
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(binaryData);
        return result;
    }

    @Override
    public int wordCount(String word) {
        String alphaNumAndSpace = word.replaceAll("[^a-zA-Z0-9]","");
        String s = alphaNumAndSpace.toLowerCase();
        if (this.wordCounter ==null){
            return 0;
        }
        if(wordCounter.get(s) != null){
            return wordCounter.get(s);
        }
        return 0;
    }

    @Override
    public Set<String> getWords() {
        if(this.text == null){
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(this.words));
    }

    @Override
    public long getLastUseTime() {
        return this.lastUsedTime;
    }

    @Override
    public void setLastUseTime(long timeInNanoseconds) {
        this.lastUsedTime = timeInNanoseconds;
    }

    @Override
    public Map<String, Integer> getWordMap() {
        return this.wordCounter;
    }

    @Override
    public void setWordMap(Map<String, Integer> wordMap) {
        this.wordCounter = (HashMap<String, Integer>) wordMap;

    }

    @Override
    public int compareTo(Document o) {
        if(this.lastUsedTime > o.getLastUseTime()){
            return 1;
        }else if(o.getLastUseTime()> this.lastUsedTime){
            return -1;
        }
        return 0;
    }
}