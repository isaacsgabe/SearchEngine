package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;

import java.util.NoSuchElementException;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E> {

    public MinHeapImpl() {
        //might want to start the heap at 1 for the case when there is only one documet and need to up heap
        this.elements = (E[]) new Comparable[5];
        this.count = 0;
    }

    @Override
    public void reHeapify(E element) {
        if (element == null) {
            throw new IllegalArgumentException();
        }
        int j = this.getArrayIndex(element);
        if (j != 1 && this.isGreater(j / 2, j)) {
            this.upHeap(j);
        } else if ((j * 2 <= this.count && this.isGreater(j, j * 2)) || (j* 2 + 1 <= this.count && this.isGreater(j, j * 2 + 1))) {
            this.downHeap(j);
        }
    }

    @Override
    public int getArrayIndex(E element) {
        for (int i = 1; i < this.elements.length; i++) {
            if (this.elements[i] == null) {
                continue;
            } else if (this.elements[i].equals(element)) {
                return i;
            }
        }
        throw new NoSuchElementException();
    }

    @Override
    protected void doubleArraySize() {
        Comparable<E>[] doubled = new Comparable[elements.length * 2];
        System.arraycopy(this.elements, 0, doubled, 0, this.elements.length);
        this.elements = (E[]) doubled;
    }
}
