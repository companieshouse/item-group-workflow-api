package uk.gov.companieshouse.item.group.workflow.api.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
@AutoConfigureMockMvc
@SpringBootTest
public class ItemGroupControllerIntegrationTest {
//    private static final String COMPANY_NAME = "Outlandish Enterprises";
//    private static final String COMPANY_NUMBER = "1337";
//    @Autowired
//    private MockMvc mockMvc;
//    @Autowired
//    private ObjectMapper mapper;
//    @Autowired
//    private LoggingUtils logger;
//    @MockBean
//    private ItemGroupsService itemGroupsService;
//    @Autowired
//    private ItemGroupsRepository itemGroupsRepository;
//
//    @Test
//    @DisplayName("Returns status OK (200)")
//    public void get200response_returnAppNameTest() throws Exception {
//        final var resultActions = mockMvc.perform(get("${uk.gov.companieshouse.item.group.workflow.api.sanity.controller.ok}"))
//            .andExpect(status().isOk());
//
//        final var result = resultActions.andReturn();
//        final var response = result.getResponse();
//        final var contentAsString = response.getContentAsString();
//        logger.getLogger().info("XXX = " + contentAsString);
//    }
//
//    @Test
//    @DisplayName("Returns status CREATED (201)")
//    public void get201responseTest() throws Exception {
//        mockMvc.perform(get("${uk.gov.companieshouse.item.group.workflow.api.sanity.controller.created}"))
//            .andExpect(status().isCreated());
//    }
//
//    @Test
//    @DisplayName("Returns status UNAUTHORIZED (401)")
//    public void get401responseTest() throws Exception {
//        mockMvc.perform(get("${uk.gov.companieshouse.item.group.workflow.api.sanity.controller.unauthorized}"))
//            .andExpect(status().isUnauthorized());
//    }

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