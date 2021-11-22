package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;

import java.io.IOException;
import java.util.Arrays;

public class BTreeImpl<Key extends Comparable<Key>, Value> implements BTree<Key, Value> {
    //max children per B-tree node = MAX-1 (must be an even number and greater than 2)
    private static final int MAX = 6;
    private Node root; //root of the B-tree
    private Node leftMostExternalNode;
    private int height; //height of the B-tree
    private int n; //number of k-vue pairs in the B-tree
    private PersistenceManager<Key,Value> pm;
    private Class clss = null;

    //B-tree node data type
    private static final class Node
    {
        private int entryCount; // number of entries
        private Entry[] entries = new Entry[MAX]; // the array of children
        private Node next;
        private Node previous;

        // create a node with k entries
        private Node(int k)
        {
            this.entryCount = k;
        }

        private void setNext(Node next)
        {
            this.next = next;
        }
        private Node getNext()
        {
            return this.next;
        }
        private void setPrevious( Node previous)
        {
            this.previous = previous;
        }
        private  Node getPrevious()
        {
            return this.previous;
        }

        private  Entry[] getEntries()
        {
            return Arrays.copyOf(this.entries, this.entryCount);
        }

    }

    //internal nodes: only use k and child
    //external nodes: only use k and vue
    public static class Entry
    {
        private Comparable k;
        private Object v;
        private  Node child;

        public Entry(Comparable k, Object v,  Node child)
        {
            this.k= k;
            this.v = v;
            this.child = child;
        }
        public Object getValue()
        {
            return this.v;
        }
        public Comparable getKey()
        {
            return this.k;
        }
    }

