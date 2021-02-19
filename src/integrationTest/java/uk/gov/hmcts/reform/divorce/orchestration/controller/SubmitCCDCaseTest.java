package uk.gov.hmcts.reform.divorce.orchestration.controller;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.model.usersession.DivorceSession;
import uk.gov.hmcts.reform.divorce.orchestration.client.CMSClient;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.USER_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.DataTransformationTestHelper.getTestDivorceSessionData;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.DataTransformationTestHelper.getTestDraftDivorceSessionData;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class SubmitCCDCaseTest extends IdamTestSupport {

    private static final String GET_CASE_CONTEXT_PATH = "/case";

    private DivorceSession testDivorceSessionData;
    private DivorceSession testDivorceSessionDataDraft;

    private static final Map<String, Object> CASE_DATA = Collections.singletonMap(D_8_DIVORCE_UNIT, TEST_COURT);

    @Autowired
    private MockMvc webClient;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private CMSClient cmsClient;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @Value("${ccd.jurisdictionid}")
    private String jurisdictionId;

    @Value("${ccd.casetype}")
    private String caseType;

    @Before
    public void setUp() throws IOException {
        stubUserDetailsEndpoint(OK, AUTH_TOKEN, convertObjectToJsonString(UserDetails.builder().email(TEST_EMAIL).build()));
        testDivorceSessionData = getTestDivorceSessionData();
        testDivorceSessionDataDraft = getTestDraftDivorceSessionData();
    }

    @Test
    public void givenNoAuthToken_whenPostCase_thenReturnBadRequest() throws Exception {
        webClient.perform(post(GET_CASE_CONTEXT_PATH)
            .content(convertObjectToJsonString(testDivorceSessionData))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenNoPayload_whenPostCase_thenReturnBadRequest() throws Exception {
        webClient.perform(post(GET_CASE_CONTEXT_PATH)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenNoCaseExistsInCcd_whenPostCase_thenSubmitCase() throws Exception {
        final Map<String, Object> requestPayload = singletonMap("requestPayloadKey", "requestPayloadValue");

        final String message = getCitizenUserDetails();

        stubUserDetailsEndpoint(HttpStatus.OK, USER_TOKEN, message);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);

        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, TEST_SERVICE_TOKEN, TEST_USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(emptyList());

        final MvcResult result = webClient.perform(post(GET_CASE_CONTEXT_PATH)
            .header(AUTHORIZATION, USER_TOKEN)
            .content(convertObjectToJsonString(testDivorceSessionDataDraft))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andReturn();
    }


    @Test
    public void givenCaseExists_whenPostCase_thenThrowException() throws Exception {
        final String message = getCitizenUserDetails();

        stubUserDetailsEndpoint(HttpStatus.OK, USER_TOKEN, message);

        final CaseDetails caseDetails = createCaseDetails(TEST_STATE);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, TEST_SERVICE_TOKEN, TEST_USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(Collections.singletonList(caseDetails));

        webClient.perform(post(GET_CASE_CONTEXT_PATH)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    public uk.gov.hmcts.reform.ccd.client.model.CaseDetails createCaseDetails(String state) {
        return CaseDetails
            .builder()
            .id(1L)
            .state(state)
            .data(ImmutableMap.of(D_8_PETITIONER_EMAIL, TEST_EMAIL))
            .build();
    }
}
