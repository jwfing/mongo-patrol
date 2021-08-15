package cn.leancloud.utils;

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

import java.util.Random;

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
                    new DBRefCodecProvider(),
                    new DBObjectCodecProvider(),
                    new BsonValueCodecProvider(),
                    new GeoJsonCodecProvider(),
                    new GridFSFileCodecProvider()));

    private static final BsonTypeClassMap DEFAULT_BSON_TYPE_CLASS_MAP = new BsonTypeClassMap();

    private static final DocumentCodec documentCodec = new DocumentCodec(
            DEFAULT_REGISTRY,
            DEFAULT_BSON_TYPE_CLASS_MAP
    );

    public static void main( String[] args )
    {
        Random rand = new Random();
        Gson gson = new Gson();
        String uri = "mongodb://localhost:27017";
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoCursor<String> dbIterator = mongoClient.listDatabaseNames().iterator();
            while (dbIterator.hasNext()) {
                String databaseName = dbIterator.next();
                MongoDatabase database = mongoClient.getDatabase(databaseName);
                MongoCursor<String> collIterator = database.listCollectionNames().iterator();
                while (collIterator.hasNext()) {
                    String collectionName = collIterator.next();
                    MongoCollection<Document> collection = database.getCollection(collectionName);
                    long documentCount = collection.estimatedDocumentCount();
                    if (documentCount > 0) {
                        int range = documentCount > 200? 200: (int) documentCount;
                        int skip = rand.nextInt(range);
                        Document doc = collection.find(exists("_id"))
                                .sort(orderBy(descending("createdAt")))
                                .skip(skip)
                                .first();
                        if (null != doc) {
                            String docJson = doc.toJson(documentCodec);
                            long begin = System.currentTimeMillis();
                            JsonObject jsonDoc = gson.fromJson(docJson, JsonObject.class);
                            long end = System.currentTimeMillis();
                            String tag = end -begin > 50? "Warning": "Notice";
                            System.out.println(String.format("[%s] db=%s, coll=%s, jsonCost=%d(ms), doc:%s", tag,
                                    databaseName, collectionName, end-begin, docJson));
                        } else {
                            System.out.println(String.format("Irregular collection. db=%s, coll=%s, estimatedCount=%d, skip=%d",
                                    databaseName, collectionName, documentCount, skip));
                        }
                    } else {
                        System.out.println(String.format("empty collection, skip. db=%s, coll=%s",
                                databaseName, collectionName));
                    }
                }
            }
        }
    }
}
