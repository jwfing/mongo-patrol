package cn.leancloud.utils;

import org.bson.json.Converter;
import org.bson.json.StrictJsonWriter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class JsonDateTimeConverter implements Converter<Long> {
    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_INSTANT
            .withZone(ZoneId.of("UTC"));

    @Override
    public void convert(Long value, StrictJsonWriter strictJsonWriter) {
        try {
            Instant instant = new Date(value).toInstant();
            String s = DATE_TIME_FORMATTER.format(instant);
            strictJsonWriter.writeString(s);
        } catch (Exception e) {
            System.out.println(String.format("Fail to convert offset %d to JSON date", value) + e);
        }
    }
}
