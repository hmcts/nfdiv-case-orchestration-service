package uk.gov.hmcts.reform.divorce.orchestration.controller.ccdcase;

import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.divorce.model.usersession.DivorceSession;
import uk.gov.hmcts.reform.divorce.orchestration.controller.MockedFunctionalTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.DataTransformationTestHelper.getTestDivorceSessionData;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class PatchCaseTest extends MockedFunctionalTest {

    private static final String IDAM_USER_DETAILS_CONTEXT_PATH = "/details";
    private static final String PATCH_PATH = "/case";
    private static final String USER_ID = "1";
    private static final String CITIZEN_ROLE = "citizen";
    private static final String CASE_ID = "123456789";

    private static final String DIVORCE_CASE_PATCH_EVENT_SUMMARY = "Divorce case patch event";
    private static final String DIVORCE_CASE_PATCH_EVENT_DESCRIPTION = "Patching Divorce Case";

    private DivorceSession divorceSession;

    @Value("${ccd.jurisdictionid}")
    private String jurisdictionId;

    @Value("${ccd.casetype}")
    private String caseType;

    @Value("${ccd.eventid.patch}")
    private String patchEventId;

    @Autowired
    private MockMvc webClient;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @Before
    public void setUp() throws IOException {
        divorceSession = getTestDivorceSessionData();
    }

    @Test
    public void shouldPatchCase() throws Exception {

        final Map<String, Object> caseData = new HashMap<>();
        final String caseDataWithIdJson = "{\"id\": \"123456789\", \"data\": {}}";
        final String userDetails = getUserDetailsForRole();
        final CaseDetails caseDetails = CaseDetails.builder().build();

        final StartEventResponse startEventResponse = createStartEventResponse();
        final CaseDataContent caseDataContent = createCaseDataContent(caseData, startEventResponse);

        stubUserDetailsEndpoint(userDetails);

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .startEventForCitizen(
                BEARER_AUTH_TOKEN,
                TEST_SERVICE_TOKEN,
                USER_ID,
                jurisdictionId,
                caseType,
                CASE_ID,
                patchEventId))
            .thenReturn(startEventResponse);
        when(coreCaseDataApi
            .submitEventForCitizen(
                BEARER_AUTH_TOKEN,
                TEST_SERVICE_TOKEN,
                USER_ID,
                jurisdictionId,
                caseType,
                CASE_ID,
                true,
                caseDataContent))
            .thenReturn(caseDetails);

        webClient.perform(patch(PATCH_PATH)
            .content(caseDataWithIdJson)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnBadRequestIfNoAuthenticationToken() throws Exception {
        webClient.perform(patch(PATCH_PATH)
            .content(convertObjectToJsonString(divorceSession))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    private String getUserDetailsForRole() {
        return "{\"id\":\"" + USER_ID
            + "\",\"email\":\"" + TEST_USER_EMAIL
            + "\",\"forename\":\"forename\",\"surname\":\"Surname\",\"roles\":[\"" + CITIZEN_ROLE + "\"]}";
    }

    private StartEventResponse createStartEventResponse() {
        return StartEventResponse.builder()
            .eventId(TEST_EVENT_ID)
            .token(TEST_TOKEN)
            .build();
    }

    private void stubUserDetailsEndpoint(final String message) {
        idamServer.stubFor(get(IDAM_USER_DETAILS_CONTEXT_PATH)
            .withHeader(AUTHORIZATION, new EqualToPattern(BEARER_AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(message)));
    }

    private CaseDataContent createCaseDataContent(final Map<String, Object> caseData, final StartEventResponse startEventResponse) {

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(DIVORCE_CASE_PATCH_EVENT_SUMMARY)
                    .description(DIVORCE_CASE_PATCH_EVENT_DESCRIPTION)
                    .build()
            ).data(caseData)
            .build();
    }
}
