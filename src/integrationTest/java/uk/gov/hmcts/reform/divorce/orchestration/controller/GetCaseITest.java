package uk.gov.hmcts.reform.divorce.orchestration.controller;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.GetCaseResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.USER_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AMEND_PETITION_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class GetCaseITest extends IdamTestSupport {

    private static final String GET_CASE_CONTEXT_PATH = "/case";

    private static final Map<String, Object> CASE_DATA = Collections.singletonMap(D_8_DIVORCE_UNIT, TEST_COURT);
    private static final CaseDetails CASE_DETAILS =
        CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(CASE_DATA)
            .build();


    @Autowired
    private MockMvc webClient;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @Value("${ccd.jurisdictionid}")
    private String jurisdictionId;

    @Value("${ccd.casetype}")
    private String caseType;

    @Before
    public void setUp() {
        stubUserDetailsEndpoint(OK, AUTH_TOKEN, convertObjectToJsonString(UserDetails.builder().email(TEST_EMAIL).build()));
    }

    @Test
    public void givenNoAuthToken_whenGetCase_thenReturnBadRequest() throws Exception {
        webClient.perform(get(GET_CASE_CONTEXT_PATH)
            .accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenInvalidUserToken_whenGetCase_thenReturnForbiddenError() throws Exception {
        final String message = "some message";
        stubUserDetailsEndpoint(HttpStatus.FORBIDDEN, USER_TOKEN, message);

        webClient.perform(get(GET_CASE_CONTEXT_PATH)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .accept(APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(content().string(containsString(message)));
    }

    @Test
    public void givenCouldNotConnectToAuthService_whenGetCase_thenReturnHttp503() throws Exception {
        final String userDetails = getCitizenUserDetails();
        stubUserDetailsEndpoint(HttpStatus.OK, USER_TOKEN, userDetails);

        when(serviceTokenGenerator.generate()).thenThrow(new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        webClient.perform(MockMvcRequestBuilders.get(GET_CASE_CONTEXT_PATH)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isServiceUnavailable());
    }

    @Test
    public void givenNoCaseExistsInCcd_whenGetCase_thenReturn404Response() throws Exception {
        final String message = getCitizenUserDetails();

        stubUserDetailsEndpoint(HttpStatus.OK, USER_TOKEN, message);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);

        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, TEST_SERVICE_TOKEN, TEST_USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(null);

        webClient.perform(get(GET_CASE_CONTEXT_PATH)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .accept(APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }


    @Test
    public void givenMultipleCaseInCcd_whenGetCase_thenReturnReturn300() throws Exception {
        final String message = getCitizenUserDetails();

        stubUserDetailsEndpoint(HttpStatus.OK, USER_TOKEN, message);

        final uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails1 = createCaseDetails(TEST_STATE);
        final uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails2 = createCaseDetails(TEST_STATE);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, TEST_SERVICE_TOKEN, TEST_USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(List.of(caseDetails1, caseDetails2));

        webClient.perform(MockMvcRequestBuilders.get(GET_CASE_CONTEXT_PATH)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isMultipleChoices());
    }

    @Test
    public void givenCaseExists_whenGetCase_thenReturnCaseInformation() throws Exception {
        final String message = getCitizenUserDetails();

        stubUserDetailsEndpoint(HttpStatus.OK, USER_TOKEN, message);

        final uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = createCaseDetails(TEST_STATE);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, TEST_SERVICE_TOKEN, TEST_USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(Collections.singletonList(caseDetails));

        webClient.perform(MockMvcRequestBuilders.get(GET_CASE_CONTEXT_PATH)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                .json(ObjectMapperTestUtil
                    .convertObjectToJsonString(expectedResponse())));
    }

    @Test
    public void givenAmendedAndCompletedCaseExists_whenGetCase_thenReturnCompletedCase() throws Exception {
        final String message = getCitizenUserDetails();

        stubUserDetailsEndpoint(HttpStatus.OK, USER_TOKEN, message);

        final uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails1 = createCaseDetails(TEST_STATE);
        final uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails2 = createCaseDetails(AMEND_PETITION_STATE);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, TEST_SERVICE_TOKEN, TEST_USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(List.of(caseDetails1, caseDetails2));

        webClient.perform(MockMvcRequestBuilders.get(GET_CASE_CONTEXT_PATH)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                .json(ObjectMapperTestUtil
                    .convertObjectToJsonString(expectedResponse())));
    }

    private GetCaseResponse expectedResponse() {
        GetCaseResponse getCaseResponse = GetCaseResponse.builder()
            .id("1")
            .state(TEST_STATE)
            .data(ImmutableMap.of(D_8_PETITIONER_EMAIL, TEST_EMAIL))
            .build();
        return getCaseResponse;
    }

    public uk.gov.hmcts.reform.ccd.client.model.CaseDetails createCaseDetails(String state) {
        return uk.gov.hmcts.reform.ccd.client.model.CaseDetails
            .builder()
            .id(1L)
            .state(state)
            .data(ImmutableMap.of(D_8_PETITIONER_EMAIL, TEST_EMAIL))
            .build();
    }
}
