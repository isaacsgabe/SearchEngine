package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class DocumentStoreImpl2Test {

    private DocumentStoreImpl tester() throws Exception {
        File file = new File("C:\\Users\\gabei\\Documents\\Zoom\\ffp");
        DocumentStoreImpl docs = new DocumentStoreImpl(file);
        URI uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
        String txt1 = "orange apple banana grape peach prunes";
        URI uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        String txt2 = "orange lettuce apple tomatoes banana peppers grape cucumbers peach prunes";
        URI uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        String txt3 = "pizza peppers packs poor pouring";
        docs.putDocument(new ByteArrayInputStream(txt1.getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
        docs.putDocument(new ByteArrayInputStream(txt2.getBytes()),uri2, DocumentStore.DocumentFormat.TXT);
        docs.putDocument(new ByteArrayInputStream(txt3.getBytes()),uri3, DocumentStore.DocumentFormat.TXT);
        return docs;
    }

    @Test
    public void Test1() throws Exception {
        URI uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
        String txt1 = "orange apple banana grape peach prunes";
        URI uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        String txt2 = "orange lettuce apple tomatoes banana peppers grape cucumbers peach prunes";
        URI uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        String txt3 = "pizza peppers packs poor pouring";
        DocumentStoreImpl docs = tester();
        docs.setMaxDocumentCount(0);
        docs.setMaxDocumentCount(2);
        docs.getDocument(uri1);
        docs.getDocument(uri3);
        docs.getDocument(uri2);
        docs.getDocument(uri1);
        assertEquals(docs.getDocument(uri3).getDocumentTxt().length(),txt3.length());
    }
    @Test
    public void TestDeleteAndUndo() throws Exception {
        URI uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
        String txt1 = "orange apple banana grape peach prunes";
        URI uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        String txt2 = "orange lettuce apple tomatoes banana peppers grape cucumbers peach prunes";
        URI uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        String txt3 = "pizza peppers packs poor pouring";
        DocumentStoreImpl docs = tester();
        docs.setMaxDocumentCount(0);
        docs.deleteAll("prunes");
        assertNull(docs.getDocument(uri2));
        docs.undo();
        assertNotNull(docs.getDocument(uri2));
    }

    @Test
    public void TestDeleteSingleAndUndo() throws Exception {
        URI uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
        String txt1 = "orange apple banana grape peach prunes";
        URI uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        String txt2 = "orange lettuce apple tomatoes banana peppers grape cucumbers peach prunes";
        URI uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        String txt3 = "pizza peppers packs poor pouring";
        DocumentStoreImpl docs = tester();
        docs.setMaxDocumentCount(2);
        docs.deleteDocument(uri1);
        docs.undo();
        docs.undo(uri2);
    }

    @Test
    public void TestDeleteAndUndoSpecificUri() throws Exception {
        URI uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
        String txt1 = "orange apple banana grape peach prunes";
        URI uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        String txt2 = "orange lettuce apple tomatoes banana peppers grape cucumbers peach prunes";
        URI uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        String txt3 = "pizza peppers packs poor pouring";
        DocumentStoreImpl docs = tester();
        docs.setMaxDocumentCount(0);
        docs.deleteAll("prunes");
        docs.undo(uri1);
        docs.undo();
        assertEquals(docs.getDocument(uri2).getWords().size(),10);
        assertEquals(docs.getDocument(uri1).getDocumentTxt().length(),txt1.length());
    }
    @Test
    public void deleteAllWithPrefix() throws Exception {
        URI uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
        String txt1 = "orange apple banana grape peach prunes";
        URI uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        String txt2 = "orange lettuce apple tomatoes banana peppers grape cucumbers peach prunes";
        URI uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        String txt3 = "pizza peppers packs poor pouring";
        DocumentStoreImpl docs = tester();
        docs.setMaxDocumentCount(0);
        docs.deleteAllWithPrefix("prune");
        docs.undo();
        assertNotNull(docs.getDocument(uri1));
        assertNotNull(docs.getDocument(uri2));
        docs.deleteAllWithPrefix("prune");
        docs.undo(uri1);
        assertNull(docs.getDocument(uri2));
        assertNotNull(docs.getDocument(uri1));
        docs.undo(uri2);
        assertNotNull(docs.getDocument(uri2));
    }

    @Test
    public void TestSearchAll() throws Exception {
        URI uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
        String txt1 = "orange apple banana grape peach prunes";
        URI uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        String txt2 = "orange lettuce apple tomatoes banana peppers grape cucumbers peach prunes";
        URI uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        String txt3 = "pizza peppers packs poor pouring";
        DocumentStoreImpl docs = tester();
        docs.setMaxDocumentCount(2);
        docs.search("prunes");
        assertEquals(docs.getDocument(uri3).getDocumentTxt().length(),txt3.length());
    }

    @Test
    public void TestSearchAllWithPrefix() throws Exception {
        URI uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
        String txt1 = "orange apple banana grape peach prunes";
        URI uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        String txt2 = "orange lettuce apple tomatoes banana peppers grape cucumbers peach prunes";
        URI uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        String txt3 = "pizza peppers packs poor pouring";
        DocumentStoreImpl docs = tester();
        docs.setMaxDocumentCount(2);
        docs.searchByPrefix("prun");


    }

    @Test
    public void URINotThere() throws Exception {
        URI uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
        String txt1 = "orange apple banana grape peach prunes";
        URI uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        String txt2 = "orange lettuce apple tomatoes banana peppers grape cucumbers peach prunes";
        URI uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        String txt3 = "pizza peppers packs poor pouring";
        URI uri40 = new URI("http://edu.yu.cs/com1320/project/doc40000");
        DocumentStoreImpl docs = tester();
        docs.searchByPrefix("prun");
        docs.setMaxDocumentCount(2);
        assert docs.getDocument(uri40) == null;
    }

    @Test
    public void simpleTest() throws Exception {
        URI uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
        String txt1 = "orange apple banana grape peach prunes";
        URI uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        String txt2 = "orange lettuce apple tomatoes banana peppers grape cucumbers peach prunes";
        URI uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        String txt3 = "pizza peppers packs poor pouring";
        DocumentStoreImpl docs = tester();
        docs.setMaxDocumentCount(0);
        docs.putDocument(null,uri1, DocumentStore.DocumentFormat.TXT);
    }

    @Test
    public void ByteTest() throws Exception {
        URI uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
        String txt1 = "orange apple banana grape peach prunes";
        URI uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        String txt2 = "orange lettuce apple tomatoes banana peppers grape cucumbers peach prunes";
        URI uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        String txt3 = "pizza peppers packs poor pouring";
        DocumentStoreImpl docs = tester();
        docs.setMaxDocumentBytes(0);
        docs.deleteDocument(uri1);
        docs.undo();
        docs.deleteAllWithPrefix("prun");
        docs.undo();
    }

    @Test
    public void piazzaTest() throws Exception {
        URI uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
        String txt1 = "orange apple banana grape peach prunes";
        URI uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        String txt2 = "orange lettuce apple tomatoes banana peppers grape cucumbers peach prunes";
        URI uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        String txt3 = "pizza peppers packs poor pouring";
        DocumentStoreImpl docs = tester();
        docs.setMaxDocumentCount(2);
        docs.undo();
    }

    @Test
    public void JakeTest2() throws Exception {
        File file = new File("C:\\Users\\gabei\\Documents\\comp sci\\semester 2\\FolderForProject");
        DocumentStoreImpl docs = new DocumentStoreImpl(file);
        URI uri1 = new URI("ryan://edu.yu.cs/com1320/project/doc1");
        String txt1 = "orange apple banana grape peach prunes";
        URI uri2 = new URI("ryan://edu.yu.cs/com1320/project/doc2");
        String txt2 = "orange lettuce apple tomatoes banana peppers grape cucumbers peach prunes";
        URI uri3 = new URI("ryan://edu.yu.cs/com1320/project/doc3");
        String txt3 = "pizza peppers packs poor pouring";
        docs.putDocument(new ByteArrayInputStream(txt1.getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
        docs.putDocument(new ByteArrayInputStream(txt2.getBytes()),uri2, DocumentStore.DocumentFormat.TXT);
        docs.putDocument(new ByteArrayInputStream(txt3.getBytes()),uri3, DocumentStore.DocumentFormat.TXT);
        docs.setMaxDocumentCount(2);
        System.out.println(uri2.getHost() + uri2.getPath());
    }

    @Test
    public void JakeTest3() throws Exception {
        File file = new File("C:\\Users\\gabei\\Documents\\comp sci\\semester 2\\FolderForProject");
        DocumentStoreImpl docs = new DocumentStoreImpl(file);
        URI uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
        String txt1 = "orange apple banana grape peach prunes";
        URI uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        String txt2 = "orange lettuce apple tomatoes banana peppers grape cucumbers peach prunes";
        URI uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        String txt3 = "pizza peppers packs poor pouring";
        docs.putDocument(new ByteArrayInputStream(txt1.getBytes()),uri1, DocumentStore.DocumentFormat.BINARY);
        docs.putDocument(new ByteArrayInputStream(txt2.getBytes()),uri2, DocumentStore.DocumentFormat.TXT);
        docs.putDocument(new ByteArrayInputStream(txt3.getBytes()),uri3, DocumentStore.DocumentFormat.BINARY);
        docs.setMaxDocumentCount(2);
    }
    @Test
    public void JakeTest4() throws Exception {
        File file = new File("C:\\Users\\gabei\\Documents\\comp sci\\semester 2\\FolderForProject");
        DocumentStoreImpl docs = new DocumentStoreImpl(file);
        URI uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
        String txt1 = "orange apple banana grape peach prunes";
        URI uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        String txt2 = "orange lettuce apple tomatoes banana peppers grape cucumbers peach prunes";
        URI uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        String txt3 = "pizza peppers packs poor pouring";
        docs.putDocument(new ByteArrayInputStream(txt1.getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
        docs.putDocument(new ByteArrayInputStream(txt2.getBytes()),uri2, DocumentStore.DocumentFormat.TXT);
        docs.putDocument(new ByteArrayInputStream(txt3.getBytes()),uri3, DocumentStore.DocumentFormat.TXT);
        docs.setMaxDocumentCount(0);
        docs.putDocument(new ByteArrayInputStream(txt3.getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
        System.out.println(docs.getDocument(uri1).getWords());

    }

    @Test
    public void JakeTest5() throws Exception {
        File file = new File("C:\\Users\\gabei\\Documents\\comp sci\\semester 2\\FolderForProject");
        DocumentStoreImpl docs = new DocumentStoreImpl(file);
        URI uri1 = new URI("http://edu.yu.cs/com1320/project/doc1/");
        String txt1 = "orange apple banana grape peach prunes";
        URI uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        String txt2 = "orange lettuce apple tomatoes banana peppers grape cucumbers peach prunes";
        URI uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        String txt3 = "pizza peppers packs poor pouring";

        docs.putDocument(new ByteArrayInputStream(txt2.getBytes()),uri2, DocumentStore.DocumentFormat.TXT);
        docs.putDocument(new ByteArrayInputStream(txt3.getBytes()),uri3, DocumentStore.DocumentFormat.TXT);
        boolean tr = false;
        try{
            docs.putDocument(new ByteArrayInputStream(txt1.getBytes()),uri1, DocumentStore.DocumentFormat.TXT);
            fail();
        }catch(IllegalArgumentException e){
            tr = true;
            assertTrue(tr);
        }
        docs.setMaxDocumentCount(0);

    }

    @Test
    public void JakeTest6() throws Exception {
        File file = new File("C:\\Users\\gabei\\Documents\\comp sci\\semester 2\\FolderForProject");
        DocumentStoreImpl docs = new DocumentStoreImpl(file);
        URI uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
        String txt1 = "orange apple banana grape peach prunes";
        URI uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        String txt2 = "orange lettuce apple tomatoes banana peppers grape cucumbers peach prunes";
        URI uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        String txt3 = "pizza peppers packs poor pouring";
        docs.putDocument(new ByteArrayInputStream(txt1.getBytes()),uri1, DocumentStore.DocumentFormat.BINARY);
        docs.putDocument(new ByteArrayInputStream(txt2.getBytes()),uri2, DocumentStore.DocumentFormat.TXT);
        docs.putDocument(new ByteArrayInputStream(txt3.getBytes()),uri3, DocumentStore.DocumentFormat.BINARY);
        docs.setMaxDocumentCount(2);
        docs.undo();
    }



}