    /**
     * Initializes an empty B-tree.
     */
    public BTreeImpl()
    {
        this.root = new  Node(0);
        this.leftMostExternalNode = this.root;
    }
    @Override
    public Value get(Key k){
        if (k== null)
        {
            throw new IllegalArgumentException("argument to get() is null");
        }
        Entry entry = this.get(this.root, k, this.height);
        if(entry != null)
        {
            if(entry.v != null && !(entry.v instanceof Document)){
                Value j = null;
                try {
                    j = this.pm.deserialize(k);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                entry.v = j;
            }
            return (Value)entry.v;
        }
        return null;
    }

    private Entry get(Node currentNode, Key k, int height)
    {
        Entry[] entries = currentNode.entries;

        //current node is external (i.e. height == 0)
        if (height == 0)
        {
            for (int j = 0; j < currentNode.entryCount; j++)
            {
                if(isEqual(k, entries[j].k))
                {
                    //found desired k. Return its vue
                    return entries[j];
                }
            }
            //didn't find the k
            return null;
        }

        //current node is internal (height > 0)
        else
        {
            for (int j = 0; j < currentNode.entryCount; j++)
            {
                //if (we are at the last kin this node OR the kwe
                //are looking for is less than the next k, i.e. the
                //desired k must be in the subtree below the current entry),
                //then recurse into the current entry’s child
                if (j + 1 == currentNode.entryCount || less(k, entries[j + 1].k))
                {
                    return this.get(entries[j].child, k, height - 1);
                }
            }
            //didn't find the k
            return null;
        }
    }

    @Override
    public Value put(Key k, Value v)  {
            if (k== null) {
                throw new IllegalArgumentException("argument k is null");
            }
            if(v != null && clss == null){
                clss = v.getClass();
            }
            //if the k already exists in the b-tree, simply replace the vue
            Entry alreadyThere = this.get(this.root, k, this.height);
            if (alreadyThere != null) {
                Value value = (Value) alreadyThere.v;
                if(value != null && !this.clss.equals(value.getClass())){
                    try {
                        this.pm.deserialize(k);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                alreadyThere.v = v;
                return value;
            }

            Node newNode = this.put(this.root, k, v, this.height);
            this.n++;
            if (newNode == null) {
                return null;
            }

            //split the root:
            //Create a new node to be the root.
            //Set the old root to be new root's first entry.
            //Set the node returned from the call to put to be new root's second entry
            Node newRoot = new Node(2);
            newRoot.entries[0] = new Entry(this.root.entries[0].k, null, this.root);
            newRoot.entries[1] = new Entry(newNode.entries[0].k, null, newNode);
            this.root = newRoot;
            //a split at the root always increases the tree height by 1
            this.height++;
            return null;
        }
      

        /**
         *
         * @param currentNode
         * @param k
         * @param v
         * @param height
         * @return null if no new node was created (i.e. just added a new Entry into an existing node). If a new node was created due to the need to split, returns the new node
         */
        private Node put(Node currentNode, Key k, Value v,int height)
        {
            int j;
            Entry newEntry = new Entry(k, v, null);

            //external node
            if (height == 0) {
                //find index in currentNode’s entry[] to insert new entry
                //we look for k< entry.k since we want to leave j
                //pointing to the slot to insert the new entry, hence we want to find
                //the first entry in the current node that kis LESS THAN
                for (j = 0; j < currentNode.entryCount; j++) {
                    if (less(k, currentNode.entries[j].k)) {
                        break;
                    }
                }
            }

            // internal node
            else {
                //find index in node entry array to insert the new entry
                for (j = 0; j < currentNode.entryCount; j++) {
                    //if (we are at the last kin this node OR the kwe
                    //are looking for is less than the next k, i.e. the
                    //desired k must be added to the subtree below the current entry),
                    //then do a recursive call to put on the current entry’s child
                    if ((j + 1 == currentNode.entryCount) || less(k, currentNode.entries[j + 1].k)) {
                        //increment j (j++) after the call so that a new entry created by a split
                        //will be inserted in the next slot
                        Node newNode = this.put(currentNode.entries[j++].child, k, v, height - 1);
                        if (newNode == null) {
                            return null;
                        }
                        //if the call to put returned a node, it means I need to add a new entry to
                        //the current node
                        newEntry.k= newNode.entries[0].k;
                        newEntry.v = null;
                        newEntry.child = newNode;
                        break;
                    }
                }
            }
            //shift entries over one place to make room for new entry
            for (int i = currentNode.entryCount; i > j; i--) {
                currentNode.entries[i] = currentNode.entries[i - 1];
            }
            //add new entry
            currentNode.entries[j] = newEntry;
            currentNode.entryCount++;
            if (currentNode.entryCount < MAX) {
                //no structural changes needed in the tree
                //so just return null
                return null;
            } else {
                //will have to create new entry in the parent due
                //to the split, so return the new node, which is
                //the node for which the new entry will be created
                return this.split(currentNode, height);
            }
        }
    

    @Override
    public void moveToDisk(Key k) throws Exception {
        if (k == null){
            throw new IllegalArgumentException();
        }
        Entry entry = this.get(this.root,k,this.height);
        if(entry.v==null){
            throw new IllegalArgumentException();
        }
        this.pm.serialize(k, (Value) entry.v);
        entry.v = k;

    }

    @Override
    public void setPersistenceManager(PersistenceManager<Key, Value> pm) {
        this.pm = pm;
    }

    private static boolean less(Comparable k1, Comparable k2)
    {
        return k1.compareTo(k2) < 0;
    }

    private static boolean isEqual(Comparable k1, Comparable k2)
    {
        return k1.compareTo(k2) == 0;
    }

    private Node split(Node currentNode, int height)
    {
         Node newNode = new  Node( MAX / 2);
        //by changing currentNode.entryCount, we will treat any vue
        //at index higher than the new currentNode.entryCount as if
        //it doesn't exist
        currentNode.entryCount =  MAX / 2;
        //copy top half of h into t
        for (int j = 0; j <  MAX / 2; j++)
        {
            newNode.entries[j] = currentNode.entries[ MAX / 2 + j];
        }
        //external node
        if (height == 0)
        {
            newNode.setNext(currentNode.getNext());
            newNode.setPrevious(currentNode);
            currentNode.setNext(newNode);
        }
        return newNode;
    }
}

