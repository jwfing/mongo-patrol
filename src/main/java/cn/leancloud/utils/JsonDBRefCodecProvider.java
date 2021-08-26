package cn.leancloud.utils;

import com.mongodb.DBRef;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class JsonDBRefCodecProvider implements CodecProvider {
    public JsonDBRefCodecProvider() {
    }

    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        return clazz == DBRef.class ? (Codec<T>) new JsonDBRefCodec(registry) : null;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else {
            return o != null && this.getClass() == o.getClass();
        }
    }

    public int hashCode() {
        return 0;
    }
}
