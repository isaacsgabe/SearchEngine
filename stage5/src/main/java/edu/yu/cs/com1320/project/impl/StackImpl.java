package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Stack;

public class StackImpl<T> implements Stack<T> {
    //add something
    class Entry<T>{
        private Entry<T> previous;
        private final T item;
        private Entry(T item) {
            this.previous = null;
            this.item = item;
        }
        private void setPrevious(Entry<T>  element){
            this.previous = element;
        }

    }
    private Entry<T> mostRecentItem;
    private int counter;

    public StackImpl(){
        this.mostRecentItem = null;
        this.counter = 0;
    }

    @Override
    public void push(T element) {
        if(mostRecentItem == null){
            mostRecentItem = new Entry<>(element);
            this.counter++;
        }else if(this.mostRecentItem.previous == null){
            Entry<T> oldEntry = this.mostRecentItem;
            this.mostRecentItem = new Entry<>(element);
            this.mostRecentItem.setPrevious(oldEntry);
            this.counter++;
        }else{
            Entry<T>  newEl = new Entry<>(element);
            newEl.setPrevious(this.mostRecentItem);
            this.mostRecentItem = newEl;
            this.counter++;
        }

    }

    @Override
    public T pop() {
        if(this.mostRecentItem ==null){
            return null;
        }
        Entry<T> dummy = this.mostRecentItem;
        this.mostRecentItem = this.mostRecentItem.previous;
        counter--;
        return dummy.item;
    }

    @Override
    public T peek() {
        if(this.mostRecentItem ==null){
            return null;
        }
        return this.mostRecentItem.item;
    }

    @Override
    public int size() {
        return this.counter;
    }
}