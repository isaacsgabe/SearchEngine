package edu.yu.cs.com1320.project;

import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import java.io.IOException;

public interface BTree<Key extends Comparable<Key>, Value> {
    Value get(Key k) throws IOException;
    Value put(Key k, Value v) throws IOException;
    void moveToDisk(Key k) throws Exception;
    void setPersistenceManager(PersistenceManager<Key,Value> pm);
}