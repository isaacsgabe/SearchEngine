package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentPersistenceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.print.Doc;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class BTreeImplTest {
    private URI uria;
    private String txta;

    private URI urib;
    private String txtb;

    private URI uric;
    private String txtc;

    private URI urid;
    private String txtd;

    private URI urie;
    private String txte;

    private URI urif;
    private String txtf;

    @BeforeEach
    public void init3() throws Exception {
        //init possible values for doc1
        this.uria = new URI("http://edu.yu.cs/com1320/project/doc1");
        this.txta = "A Apple Pizza Fish Pie B C";

        //init possible values for doc2
        this.urib = new URI("http://edu.yu.cs/com1320/project/doc2");
        this.txtb = "Pizza Pizza Pizza Pizza Pizza";

        //init possible values for doc3
        this.uric = new URI("http://edu.yu.cs/com1320/project/doc3");
        this.txtc = "Penguin Park Piccalo Pants Pain Possum Parties pony";

        this.urid = new URI("http://edu.yu.cs/com1320/project/doc4");
        this.txtd = "Sally sells seashells by the seashore";

        //init possible values for doc3
        this.urie = new URI("http://edu.yu.cs/com1320/project/doc5");
        this.txte = "How much wood can a woodchuck chuck if a woodchuck could chuck wood";

        this.urif = new URI("http://edu.yu.cs/com1320/project/doc6");
        this.txtf = "I want to test array doubling h h p z v s e fk  kas ";
    }
    @Test
    public void Test1() throws Exception {
        BTreeImpl<URI,Document> btree = new BTreeImpl<>();
        Document document1 = new DocumentImpl(uria,txta);
        btree.put(uria,document1);
        File file = new File("C:\\Users\\gabei\\Documents\\comp sci\\semester 2\\FolderForProject");
        btree.setPersistenceManager(new DocumentPersistenceManager(file));
        btree.moveToDisk(uria);
        Document j = btree.get(uria);
    }

    @Test
    public void Test2() throws Exception {
        BTreeImpl<URI,Document> btree = new BTreeImpl<>();
        File file = new File("C:\\Users\\gabei\\Documents\\comp sci\\semester 2\\FolderForProject");
        btree.setPersistenceManager(new DocumentPersistenceManager(file));
        Document document1 = new DocumentImpl(uria,txta);
        Document document2 = new DocumentImpl(uria,txtb);
        btree.put(uria,document1);
        btree.put(uria,document2);
        btree.moveToDisk(uria);
        Document j = btree.get(uria);
        assertEquals(j.getDocumentTxt().length(),document2.getDocumentTxt().length());
    }

    @Test
    public void Test3() throws Exception {
        BTreeImpl<URI,Document> btree = new BTreeImpl<>();
        File file = new File("C:\\Users\\gabei\\Documents\\comp sci\\semester 2\\FolderForProject");
        btree.setPersistenceManager(new DocumentPersistenceManager(file));
        Document document1 = new DocumentImpl(uria,txta);
        Document document2 = new DocumentImpl(uria,txtb);
        btree.put(uria,document1);
        btree.moveToDisk(uria);
        btree.put(uria,null);
        Document j = btree.get(uria);
        assertNull(j);
    }

    @Test
    public void Test4() throws Exception {
        BTreeImpl<URI,Document> btree = new BTreeImpl<>();
        File file = new File("C:\\Users\\gabei\\Documents\\comp sci\\semester 2\\FolderForProject");
        btree.setPersistenceManager(new DocumentPersistenceManager(file));
        Document document1 = new DocumentImpl(uria,txta);
        Document document2 = new DocumentImpl(uria,txtb);
        btree.put(uria,document1);
        btree.moveToDisk(uria);
        btree.put(uria,null);
        Document j = btree.get(uria);
        btree.put(uria,document1);
        assertNull(j);
    }

    @Test
    public void Test5() throws Exception {
        BTreeImpl<URI,Document> btree = new BTreeImpl<>();
        File file = new File("C:\\Users\\gabei\\Documents\\comp sci\\semester 2\\FolderForProject");
        btree.setPersistenceManager(new DocumentPersistenceManager(file));
        Document document1 = new DocumentImpl(uria,txta);
        Document document2 = new DocumentImpl(uria,txtb);
        btree.put(uria,document1);
        btree.moveToDisk(uria);
        btree.put(uria,null);
        Document j = btree.get(uria);
        btree.put(uria,document1);
        btree.put(urib,document2);
        btree.moveToDisk(uria);
        btree.moveToDisk(urib);
        btree.put(uria,null);
        btree.put(urib,null);
    }
}