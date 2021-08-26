package cn.leancloud.utils;

import org.bson.json.Converter;
import org.bson.json.StrictJsonWriter;
import org.bson.types.ObjectId;

public class JsonObjectIdConverter implements Converter<ObjectId> {
    @Override
    public void convert(ObjectId objectId, StrictJsonWriter strictJsonWriter) {
        strictJsonWriter.writeString(objectId.toHexString());
    }
}
