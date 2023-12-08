package uk.gov.companieshouse.itemgroupworkflowapi.kafka;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import uk.gov.companieshouse.itemgroupprocessed.ItemGroupProcessed;

public class ItemGroupProcessedAvroSerializer implements Serializer<ItemGroupProcessed> {

    @Override
    public byte[] serialize(String topic, ItemGroupProcessed data) {
        final DatumWriter<ItemGroupProcessed> datumWriter = new SpecificDatumWriter<>();

        try (final var out = new ByteArrayOutputStream()) {
            final var encoder = EncoderFactory.get().binaryEncoder(out, null);
            datumWriter.setSchema(data.getSchema());
            datumWriter.write(data, encoder);
            encoder.flush();

            byte[] serializedData = out.toByteArray();
            encoder.flush();

            return serializedData;
        } catch (IOException e) {
            throw new SerializationException("Error when serializing ItemGroupProcessed to byte[]");
        }
    }
}
