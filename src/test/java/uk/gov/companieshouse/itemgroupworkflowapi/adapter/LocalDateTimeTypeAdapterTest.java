package uk.gov.companieshouse.itemgroupworkflowapi.adapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.DateTimeException;
import java.time.LocalDateTime;

@SpringBootTest
@EmbeddedKafka
public class LocalDateTimeTypeAdapterTest {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();

    @Test
    public void testSerialize() {
        LocalDateTime localDateTime = LocalDateTime.of(2023, 6, 6, 12, 34, 56);
        String json = gson.toJson(localDateTime);
        Assertions.assertEquals("\"6::Jun::2023 12::34::56\"", json);
    }

    @Test
    public void testDeserialize() {
        String json = "\"6::Jun::2023 12::34::56\"";
        LocalDateTime localDateTime = gson.fromJson(json, LocalDateTime.class);
        Assertions.assertEquals(LocalDateTime.of(2023, 6, 6, 12, 34, 56), localDateTime);
    }

    @Test
    public void testDeserializeInvalidFormat() {
        String json = "\"6::Jun::2023 12::89::56\"";
        Assertions.assertThrows(DateTimeException.class, () -> gson.fromJson(json, LocalDateTime.class));
    }

}
