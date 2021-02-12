package uk.gov.hmcts.reform.divorce.orchestration.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ERROR;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
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
    public void givenCMSThrowsException_whenGetCase_thenPropagateException() throws Exception {
        stubGetCaseFromCMS(HttpStatus.INTERNAL_SERVER_ERROR, TEST_ERROR);

        webClient.perform(get(GET_CASE_CONTEXT_PATH)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .accept(APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString(TEST_ERROR)));
    }

    @Test
    public void givenNoCaseExists_whenGetCase_thenReturnEmptyResponse() throws Exception {
        stubGetCaseFromCMS(HttpStatus.NOT_FOUND, TEST_ERROR);

        webClient.perform(get(GET_CASE_CONTEXT_PATH)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .accept(APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void givenMultipleCases_whenGetCase_thenPropagateException() throws Exception {
        stubGetMultipleCaseFromCMS();

        webClient.perform(get(GET_CASE_CONTEXT_PATH)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .accept(APPLICATION_JSON))
            .andExpect(status().isMultipleChoices())
            .andExpect(content().string(""));
    }

    @Test
    public void givenCaseExists_whenGetCase_thenReturnCaseInformation() throws Exception {
        stubGetCaseFromCMS(CASE_DETAILS);

        CaseDataResponse expected = CaseDataResponse.builder()
            .data(CASE_DATA)
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .court(TEST_COURT)
            .build();

        webClient.perform(get(GET_CASE_CONTEXT_PATH)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));
    }

    private void stubGetCaseFromCMS(CaseDetails caseDetails) {
        stubGetCaseFromCMS(OK, convertObjectToJsonString(caseDetails));
    }

    private void stubGetCaseFromCMS(HttpStatus status, String responseBody) {
        maintenanceServiceServer.stubFor(WireMock.get(GET_CASE_CONTEXT_PATH)
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(responseBody)));
    }

    private void stubGetMultipleCaseFromCMS() {
        stubGetCaseFromCMS(HttpStatus.MULTIPLE_CHOICES, "");
    }
}
