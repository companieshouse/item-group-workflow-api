package uk.gov.companieshouse.itemgroupworkflowapi.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests the {@link FieldNameConverter} class.
 */
@SpringBootTest
@EmbeddedKafka
class FieldNameConverterTest {

    @Qualifier("fieldNameConverter")
    @Autowired
    private FieldNameConverter converterUnderTest;

    @Test
    void toSnakeCaseWorksAsExpected() {
        assertThat(converterUnderTest.fromUpperCamelToSnakeCase("itemCosts"), is("item_costs"));
        assertThat(converterUnderTest.fromUpperCamelToSnakeCase("item"), is("item"));
        assertThat(converterUnderTest.fromUpperCamelToSnakeCase("certIncConLast"), is("cert_inc_con_last"));
        assertThat(converterUnderTest.fromUpperCamelToSnakeCase("isPostalDelivery"), is("postal_delivery"));
    }
}
