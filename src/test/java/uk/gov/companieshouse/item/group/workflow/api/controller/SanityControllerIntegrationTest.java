package uk.gov.companieshouse.item.group.workflow.api.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.companieshouse.item.group.workflow.api.logging.LoggingUtils;
import uk.gov.companieshouse.item.group.workflow.api.repository.ItemGroupsRepository;
import uk.gov.companieshouse.item.group.workflow.api.service.ItemGroupsService;

import java.io.UnsupportedEncodingException;

@DirtiesContext
@AutoConfigureMockMvc
@SpringBootTest
public class SanityControllerIntegrationTest {
    private static final String COMPANY_NAME = "Outlandish Enterprises";
    private static final String COMPANY_NUMBER = "1337";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private LoggingUtils logger;
    @MockBean
    private ItemGroupsService itemGroupsService;
    @Autowired
    private ItemGroupsRepository itemGroupsRepository;

    @Test
    @DisplayName("Returns status OK (200)")
    public void get200response_returnAppNameTest() throws Exception {
        final var resultActions = mockMvc.perform(get("${uk.gov.companieshouse.item.group.workflow.api.sanity.controller.ok}"))
            .andExpect(status().isOk());

        final var result = resultActions.andReturn();
        final var response = result.getResponse();
        final var contentAsString = response.getContentAsString();
        logger.getLogger().info("XXX = " + contentAsString);
    }

    @Test
    @DisplayName("Returns status CREATED (201)")
    public void get201responseTest() throws Exception {
        mockMvc.perform(get("${uk.gov.companieshouse.item.group.workflow.api.sanity.controller.created}"))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Returns status UNAUTHORIZED (401)")
    public void get401responseTest() throws Exception {
        mockMvc.perform(get("${uk.gov.companieshouse.item.group.workflow.api.sanity.controller.unauthorized}"))
            .andExpect(status().isUnauthorized());
    }

//    @Test
//    @DisplayName("POST DTO and return status CREATED (201) and check result DTO has correct values")
//    public void dtoTestPostTest() throws Exception {
//        //
//        // Init the DTO
//        //
//        final TestDTO testDTO = new TestDTO();
//        testDTO.setCompanyNumber(COMPANY_NUMBER);
//        testDTO.setCompanyName(COMPANY_NAME);
//        //
//        // Make the POST request and ensure status is HttpStatus.CREATED
//        //
//        final var resultActions = mockMvc.perform(post("${uk.gov.companieshouse.item.group.workflow.api.sanity.controller.dto_test}")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(mapper.writeValueAsString(testDTO)))
//            .andExpect(status().isCreated());
//        //
//        // Check the correct DTO values are returned.
//        //
//        TestDTO responseDTO = getResponseDTO(resultActions);
//        assert(responseDTO != null);
//        assertThat(responseDTO.getCompanyNumber(), is(COMPANY_NUMBER));
//        assertThat(responseDTO.getCompanyName(), is(COMPANY_NAME));
//    }
//
//    private TestDTO getResponseDTO(final ResultActions resultActions)
//        throws JsonProcessingException, UnsupportedEncodingException {
//        final var result = resultActions.andReturn();
//        final var response = result.getResponse();
//        final var contentAsString = response.getContentAsString();
//        return mapper.readValue(contentAsString, TestDTO.class);
//    }
}