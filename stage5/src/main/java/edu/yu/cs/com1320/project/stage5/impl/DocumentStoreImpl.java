package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.CommandSet;
import edu.yu.cs.com1320.project.GenericCommand;

import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.*;



import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;


import java.util.function.Function;

public class DocumentStoreImpl implements DocumentStore {
    private final BTree<URI,Document> store;
    private final StackImpl<Undoable> stack;
    private final TrieImpl<URI> trie;
    private final MinHeapImpl<UriTimeConnection> heap;
    private int maxDocs;
    private int maxBytes;
    //fix byteCounter
    private int byteCounter;
    private int docCounter;
    private Set<URI> urisToBePutBack;

    public DocumentStoreImpl(File file) {
        this.heap = new MinHeapImpl<>();
        this.store = new BTreeImpl<>();
        this.store.setPersistenceManager(new DocumentPersistenceManager(file));
        this.stack = new StackImpl<>();
        this.trie = new TrieImpl<>();
        this.maxBytes = -1;
        this.maxDocs = -1;
        this.byteCounter = 0;
        this.urisToBePutBack = new HashSet<>();

    }
    public DocumentStoreImpl(){
        this.heap = new MinHeapImpl<>();
        this.store = new BTreeImpl<>();
        this.stack = new StackImpl<>();
        this.store.setPersistenceManager(new DocumentPersistenceManager(null));
        this.trie = new TrieImpl<>();
        this.maxBytes = -1;
        this.maxDocs = -1;
        this.byteCounter = 0;
        this.urisToBePutBack = new HashSet<>();
    }
    private class UriTimeConnection implements Comparable<UriTimeConnection> {
        private BTree<URI,Document> tree;
        private URI uri;

        private UriTimeConnection(BTree b, URI u){
            this.tree = b;
            this.uri = u;
        }
        private URI getUri(){
            return this.getUri();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UriTimeConnection that = (UriTimeConnection) o;
            return Objects.equals(uri, that.uri);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uri);
        }

        private Document getDoc() throws IOException {
            return tree.get(this.uri);
        }

