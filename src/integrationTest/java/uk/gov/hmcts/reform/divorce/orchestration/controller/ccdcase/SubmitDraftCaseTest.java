package uk.gov.hmcts.reform.divorce.orchestration.controller.ccdcase;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.divorce.model.ccd.CoreCaseData;
import uk.gov.hmcts.reform.divorce.model.usersession.DivorceSession;
import uk.gov.hmcts.reform.divorce.orchestration.controller.MockedFunctionalTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.DataTransformationTestHelper.getExpectedTranslatedDraftCoreCaseData;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.DataTransformationTestHelper.getTestDivorceSessionData;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.DataTransformationTestHelper.getTestDraftDivorceSessionData;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class SubmitDraftCaseTest extends MockedFunctionalTest {

    private static final String SUBMISSION_CONTEXT_PATH = "/case";
    private static final Map<String, Object> CASE_DATA = Collections.singletonMap(D_8_DIVORCE_UNIT, TEST_COURT);
    private static final CaseDetails CASE_DETAILS =
        CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(CASE_DATA)
            .build();


    private DivorceSession testDivorceSessionData;
    private DivorceSession testDivorceSessionDataDraft;
    private CoreCaseData expectedTranslatedCcdSessionDataDraft;

    @Autowired
    private MockMvc webClient;

    @Before
    public void setUp() throws IOException {
        testDivorceSessionData = getTestDivorceSessionData();
        testDivorceSessionDataDraft = getTestDraftDivorceSessionData();
        expectedTranslatedCcdSessionDataDraft = getExpectedTranslatedDraftCoreCaseData();
    }

    @Test
    public void shouldSubmitDraftCase() throws Exception {

        stubMaintenanceServerEndpointForDraftSubmit(singletonMap(ID, TEST_CASE_ID));
        stubMaintenanceServerEndpointForRetrieveExisting(OK, null);

        final MvcResult result = webClient.perform(post(SUBMISSION_CONTEXT_PATH)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(testDivorceSessionDataDraft))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andReturn();

        final String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody, allOf(
            isJson(),
            hasJsonPath("$.caseId", equalTo(TEST_CASE_ID)),
            hasJsonPath("$.status", equalTo(SUCCESS_STATUS))
        ));

    }

    @Test
    public void shouldReturnBadRequestIfNoAuthenticationToken() throws Exception {
        webClient.perform(post(SUBMISSION_CONTEXT_PATH)
            .content(convertObjectToJsonString(testDivorceSessionData))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnBadRequestIfNoPayload() throws Exception {
        webClient.perform(post(SUBMISSION_CONTEXT_PATH)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnBadRequestIfDuplicateCase() throws Exception {

        stubMaintenanceServerEndpointForDraftSubmit(singletonMap(ID, TEST_CASE_ID));
        stubMaintenanceServerEndpointForRetrieveExisting(OK, convertObjectToJsonString(CASE_DETAILS));

        webClient.perform(post(SUBMISSION_CONTEXT_PATH)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    private void stubMaintenanceServerEndpointForDraftSubmit(Map<String, Object> response) {

        final String testCcdTranslatedData = convertObjectToJsonString(expectedTranslatedCcdSessionDataDraft);

        maintenanceServiceServer.stubFor(WireMock.post(SUBMISSION_CONTEXT_PATH)
            .withRequestBody(equalToJson(testCcdTranslatedData))
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(convertObjectToJsonString(response))));
    }

    private void stubMaintenanceServerEndpointForRetrieveExisting(HttpStatus status, String message) {
        maintenanceServiceServer.stubFor(WireMock.get(SUBMISSION_CONTEXT_PATH)
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(message)));
    }
}
