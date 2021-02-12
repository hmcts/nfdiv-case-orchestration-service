package uk.gov.hmcts.reform.divorce.orchestration.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.model.response.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseCreationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseResponse;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.VALIDATION_ERROR_KEY;

@RunWith(MockitoJUnitRunner.class)
public class CaseControllerTest {

    @Mock
    private CaseService caseService;

    @InjectMocks
    private CaseController classUnderTest;

    @Test
    public void whenSubmitDraft_thenReturnCaseResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final Map<String, Object> serviceReturnData = new HashMap<>();
        serviceReturnData.put(ID, TEST_CASE_ID);
        when(caseService.submitDraftCase(caseData, AUTH_TOKEN)).thenReturn(serviceReturnData);

        final ResponseEntity<CaseCreationResponse> response = classUnderTest.submitCase(AUTH_TOKEN, caseData);

        final CaseCreationResponse responseBody = response.getBody();
        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(responseBody, notNullValue());
        assertThat(responseBody.getCaseId(), equalTo(TEST_CASE_ID));
        assertThat(responseBody.getStatus(), equalTo(SUCCESS_STATUS));
    }

    @Test
    public void givenErrors_whenSubmittingDraft_thenReturnBadRequest() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final Map<String, Object> invalidResponse = Collections.singletonMap(
            VALIDATION_ERROR_KEY,
            ValidationResponse.builder().build()
        );

        when(caseService.submitDraftCase(caseData, AUTH_TOKEN)).thenReturn(invalidResponse);

        final ResponseEntity<CaseCreationResponse> response = classUnderTest.submitCase(AUTH_TOKEN, caseData);

        assertThat(response.getStatusCode(), equalTo(BAD_REQUEST));
    }

    @Test
    public void whenUpdatingCase_thenReturnCaseResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final Map<String, Object> serviceReturnData = new HashMap<>();
        serviceReturnData.put(ID, TEST_CASE_ID);
        when(caseService.patchCase(caseData, AUTH_TOKEN)).thenReturn(serviceReturnData);

        final ResponseEntity<CaseResponse> response = classUnderTest.updateCase(AUTH_TOKEN, caseData);

        final CaseResponse responseBody = response.getBody();
        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(responseBody, notNullValue());
        assertThat(responseBody.getCaseId(), equalTo(TEST_CASE_ID));
        assertThat(responseBody.getStatus(), equalTo(SUCCESS_STATUS));
    }

    @Test
    public void givenErrors_whenUpdatingCase_thenReturnBadRequest() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final Map<String, Object> invalidResponse = Collections.singletonMap(
            VALIDATION_ERROR_KEY,
            ValidationResponse.builder().build()
        );

        when(caseService.patchCase(caseData, AUTH_TOKEN)).thenReturn(invalidResponse);

        final ResponseEntity<CaseResponse> response = classUnderTest.updateCase(AUTH_TOKEN, caseData);

        assertThat(response.getStatusCode(), equalTo(BAD_REQUEST));
    }
}
