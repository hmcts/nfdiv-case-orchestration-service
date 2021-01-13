package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.CourtsMatcher;
import uk.gov.hmcts.reform.divorce.service.CaseFormatterService;

import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class RetrieveCaseITest extends IdamTestSupport {

    private static final String API_URL = "/retrieve-case";
    private static final String GET_CASE_CONTEXT_PATH = "/casemaintenance/version/1/case";
    private static final String FORMAT_TO_DIVORCE_CONTEXT_PATH = "/caseformatter/version/1/to-divorce-format";

    private static final Map<String, Object> CASE_DATA = Collections.singletonMap(D_8_DIVORCE_UNIT, TEST_COURT);
    private static final CaseDetails CASE_DETAILS =
        CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(CASE_DATA)
            .build();

    @MockBean
    private CaseFormatterService caseFormatterService;

    @Autowired
    private MockMvc webClient;

    @Before
    public void setUp() {
        stubUserDetailsEndpoint(OK, AUTH_TOKEN, convertObjectToJsonString(UserDetails.builder().email(TEST_EMAIL).build()));
    }

    @Test
    public void givenNoAuthToken_whenRetrieveCase_thenReturnBadRequest() throws Exception {
        webClient.perform(get(API_URL)
            .accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenNoCaseExists_whenGetCase_thenReturnEmptyResponse() throws Exception {
        stubGetCaseFromCMS(null);

        webClient.perform(get(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .accept(APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void givenMultipleCases_whenGetCase_thenPropagateException() throws Exception {
        stubGetMultipleCaseFromCMS();

        webClient.perform(get(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .accept(APPLICATION_JSON))
            .andExpect(status().isMultipleChoices())
            .andExpect(content().string(""));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void givenAllGoesWellProceedAsExpected_RetrieveCaseInformation() throws Exception {
        stubGetCaseFromCMS(CASE_DETAILS);

        when(caseFormatterService.transformToDivorceSession(any(Map.class))).thenReturn(CASE_DATA);

        CaseDataResponse expected = CaseDataResponse.builder()
            .data(CASE_DATA)
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .court(TEST_COURT)
            .build();

        webClient.perform(get(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)))
            .andExpect(content().string(hasJsonPath("$.data.court", CourtsMatcher.isExpectedCourtsList())));
    }

    private void stubGetCaseFromCMS(CaseDetails caseDetails) {
        stubGetCaseFromCMS(OK, convertObjectToJsonString(caseDetails));
    }

    private void stubGetCaseFromCMS(HttpStatus status, String message) {
        maintenanceServiceServer.stubFor(WireMock.get(GET_CASE_CONTEXT_PATH)
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(message)));
    }

    private void stubGetMultipleCaseFromCMS() {
        stubGetCaseFromCMS(HttpStatus.MULTIPLE_CHOICES, "");
    }
}