        @Override
        public int compareTo(UriTimeConnection o) {
            try {
                if(this.getDoc().getLastUseTime() > o.getDoc().getLastUseTime()){
                    return 1;
                }else if(o.getDoc().getLastUseTime() > this.getDoc().getLastUseTime()){
                    return -1;
                }else{
                    return 0;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

    public int putDocument(InputStream input, URI uri, DocumentFormat format) throws IOException {
        Document newDoc;
        if(uri == null|| format == null){
            throw new IllegalArgumentException();
        }else {
            if(uri.toString().charAt(uri.toString().length() - 1) == '/' || uri.toString().charAt(uri.toString().length() - 1) == '\\'){
                throw new IllegalArgumentException();
            }
        }
        if (input == null) {
            Document oldDoc = store.get(uri);
            if(this.deleteDocument(uri)){
                return oldDoc.hashCode();
            }else{
                return 0;
            }
        }
        byte[] bytes = input.readAllBytes();
        byteCounter += bytes.length;
        this.docCounter++;
        if(format ==  DocumentFormat.BINARY) {
            newDoc = new DocumentImpl(uri, bytes);
        }else{
            newDoc = new DocumentImpl(uri, new String(bytes));
        }
        this.createPutFunction(uri,newDoc,format);
        Document oldDoc = store.put(uri, newDoc);
        newDoc.setLastUseTime(System.nanoTime());
        UriTimeConnection doc = new UriTimeConnection(this.store,newDoc.getKey());
        this.heap.insert(doc);
        if(oldDoc != null){
            deleteFromHeap(oldDoc);
            this.checkAndRemoveDocForPut();
            for(String s: oldDoc.getWords()){
                this.trie.delete(s,oldDoc.getKey());
            }
        }
        //make sure undo method puts the deleted document back
        for(String s: newDoc.getWords()){
            this.trie.put(s,newDoc.getKey());
        }
        if(oldDoc == null){
            this.checkAndRemoveDocForPut();
            return 0;
        }
        return oldDoc.hashCode();
    }
    //heap finished
    @Override
    public Document getDocument(URI uri) throws IOException {
        Document j = store.get(uri);
        if(j != null){
            //if it is not in it add it, otherwise find the uri, change it and reheapify
            j.setLastUseTime(System.nanoTime());
            UriTimeConnection doc = new UriTimeConnection(this.store,j.getKey());
            try{
                this.heap.reHeapify(doc);
            }catch (NoSuchElementException e){
                this.heap.insert(doc);
                this.checkDocTypeAndAdd(j);
            }
            checkAndRemoveDoc();
        }
        return j;
    }
    //heap finished
    @Override
    public boolean deleteDocument(URI uri) throws IOException {
        Document getDoc = this.store.get(uri);
        if(getDoc == null){
            Function<URI,Boolean> thisDoc = uri1 -> true;
            Undoable command = new GenericCommand<>(uri,thisDoc);
            this.stack.push(command);
            return false;
        }else{
            deleteFromHeap(getDoc);
            store.put(uri,null);
            Function<URI,Boolean> thisDoc = (URI uri1) -> {
                //here
                getDoc.setLastUseTime(System.nanoTime());
                try {
                    this.store.put(uri1, getDoc);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                UriTimeConnection doc = new UriTimeConnection(this.store,getDoc.getKey());
                this.heap.insert(doc);
                checkDocTypeAndAdd(getDoc);
                try {
                    checkAndRemoveDoc();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Set<String> words = getDoc.getWords();
                for(String s: words){
                    trie.put(s,getDoc.getKey());
                }
                return true;
            };
            Undoable command = new GenericCommand<>(uri,thisDoc);
            this.stack.push(command);
            Set<String> wordsToDelete = getDoc.getWords();
            for(String s: wordsToDelete){
                trie.delete(s,getDoc.getKey());
            }
            return true;
        }


    }

    @Override
    public void undo() throws IOException {
        if(this.stack.size() == 0){
            throw new IllegalStateException();
        }
        Undoable undo =  this.stack.pop();
        long nano = System.nanoTime();
        if(undo instanceof CommandSet){
            for(GenericCommand<URI> g : (CommandSet<URI>) undo){
                URI getDoc = g.getTarget();
                g.undo();
                Document changeTime =this.store.get(getDoc);
                checkDocTypeAndAdd(changeTime);
                changeTime.setLastUseTime(nano);
                UriTimeConnection doc = new UriTimeConnection(this.store,changeTime.getKey());
                try{
                    this.heap.reHeapify(doc);
                }catch (NoSuchElementException e){
                    this.heap.insert(doc);
                }
                checkAndRemoveDoc();
            }
        }else{
            undo.undo();
            checkAndRemoveDoc();
        }


    }

    @Override
    public void undo(URI uri) throws IllegalStateException {
        if(this.stack.size() == 0 || uri == null){
            throw new IllegalStateException();
        }
        StackImpl<Undoable> secondStack = new StackImpl<>();
        Undoable commandToUndo =  this.stack.peek();
        URI toUndo = URI.create("blank");
        if(commandToUndo instanceof GenericCommand){
            GenericCommand<URI> needToUndo = (GenericCommand<URI>) commandToUndo;
            toUndo = needToUndo.getTarget();
        }else if(commandToUndo instanceof CommandSet){
            CommandSet<URI> needToUndo = (CommandSet<URI>) commandToUndo;
            if(needToUndo.containsTarget(uri)){
                for(GenericCommand<URI> g: needToUndo){
                    if(g.getTarget().equals(uri)){
                        toUndo = g.getTarget();
                    }
                }
            }
        }
        while(!toUndo.equals(uri)){
            Undoable undo =  this.stack.pop();
            secondStack.push(undo);
            if(this.stack.size() == 0){
                while(secondStack.size() != 0){
                    Undoable transfer = secondStack.pop();
                    this.stack.push(transfer);
                }
                throw new IllegalStateException();
            }
            if(this.stack.peek() instanceof GenericCommand){
                GenericCommand<URI> needToUndo = (GenericCommand<URI>) this.stack.peek();
                toUndo = needToUndo.getTarget();
            }else if(this.stack.peek() instanceof CommandSet){
                if(((CommandSet<URI>) this.stack.peek()).containsTarget(uri)){
                    for(GenericCommand<URI> g: (CommandSet<URI>)this.stack.peek()){
                        if(g.getTarget().equals(uri)){
                            toUndo = g.getTarget();
                        }
                    }
                }
            }
        }
        Undoable commandToUse = this.stack.pop();
        GenericCommand<URI> cmd = null;
        CommandSet<URI> cmdSet= new CommandSet<>();
        if(commandToUse instanceof CommandSet){
            for(GenericCommand<URI> g: (CommandSet<URI>)commandToUse){
                if(g.getTarget().equals(uri)){
                    cmd = g;
                }else{
                    cmdSet.addCommand(g);
                }
            }
            if(!cmdSet.isEmpty()){
                this.stack.push(cmdSet);
            }
            cmd.undo();
        }else{
            commandToUse.undo();
        }
        while(secondStack.size() != 0){
            Undoable transfer = secondStack.pop();
            this.stack.push(transfer);
        }
    }
    //heap finished
    @Override
    public List<Document> search(String keyword) throws IOException {
        String lowercase = keyword.toLowerCase();
        List<Document> toReturn = new ArrayList<>();
        List<URI> uris =  this.trie.getAllSorted(lowercase, (uri1,uri2) ->{
            try {
                if ((store.get(uri1).wordCount(lowercase) > store.get(uri2).wordCount(lowercase))){
                    return -1;
                } else if (store.get(uri1).wordCount(lowercase) > store.get(uri2).wordCount(lowercase)){
                    return 1;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;});
        // need to use nano time
        long j = System.nanoTime();
        for (URI g: uris){
            //here
            Document d = this.store.get(g);
            toReturn.add(d);
            d.setLastUseTime(j);
            UriTimeConnection doc = new UriTimeConnection(this.store,d.getKey());
            try{
                this.heap.reHeapify(doc);
            }catch (NoSuchElementException e){
                this.heap.insert(doc);
                this.checkDocTypeAndAdd(d);
            }
            checkAndRemoveDoc();
        }
        return toReturn;
    }
    //heap finished
    @Override
    public List<Document> searchByPrefix(String keywordPrefix) throws IOException {
        String lowercase = keywordPrefix.toLowerCase();
        List<Document> toReturn = new ArrayList<>();
        List<URI> uris = this.trie.getAllWithPrefixSorted(lowercase,(uri1,uri2) ->{
            int counter = 0;
            int counterTwo = 0;
            Document doc1 = null;
            try {
                doc1 = this.store.get(uri1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            for(String s: doc1.getWords()){
                if(s.startsWith(keywordPrefix)){
                    counter += doc1.wordCount(s);
                }
            }
            Document doc2 = null;
            try {
                doc2 = this.store.get(uri2);
            } catch (IOException e) {
                e.printStackTrace();
            }
            for(String s: doc2.getWords()){
                if(s.startsWith(keywordPrefix)){
                    counterTwo += doc2.wordCount(s);
                }
            }
            if (counter < counterTwo){
                return 1;
            } else if (counterTwo < counter){
                return -1;
            }
            return 0;});
        long j = System.nanoTime();
        for(URI g: uris){
            Document d = this.store.get(g);
            toReturn.add(d);
            d.setLastUseTime(j);
            UriTimeConnection doc = new UriTimeConnection(this.store,d.getKey());
            try{
                this.heap.reHeapify(doc);
            }catch (NoSuchElementException e){
                this.heap.insert(doc);
                this.checkDocTypeAndAdd(d);
            }
            checkAndRemoveDoc();

        }
        return toReturn;
    }

    @Override
    public Set<URI> deleteAll(String keyword) throws IOException {
        Set<URI> docs = this.trie.deleteAll(keyword);
        Set<URI> uris = new HashSet<>();
        if(docs.isEmpty()){
            return uris;
        }
        CommandSet<URI> cmdSet = new CommandSet<>();
        for (URI g: docs){
            Document d = this.store.get(g);
            deleteFromHeap(d);
            uris.add(d.getKey());
            for(String s: d.getWords()){
                this.trie.delete(s,d.getKey());
            }
            Function<URI,Boolean> toUndo = (URI uri1) -> {
               checkDocTypeAndAdd(d);
                try {
                    this.store.put(d.getKey(),d);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                UriTimeConnection doc = new UriTimeConnection(this.store,d.getKey());
                this.heap.insert(doc);
                for(String s: d.getWords()){
                    this.trie.put(s,d.getKey());
                }
                try {
                    checkAndRemoveDoc();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            };
            GenericCommand<URI> singleCmd = new GenericCommand<>(d.getKey(),toUndo);
            cmdSet.addCommand(singleCmd);
        }
        this.stack.push(cmdSet);
        for(URI i: uris){
            this.store.put(i,null);
        }
        return uris;
    }

    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) throws IOException {
        Set<URI> docs = this.trie.deleteAllWithPrefix(keywordPrefix);
        if(docs.isEmpty()){
            return new HashSet<>();
        }
        Set<URI> uris = new HashSet<>();
        CommandSet<URI> cmdSet = new CommandSet<>();
        for (URI g: docs){
            Document d = this.store.get(g);
            deleteFromHeap(d);
            uris.add(d.getKey());
            for(String s: d.getWords()){
                this.trie.delete(s,d.getKey());
            }
            Function<URI,Boolean> toUndo = (URI uri1) -> {
                checkDocTypeAndAdd(d);
                try {
                    this.store.put(d.getKey(),d);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                UriTimeConnection doc = new UriTimeConnection(this.store,d.getKey());
                this.heap.insert(doc);
                for(String s: d.getWords()){
                    this.trie.put(s,d.getKey());
                }
                try {
                    checkAndRemoveDoc();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            };
            GenericCommand<URI> singleCmd = new GenericCommand<>(d.getKey(),toUndo);
            cmdSet.addCommand(singleCmd);
        }
        this.stack.push(cmdSet);
        for(URI i: uris){
            this.store.put(i,null);
        }
        return uris;
    }

    @Override
    public void setMaxDocumentCount(int limit) throws IOException {
        if(limit < 0){
            throw new IllegalArgumentException();
        }
        this.maxDocs = limit;
        this.checkAndRemoveDocForPut();

    }

    @Override
    public void setMaxDocumentBytes(int limit) throws IOException {
        if(limit < 0){
            throw new IllegalArgumentException();
        }
        this.maxBytes = limit;
        this.checkAndRemoveDocForPut();
    }
    private void checkAndRemoveDoc() throws IOException {
        while((this.maxBytes != -1 && this.maxBytes < this.byteCounter) || (this.maxDocs != -1 && this.maxDocs < this.docCounter)){
            UriTimeConnection data = this.heap.remove();
            Document j = data.getDoc();
            try {
                this.store.moveToDisk(j.getKey());
            } catch (Exception e) {
                e.printStackTrace();
            }
            checkDocTypeAndSubtract(j);
        }
    }

    private void checkAndRemoveDocForPut() throws IOException {
        while((this.maxBytes != -1 && this.maxBytes < this.byteCounter) || (this.maxDocs != -1 && this.maxDocs < this.docCounter)){
            UriTimeConnection data = this.heap.remove();
            Document j = data.getDoc();
            try {
                this.store.moveToDisk(j.getKey());
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.urisToBePutBack.add(j.getKey());
            checkDocTypeAndSubtract(j);
        }
    }
    private void deleteFromHeap(Document document) throws NoSuchElementException {
        if(document == null){
            return;
        }
        try{
            this.store.get(document.getKey()).setLastUseTime(Long.MIN_VALUE);
            UriTimeConnection doc = new UriTimeConnection(this.store,document.getKey());
            this.heap.reHeapify(doc);
            this.heap.remove();
            checkDocTypeAndSubtract(document);
        }catch (NoSuchElementException | IOException ignored){
        }


    }
    private void createPutFunction(URI uri,Document newDoc,DocumentFormat format) throws IOException {
        if(store.get(uri) == null) {
            Function<URI, Boolean> firstInput = (URI uri1) -> {
                if(format == DocumentFormat.TXT){
                    for (String s : newDoc.getWords()) {
                        this.trie.delete(s, newDoc.getKey());
                    }
                }
                try {
                    this.deleteFromHeap(newDoc);
                } catch (NoSuchElementException e) {
                    e.printStackTrace();
                }
                try {
                    this.store.put(uri1, null);
                    for(URI u: this.urisToBePutBack){
                        this.getDocument(u);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            };
            Undoable command = new GenericCommand<>(uri, firstInput);
            this.stack.push(command);
        }else{
            Document oldDoc = this.store.get(uri);
            Function<URI,Boolean> secondInput = uri1 -> {
                try {
                    this.store.put(uri1,oldDoc);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // here
                try {
                    this.deleteFromHeap(newDoc);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                oldDoc.setLastUseTime(System.nanoTime());
                UriTimeConnection doc = new UriTimeConnection(this.store,oldDoc.getKey());
                this.heap.insert(doc);
                Set<String> words = oldDoc.getWords();
                for(String s: words){
                    this.trie.put(s,oldDoc.getKey());
                }
                Set<String> wordsToDelete = newDoc.getWords();
                for(String s: wordsToDelete){
                    this.trie.delete(s,newDoc.getKey());
                }

                return true;
            };
            Undoable command = new GenericCommand<>(uri,secondInput);
            this.stack.push(command);
        }
    }

    private void checkDocTypeAndAdd(Document document){
        docCounter++;
        if(document.getDocumentTxt() == null){
            byteCounter += document.getDocumentBinaryData().length;
        }else{
            byteCounter += document.getDocumentTxt().getBytes().length;
        }
    }

    private void checkDocTypeAndSubtract(Document document){
        docCounter--;
        if(document.getDocumentTxt() == null){
            byteCounter -= document.getDocumentBinaryData().length;
        }else{
            byteCounter -= document.getDocumentTxt().getBytes().length;
        }

    }

}