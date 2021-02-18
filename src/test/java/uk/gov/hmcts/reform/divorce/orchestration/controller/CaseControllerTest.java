package uk.gov.hmcts.reform.divorce.orchestration.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.GetCaseResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseCreationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.CaseAlreadyExistsException;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
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
    public void whenSubmitDraft_givenDuplicateCase_thenReturnCaseResponse() throws CaseAlreadyExistsException {
        final Map<String, Object> caseData = Collections.emptyMap();
        final Map<String, Object> serviceReturnData = new HashMap<>();
        serviceReturnData.put(ID, TEST_CASE_ID);
        when(caseService.submitDraftCase(caseData, AUTH_TOKEN)).thenThrow(new CaseAlreadyExistsException("Existing case found"));

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
        final GetCaseResponse getCaseResponse = GetCaseResponse.builder().build();

        when(caseService.getCase(AUTH_TOKEN)).thenReturn(getCaseResponse);

        ResponseEntity<GetCaseResponse> response = classUnderTest.retrieveCase(AUTH_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(getCaseResponse, response.getBody());

        verify(caseService).getCase(AUTH_TOKEN);
    }

    @Test
    public void givenThrowsCaseNotFoundException_whenGetCase_thenReturnExpectedResponse() throws CaseNotFoundException {
        when(caseService.getCase(AUTH_TOKEN)).thenThrow(new CaseNotFoundException("No case found for user id someUserId"));

        CaseNotFoundException caseNotFoundException = assertThrows(CaseNotFoundException.class, () -> classUnderTest.retrieveCase(AUTH_TOKEN));

        assertThat(caseNotFoundException.getMessage(), equalTo("No case found for user id someUserId"));

        verify(caseService).getCase(AUTH_TOKEN);
    }

    @Test
    public void givenThrowsDuplicateCaseException_whenGetCase_thenReturnExpectedResponse() throws CaseNotFoundException {
        when(caseService.getCase(AUTH_TOKEN)).thenThrow(new DuplicateCaseException("There are 2 cases for the user someUserId"));

        DuplicateCaseException duplicateCaseException = assertThrows(DuplicateCaseException.class, () -> classUnderTest.retrieveCase(AUTH_TOKEN));

        assertThat(duplicateCaseException.getMessage(), equalTo("There are 2 cases for the user someUserId"));

        verify(caseService).getCase(AUTH_TOKEN);
    }
}