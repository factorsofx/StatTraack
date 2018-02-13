package com.factorsofx.stattrack.persist.mongo.provider;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class OffsetDateTimeCodecProvider implements CodecProvider
{
    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry)
    {
        if(OffsetDateTime.class.isAssignableFrom(clazz))
        {
            return new OffsetDateTimeCodec<>();
        }
        return null;
    }

    public static class OffsetDateTimeCodec<T> implements Codec<T>
    {
        @SuppressWarnings("unchecked") // Augh
        @Override
        public T decode(BsonReader reader, DecoderContext decoderContext)
        {
            return (T) OffsetDateTime.ofInstant(Instant.ofEpochMilli(reader.readDateTime()), ZoneOffset.UTC);
        }

        @Override
        public void encode(BsonWriter writer, T value, EncoderContext encoderContext)
        {
            OffsetDateTime odt = (OffsetDateTime) value;
            writer.writeDateTime(odt.toInstant().toEpochMilli());
        }

        @SuppressWarnings("unchecked") // Eugh
        @Override
        public Class getEncoderClass()
        {
            return OffsetDateTime.class;
        }
    }
}
