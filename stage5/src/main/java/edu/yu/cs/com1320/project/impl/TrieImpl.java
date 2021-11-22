package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;

import java.util.*;
import java.util.Comparator;

public class TrieImpl<Value> implements Trie<Value> {

    private static final int alphabetSize = 36;
    private Node<Value> root;
    private final int num = 48;
    private final int let= 87;

    private static class Node<Value>{
        protected Set<Value> valueSet;
        protected Node<Value>[] links = new Node[alphabetSize];
        protected char character;

        private Node(){
            this.valueSet = new HashSet<>();
        }


    }

    public TrieImpl(){
        this.root = null;
    }

    @Override
    public void put(String key, Value val) {
        if(key == null){
            throw new IllegalArgumentException();
        }
        if(key.length()==0 || val == null){
            return;
        }
        String lowercase = key.toLowerCase();
        this.root = put(this.root, lowercase, val, 0);
    }
    private Node<Value> put(Node<Value> x, String str, Value val, int d){
        if(x == null){
            x = new Node<>();
        }
        if(d == str.length()){
            x.valueSet.add(val);
            return x;
        }
        x.character = str.charAt(d);
        if(x.character < 97){

            x.links[x.character - num] = this.put(x.links[x.character- num],str,val,d+1);
        }else{
            x.links[x.character - let] = this.put(x.links[x.character- let],str,val,d+1);
        }
        return x;
    }

    @Override
    public List<Value> getAllSorted(String key, Comparator<Value> comparator) {
        if(comparator == null || key == null){
            throw new IllegalArgumentException();
        }
        String lowercase = key.toLowerCase();
        Node<Value> x = this.get(this.root,lowercase,0);
        //find out if this is null, will it return an empty set?
        if(x == null || x.valueSet.isEmpty()){
            return new ArrayList<>();
        }else{
            List<Value> sortedList = new ArrayList<>(x.valueSet);
            sortedList.sort(comparator);
            return sortedList;
        }

    }


    private Node<Value> get(Node<Value> x, String key, int d)
    {
        //link was null - return null, indicating a miss
        if (x == null)
        {
            return null;
        }
        //we've reached the last node in the key,
        //return the node
        if (d == key.length())
        {
            return x;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        char c = key.charAt(d);
        if(c < 97){
            return this.get(x.links[c-num], key, d + 1);
        }else{
            return this.get(x.links[c-let], key, d + 1);
        }

    }

    @Override
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator) {
        if (prefix == null || comparator == null) {
            throw new IllegalArgumentException();
        }
        String lowercase = prefix.toLowerCase();
        List<Value> prefixList = new ArrayList<>();
        if(prefix.length()==0){
            return prefixList;
        }
        getAllWithPrefix(this.root,lowercase,0,prefixList);
        HashSet<Value> set = new HashSet<Value>(prefixList);
        if(!prefixList.isEmpty()){
            List<Value> toReturn = new ArrayList<>(set);
            toReturn.sort(comparator);
            return toReturn;
        }
        return prefixList;

    }
    private Node<Value> getAllWithPrefix(Node<Value> x, String str, int j,List<Value> list){
        if(x == null){
            return null;
        }
        if(str.length() == j){
            this.getKids(x,list);
            return x;
        }else{
            char c = str.charAt(j);
            if(c < 97){
                x.links[c-num] = getAllWithPrefix(x.links[c-num],str,j+1,list);
            }else{
                x.links[c-let] = getAllWithPrefix(x.links[c-let],str,j+1,list);
            }

        }
        return x;
    }
    private void getKids(Node<Value> x, List<Value> list){
        int counter = 0;
        for (Value v : x.valueSet) {
            if (v != null) {
                // don't think I need to include null values
                list.add(v);
            }
        }
        for(Node<Value> z: x.links){
            if(z == null){
                counter++;
            }else{
                if(z.valueSet != null) {
                    this.getKids(z,list);
                }

            }
            if(counter == alphabetSize){
                return;
            }
        }

    }


    @Override
    public Set<Value> deleteAllWithPrefix(String prefix) {
        if(prefix == null){
            throw new IllegalArgumentException();
        }
        if(prefix.length() == 0){
            return new HashSet<>();
        }
        String lowercase = prefix.toLowerCase();
        List<Value> toDeleteList = new ArrayList<>();
        deleteAllWithPrefix(this.root,lowercase,0,toDeleteList);
        return new HashSet<>(toDeleteList);
    }
    private Node<Value> deleteAllWithPrefix(Node<Value> x, String prefix,int j,List<Value> returned){
        if(x == null){
            return null;
        }
        //need to set this node to null i think
        if(j == prefix.length()) {
            getKids(x,returned);
            x.valueSet.clear();
            for(int i = 0; i < alphabetSize; i++){
                x.links[i] = null;
            }
            return x;
        }else{
            char c = prefix.charAt(j);
            if(c < 97){
                x.links[c-num] = deleteAllWithPrefix(x.links[c-num],prefix,j+1,returned);
            }else{
                x.links[c-let] = deleteAllWithPrefix(x.links[c-let],prefix,j+1,returned);
            }
        }
        return x;
    }



    @Override
    public Set<Value> deleteAll(String key){
        if(key == null){
            throw new IllegalArgumentException();
        }
        if(key.length()==0){
            return new HashSet<>();
        }
        String lowercase = key.toLowerCase();
        Set<Value> toDelete = new HashSet<>();
        deleteAll(this.root,lowercase,0,toDelete);
        return toDelete;
    }
    private Node<Value> deleteAll(Node<Value> x, String str, int j,Set<Value> delete){
        if(x==null){
            return null;
        }
        if(str.length()== j){
            delete.addAll(x.valueSet);
            x.valueSet.removeIf(Objects::nonNull);
        }else{
            char c = str.charAt(j);
            if(c < 97){
                x.links[c-num] = deleteAll(x.links[c-num],str,j+1,delete);
            }else{
                x.links[c- let] = deleteAll(x.links[c-let],str,j+1,delete);
            }

        }
        if (x.valueSet != null)
        {
            return x;
        }
        for (int c = 0; c <alphabetSize; c++)
        {
            if (x.links[c] != null)
            {
                return x; //not empty
            }
        }
        return null;

    }

    @Override
    public Value delete(String key, Value val) {
        //not sure if this will work
        if(key == null || val == null){
            throw new IllegalArgumentException();
        }
        String lowercase = key.toLowerCase();
        Node<Value> getter = this.get(this.root,lowercase,0);
        if(getter != null && getter.valueSet.contains(val)){
            Value toReturn = null;
            for(Value v : getter.valueSet){
                if(v.equals(val)){
                    toReturn = v;
                }
            }
            delete(this.root,lowercase,val,0);
            return toReturn;
        }
        return null;

    }
    private Node<Value> delete(Node<Value> x, String str, Value val, int j){
        if(x==null){
            return null;
        }
        if(str.length() == j){
            //might be null here
            x.valueSet.removeIf(v -> v.equals(val));
        }else{
            char c = str.charAt(j);
            if(c < 97){
                x.links[c-num] = delete(x.links[c-num],str,val,j+1);
            }else{
                x.links[c- let] = delete(x.links[c-let],str,val,j+1);
            }
        }
        if (!x.valueSet.isEmpty())
        {
            return x;
        }
        for (int c = 0; c <alphabetSize; c++)
        {
            if (x.links[c] != null)
            {
                return x; //not empty
            }
        }
        return null;
    }
}