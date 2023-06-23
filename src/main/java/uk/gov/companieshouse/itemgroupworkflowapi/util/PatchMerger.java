package uk.gov.companieshouse.itemgroupworkflowapi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.JsonMergePatch;
import jakarta.json.JsonValue;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;

@Component
public class PatchMerger {

    private final ObjectMapper jsonpObjectMapper;

    /**
     * Constructor.
     * @param jsonpObjectMapper mapper used by this to convert between {@link JsonMergePatch} and
     *                     {@link Item} instances
     */
    public PatchMerger(final ObjectMapper jsonpObjectMapper) {
        this.jsonpObjectMapper = jsonpObjectMapper;
    }

    /**
     * Applies the changes captured in the merge patch to the target bean.
     * See <a href="https://cassiomolin.com/2019/06/10/using-http-patch-in-spring/">Using HTTP PATCH in Spring</a>.
     * @param mergePatch JSON merge patch
     * @param targetBean the bean to be patched
     * @param beanClass the class of the bean to be patched
     * @param <T> the type of the bean
     * @return the patched bean
     */
    public <T> T mergePatch(final JsonMergePatch mergePatch, final T targetBean, final Class<T> beanClass) {

        // Convert the Java bean to a JSON document
        JsonValue target = jsonpObjectMapper.convertValue(targetBean, JsonValue.class);

        // Apply the JSON Merge Patch to the JSON document
        JsonValue patched = mergePatch.apply(target);

        // Convert the JSON document to a Java bean and return it
        return jsonpObjectMapper.convertValue(patched, beanClass);
    }
}
