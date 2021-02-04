package uk.gov.hmcts.reform.divorce.orchestration.controller.draftcase;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.divorce.model.ccd.CoreCaseData;
import uk.gov.hmcts.reform.divorce.model.response.ValidationResponse;
import uk.gov.hmcts.reform.divorce.model.usersession.DivorceSession;
import uk.gov.hmcts.reform.divorce.orchestration.controller.MockedFunctionalTest;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.CourtLookupService;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.DataTransformationTestHelper.getExpectedTranslatedDraftCoreCaseData;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.DataTransformationTestHelper.getTestDivorceSessionData;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.DataTransformationTestHelper.getTestDraftDivorceSessionData;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class SubmitDraftCaseTest extends MockedFunctionalTest {

    private static final String DRAFT_API_URL = "/case";
    private static final String SUBMISSION_CONTEXT_PATH = "/case";

    private DivorceSession testDivorceSessionData;
    private DivorceSession testDivorceSessionDataDraft;
    private CoreCaseData expectedTranslatedCcdSessionDataDraft;

    private static final ValidationResponse validationResponseOk = ValidationResponse.builder().build();
    private static final ValidationResponse validationResponseFail = ValidationResponse.builder()
        .errors(Collections.singletonList("An error has occurred"))
        .warnings(Collections.singletonList("Warning!"))
        .build();

    @Autowired
    private CourtLookupService courtLookupService;

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

        stubMaintenanceServerEndpointForDraftSubmit(Collections.singletonMap(ID, TEST_CASE_ID));

        final MvcResult result = webClient.perform(post(DRAFT_API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(testDivorceSessionDataDraft))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
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
        webClient.perform(post(DRAFT_API_URL)
            .content(convertObjectToJsonString(testDivorceSessionData))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnBadRequestIfNoPayload() throws Exception {
        webClient.perform(post(DRAFT_API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    private void stubMaintenanceServerEndpointForDraftSubmit(Map<String, Object> response) {
        String testCcdTranslatedData = convertObjectToJsonString(expectedTranslatedCcdSessionDataDraft);
        maintenanceServiceServer.stubFor(WireMock.post(SUBMISSION_CONTEXT_PATH)
            .withRequestBody(equalToJson(testCcdTranslatedData))
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(convertObjectToJsonString(response))));
    }
}
