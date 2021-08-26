package cn.leancloud.utils;

import org.bson.json.Converter;
import org.bson.json.StrictJsonWriter;

public class BsonRefConverter implements Converter<String> {
    @Override
    public void convert(String s, StrictJsonWriter strictJsonWriter) {
        System.out.println("encounter symble: " + s);
        strictJsonWriter.writeString(s);
    }
}
