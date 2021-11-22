package edu.yu.cs.com1320.project.stage5.impl;

import com.google.gson.*;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import javax.print.Doc;
import java.nio.file.NoSuchFileException;
import java.util.Properties;
import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * created by the document store and given to the BTree via a call to BTree.setPersistenceManager
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {

    private final File baseDirectory;

    public DocumentPersistenceManager(File baseDir) {
        if (baseDir != null) {
            this.baseDirectory = baseDir;
        } else {
            String currentDirectory = System.getProperty("user.dir");
            File file = new File(currentDirectory);
            this.baseDirectory = file;
        }


    }

    @Override
    public void serialize(URI uri, Document val) throws IOException {
        //don't forget null values
        //if the document doesn't have a backslash

        Gson gson = null;
        if (uri == null || val == null) {
            throw new IllegalArgumentException();
        }
        if (val.getDocumentTxt() != null) {
            gson = new Gson().newBuilder().excludeFieldsWithoutExposeAnnotation().create();
        } else {
            JsonSerializer<Document> serializer = new JsonSerializer<Document>() {
                @Override
                public JsonElement serialize(Document document, Type type, JsonSerializationContext jsonSerializationContext) {
                    JsonObject jsonDocument = new JsonObject();
                    jsonDocument.addProperty("key", document.getKey().toString());
                    jsonDocument.addProperty("binaryData", DatatypeConverter.printBase64Binary(document.getDocumentBinaryData()));
                    return jsonDocument;
                }
            };
            gson = new GsonBuilder().registerTypeAdapter(DocumentImpl.class, serializer).create();
        }
        String actualFile = this.baseDirectory.toString() + File.separator + uri.getHost() + uri.getPath();
        File file = new File(actualFile);
        String directory = file.getParent();
        File tester = new File(directory);
        tester.mkdirs();
        File test = new File(actualFile + ".json");
        if (test.exists()) {
            this.delete(uri);
        }
        test = new File(actualFile + ".json");
        FileWriter newFile = new FileWriter(test);
        newFile.write(gson.toJson(val));
        newFile.close();

    }

    @Override
    public Document deserialize(URI uri) throws IOException {
        //don't forget null values
        if (uri == null) {
            throw new IllegalArgumentException();
        }
        String actualFile = this.baseDirectory.toString() + File.separator + uri.getHost() + uri.getPath() + ".json";
        Path path = Paths.get(actualFile);
        File exists = new File(actualFile);
        if (!exists.exists()) {
            return null;
        }
        String jsonFileToString = new String(Files.readAllBytes(path));
        GsonBuilder gson = new GsonBuilder();
        Gson toReturn = gson.create();
        if (jsonFileToString.contains("text")) {
            Document doc = toReturn.fromJson(jsonFileToString, DocumentImpl.class);
            this.delete(uri);
            return doc;
        }
        JsonDeserializer<Document> deserializer = new JsonDeserializer<Document>() {
            @Override
            public Document deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                byte[] base64Decoded = DatatypeConverter.parseBase64Binary(jsonObject.get("binaryData").getAsString());
                return new DocumentImpl(uri, base64Decoded);
            }
        };
        gson.registerTypeAdapter(DocumentImpl.class, deserializer);
        Gson customGson = gson.create();
        Document doc = customGson.fromJson(jsonFileToString, DocumentImpl.class);
        this.delete(uri);
        return doc;
    }

    @Override
    public boolean delete(URI uri) throws IOException {
        if (uri == null) {
            throw new IllegalArgumentException();
        }
        String newUri = uri.toString();
        String fileName = newUri.replaceFirst("http://", "");
        String actualFile = this.baseDirectory.toString() + File.separator + fileName + ".json";
        File delete = new File(actualFile);
        return delete.delete();
    }
}