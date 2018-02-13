package com.factorsofx.stattrack.persist.mongo.provider;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class EnumCodecProvider implements CodecProvider
{
    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry)
    {
        if(clazz.isEnum())
        {
            return new EnumCodec<>(clazz);
        }
        return null;
    }

    private static class EnumCodec<T> implements Codec<T>
    {
        private Class<T> clazz;

        EnumCodec(Class<T> clazz)
        {
            this.clazz = clazz;
        }

        @SuppressWarnings("unchecked") // jesus I hate this so much
        @Override
        public T decode(BsonReader reader, DecoderContext decoderContext)
        {
            return (T)Enum.valueOf((Class<? extends Enum>)clazz, reader.readString());
        }

        @Override
        public void encode(BsonWriter writer, T value, EncoderContext encoderContext)
        {
            writer.writeString(((Enum)value).name());
        }

        @Override
        public Class<T> getEncoderClass()
        {
            return clazz;
        }
    }
}
