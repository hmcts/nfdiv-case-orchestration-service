package uk.gov.hmcts.reform.divorce.orchestration.controller.ccdcase;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.model.ccd.CoreCaseData;
import uk.gov.hmcts.reform.divorce.model.usersession.DivorceSession;
import uk.gov.hmcts.reform.divorce.orchestration.controller.MockedFunctionalTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_DATA_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.DataTransformationTestHelper.getExpectedTranslatedDraftCoreCaseData;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.DataTransformationTestHelper.getTestDraftDivorceSessionData;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObject;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class PatchCaseTest extends MockedFunctionalTest {

    private static final String PATCH_PATH = "/case";
    private static final UrlPattern PATCH_CONTEXT_PATH = urlPathEqualTo("/case");

    private DivorceSession testDivorceSessionData;
    private CoreCaseData expectedCcdSessionData;

    private Map<String, Object> eventData;

    @Autowired
    private MockMvc webClient;

    @Before
    public void setUp() throws IOException {
        testDivorceSessionData = getTestDraftDivorceSessionData();
        expectedCcdSessionData = getExpectedTranslatedDraftCoreCaseData();

        eventData = new HashMap<>();
        eventData.put(CASE_EVENT_DATA_JSON_KEY, convertObject(testDivorceSessionData, new TypeReference<>() {
        }));
    }

    @Test
    public void shouldPatchCase() throws Exception {
        Map<String, Object> responseData = Collections.singletonMap(ID, TEST_CASE_ID);

        stubMaintenanceServerEndpointForPatch(responseData);

        CaseResponse updateResponse = CaseResponse.builder()
            .caseId(TEST_CASE_ID)
            .status(SUCCESS_STATUS)
            .build();

        webClient.perform(patch(PATCH_PATH)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(eventData))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().string(containsString(convertObjectToJsonString(updateResponse))));
    }

    @Test
    public void shouldReturnBadRequestIfNoAuthenticationToken() throws Exception {
        webClient.perform(patch(PATCH_PATH)
            .content(convertObjectToJsonString(testDivorceSessionData))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    private void stubMaintenanceServerEndpointForPatch(Map<String, Object> response) {
        maintenanceServiceServer.stubFor(WireMock.patch(PATCH_CONTEXT_PATH)
            .withRequestBody(equalToJson(convertObjectToJsonString(expectedCcdSessionData)))
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(convertObjectToJsonString(response))));
    }

}
