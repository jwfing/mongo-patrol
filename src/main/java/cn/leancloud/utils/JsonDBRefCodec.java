package cn.leancloud.utils;

import com.mongodb.DBRef;
import com.mongodb.assertions.Assertions;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;


public class JsonDBRefCodec implements Codec<DBRef> {
    private final CodecRegistry registry;

    public JsonDBRefCodec(CodecRegistry registry) {
        this.registry = (CodecRegistry) Assertions.notNull("registry", registry);
    }

    public void encode(BsonWriter writer, DBRef value, EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeString("_class", value.getCollectionName());
        writer.writeName("_objectId");
        Codec codec = this.registry.get(value.getId().getClass());
        codec.encode(writer, value.getId(), encoderContext);
        if (value.getDatabaseName() != null) {
            writer.writeString("_database", value.getDatabaseName());
        }

        writer.writeEndDocument();
    }

    public Class<DBRef> getEncoderClass() {
        return DBRef.class;
    }

    public DBRef decode(BsonReader reader, DecoderContext decoderContext) {
        throw new UnsupportedOperationException("DBRefCodec does not support decoding");
    }
}