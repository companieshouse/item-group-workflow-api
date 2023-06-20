package uk.gov.companieshouse.itemgroupworkflowapi.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;

import jakarta.json.JsonMergePatch;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests the {@link JsonMergePatchHttpMessageConverter} class.
 */
@ExtendWith(MockitoExtension.class)
class JsonMergePatchHttpMessageConverterTest {

    private static final MediaType SUPPORTED_MEDIA_TYPE = PatchMediaType.APPLICATION_MERGE_PATCH;

    @InjectMocks
    @Spy
    private JsonMergePatchHttpMessageConverter  converterUnderTest;

    @Mock
    private HttpInputMessage inputMessage;

    @Mock
    private HttpOutputMessage outputMessage;

    @Mock
    private HttpHeaders headers;

    @Mock
    private ByteArrayOutputStream byteArrayOutputStream;

    @Test
    @DisplayName("application/merge-patch+json is only supported media type")
    void mergePatchJsonIsOnlySupportedMediaType() {
        assertThat(converterUnderTest.getSupportedMediaTypes(), is(singletonList(SUPPORTED_MEDIA_TYPE)));
    }

    @Test
    @DisplayName("Can read JsonMergePatch instances")
    void canReadJsonMergePatches() {
        assertThat(converterUnderTest.canRead(JsonMergePatch.class, SUPPORTED_MEDIA_TYPE), is(true));
    }

    @Test
    @DisplayName("Reads and writes JsonMergePatch instances")
    void readsAndWritesJsonMergePatch() throws IOException {

        // Given
        when(inputMessage.getBody()).thenReturn(new ByteArrayInputStream("{ \"id\": 1 }".getBytes(UTF_8)));
        when(outputMessage.getHeaders()).thenReturn(headers);
        when(outputMessage.getBody()).thenReturn(/*new ByteArrayOutputStream()*/byteArrayOutputStream);

        // When
        final JsonMergePatch patch = converterUnderTest.read(JsonMergePatch.class, inputMessage);
        // Then
        verify(converterUnderTest).readInternal(JsonMergePatch.class, inputMessage);

        // When
        converterUnderTest.write(patch, SUPPORTED_MEDIA_TYPE, outputMessage);
        // Then
        verify(byteArrayOutputStream).write(any(byte[].class), anyInt(), anyInt());
        verify(byteArrayOutputStream).flush();
    }

}
