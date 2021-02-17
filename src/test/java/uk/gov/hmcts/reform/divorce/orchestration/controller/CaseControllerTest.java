package uk.gov.hmcts.reform.divorce.orchestration.controller;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseCreationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
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
    public void whenSubmitDraft_givenDuplicateCase_thenReturnCaseResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final Map<String, Object> serviceReturnData = new HashMap<>();
        serviceReturnData.put(ID, TEST_CASE_ID);
        when(caseService.submitDraftCase(caseData, AUTH_TOKEN)).thenThrow(new TaskException("Existing case found"));

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
    public void whenGetCaseFromCcd_thenReturnExpectedResponse() throws CaseNotFoundException {
        final CaseDataResponse caseDataResponse = CaseDataResponse.builder().build();

        when(caseService.getCase(AUTH_TOKEN)).thenReturn(caseDataResponse);

        ResponseEntity<CaseDataResponse> response = classUnderTest.retrieveCase(AUTH_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(caseDataResponse, response.getBody());

        verify(caseService).getCase(AUTH_TOKEN);
    }

    @Test
    public void givenThrowsFeignNotFoundException_whenGetCase_thenReturnExpectedResponse() {
        Request request = Request.create(Request.HttpMethod.GET, "url",
            new HashMap<>(), null, new RequestTemplate());

        when(caseService.getCase(AUTH_TOKEN)).thenThrow(new FeignException.NotFound("", request, null));

        FeignException feignException = assertThrows(FeignException.class, () -> classUnderTest.retrieveCase(AUTH_TOKEN));

        assertThat(feignException.status(), is(404));

        verify(caseService).getCase(AUTH_TOKEN);
    }
}
