package uk.gov.companieshouse.itemgroupworkflowapi.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.JsonMergePatch;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.itemgroupworkflowapi.dto.TemporaryItemPatchDto;

import java.util.Map;

/**
 * TODO DCAC-78 Replace this.
 * Temporary stand-in for the PatchMerger class, to be used whilst the Item entity is represented by a Map.
 */
@Component
public class TemporaryMapPatchMerger {

    private final ObjectMapper objectMapper;

    public TemporaryMapPatchMerger(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    public Map<String, Object> mergePatch(final JsonMergePatch mergePatch, final Map<String, Object> item) {


        try {
            final TemporaryItemPatchDto dto =
                    objectMapper.readValue(mergePatch.toJsonValue().toString(), TemporaryItemPatchDto.class);
            item.put("digital_document_location", dto.getDigitalDocumentLocation());
            item.put("status", dto.getStatus());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

       return item;

    }
}
