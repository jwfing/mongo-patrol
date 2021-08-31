package cn.leancloud.utils;

import cn.leancloud.LCObject;
import cn.leancloud.core.LeanCloud;
import com.google.gson.Gson;
import com.mongodb.DBObjectCodecProvider;
import com.mongodb.DBRefCodecProvider;
import com.mongodb.client.*;
import com.mongodb.client.gridfs.codecs.GridFSFileCodecProvider;
import com.mongodb.client.model.geojson.codecs.GeoJsonCodecProvider;
import org.bson.Document;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import com.google.gson.JsonObject;
import org.bson.json.JsonWriterSettings;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Sorts.orderBy;
import static com.mongodb.client.model.Sorts.descending;
import static java.util.Arrays.asList;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final CodecRegistry DEFAULT_REGISTRY = CodecRegistries.fromProviders(
            asList(new ValueCodecProvider(),
                    new BsonValueCodecProvider(),
                    new DocumentCodecProvider(),
                    new JsonDBRefCodecProvider(), //new DBRefCodecProvider(),
                    new DBObjectCodecProvider(),
                    new BsonValueCodecProvider(),
                    new GeoJsonCodecProvider(),
                    new GridFSFileCodecProvider()));

    private static final BsonTypeClassMap DEFAULT_BSON_TYPE_CLASS_MAP = new BsonTypeClassMap();

    public static final DocumentCodec documentCodec = new DocumentCodec(
            DEFAULT_REGISTRY,
            DEFAULT_BSON_TYPE_CLASS_MAP
    );

    private static JsonWriterSettings settings =JsonWriterSettings.builder()
            .dateTimeConverter(new JsonDateTimeConverter())
            .objectIdConverter(new JsonObjectIdConverter())
            .build();

    private static Random rand = new Random();
    private static Gson gson = new Gson();

    private static void scanCluster(String mongodbUri) {
        String cluster = mongodbUri.substring(0, 4);
        try (MongoClient mongoClient = MongoClients.create("mongodb://" + mongodbUri)) {
            MongoCursor<String> dbIterator = mongoClient.listDatabaseNames().iterator();
            while (dbIterator.hasNext()) {
                String databaseName = dbIterator.next();
                if ("admin".equals(databaseName) || "local".equals(databaseName) || "config".equals(databaseName)) {
                    continue;
                }
                MongoDatabase database = mongoClient.getDatabase(databaseName);
                MongoCursor<String> collIterator = database.listCollectionNames().iterator();
                while (collIterator.hasNext()) {
                    String collectionName = collIterator.next();
                    if (collectionName.startsWith("system.")) {
                        continue;
                    }
                    MongoCollection<Document> collection = database.getCollection(collectionName);
                    long documentCount = collection.estimatedDocumentCount();
                    if (documentCount > 0) {
                        try {
                            int range = documentCount > 200? 200: (int) documentCount;
                            int skip = rand.nextInt(range);
                            Document doc = collection.find(exists("createdAt"))
                                    .sort(orderBy(descending("createdAt")))
                                    .skip(skip)
                                    .first();
                            if (null != doc) {
                                String docJson = doc.toJson(settings, documentCodec);
                                long begin = System.currentTimeMillis();
                                JsonObject jsonObject = gson.fromJson(docJson, JsonObject.class);
                                long end = System.currentTimeMillis();
                                if (end - begin >= 10) {
                                    try {
                                        LCObject record = new LCObject("Suspect");
                                        record.put("cluster", cluster);
                                        record.put("database", databaseName);
                                        record.put("collection", collectionName);
                                        //record.put("document", jsonObject);
                                        record.put("docJson", docJson);
                                        record.save();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                    System.out.println(String.format("[WARN] db=%s, coll=%s, jsonCost=%d(ms), doc:%s",
                                            databaseName, collectionName, end-begin, docJson));
                                }
                            } else {
                                System.out.println(String.format("Irregular collection. db=%s, coll=%s, estimatedCount=%d, skip=%d",
                                        databaseName, collectionName, documentCount, skip));
                            }
                        } catch (Exception ex) {
                            System.err.println(String.format("Illegal document. db:%s, coll:%s", databaseName, collectionName));
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static void main( String[] args )
    {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("./mongocluster.properties")) {
            // load a properties file
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (args.length < 1) {
            System.out.println("Usage: app mongodbcluster(all, clusterName, or raw mongo Uri)");
            return;
        }
        List<String> clusters = new ArrayList<>(prop.entrySet().size());
        if (args[0].equalsIgnoreCase("all")) {
            for (Map.Entry entry: prop.entrySet()) {
                clusters.add((String) entry.getValue());
            }
        } else {
            String targetCluster = (String) prop.get(args[0]);
            if (null == targetCluster || targetCluster.length() < 1) {
                clusters.add(args[0]);
            } else {
                clusters.add(targetCluster);
            }
        }

        LeanCloud.initialize("RGM6sPnzVjLxva3WBR5CBrRo-gzGzoHsz", "hU4UT7ornEsiyyj9GCdVRJ8D",
                "https://rgm6spnz.lc-cn-n1-shared.com");

        for (String mongodbUri: clusters) {
            try {
                scanCluster(mongodbUri);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
