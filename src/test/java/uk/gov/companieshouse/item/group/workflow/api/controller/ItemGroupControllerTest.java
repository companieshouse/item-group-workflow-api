package uk.gov.companieshouse.item.group.workflow.api.controller;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ItemGroupControllerTest {
//    private static final String COMPANY_NAME = "Outlandish Enterprises";
//    private static final String COMPANY_NUMBER = "1337";
//    @Mock
//    private LoggingUtils loggingUtils;
//    @Mock
//    private Logger logger;
//    @InjectMocks
//    private ItemGroupController controller;
//    @Mock
//    private ItemGroupsService itemGroupsService;
//    @Mock
//    private ItemGroupsRepository itemGroupsRepository;
//
//    @BeforeEach
//    void beforeEach() {
//        when(loggingUtils.getLogger()).thenReturn(logger);
//    }
//    @AfterEach
//    void afterEach() {
//        logger = null;
//    }
//
//    @Test
//    void get200response_returnAppName() {
//        final ResponseEntity<String> response = controller.get200response_returnAppName(); // GET
//        assertThat(response.getStatusCode(), is(HttpStatus.OK));
//        assertThat(response.getBody(), Matchers.is(LoggingUtilsConfiguration.APPLICATION_NAMESPACE));
//    }
//
//    @Test
//    void get201response() {
//        final ResponseEntity<Void> response = controller.get201response();  // GET
//        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
//    }
//
//    @Test
//    void get401response() {
//        final ResponseEntity<Void> response = controller.get401response();  // GET
//        assertThat(response.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
//    }

//    @Test
//    void postDtoTest_returnDto() {
//        //
//        // Init the DTO
//        //
//        final TestDTO testDTO = new TestDTO();
//        testDTO.setCompanyNumber(COMPANY_NUMBER);
//        testDTO.setCompanyName(COMPANY_NAME);
//        //
//        // Make the POST request and ensure we have HttpStatus.CREATED
//        //
//        final ResponseEntity<Object> response = controller.postDtoTest_returnDto(testDTO);    // POST
//        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
//        //
//        // Check we got the correct DTO values back.
//        //
//        final TestDTO responseDTO = (TestDTO) response.getBody();
//        assert(responseDTO != null);
//        assertThat(responseDTO.getCompanyNumber(), is(COMPANY_NUMBER));
//        assertThat(responseDTO.getCompanyName(), is(COMPANY_NAME));
//    }
}
