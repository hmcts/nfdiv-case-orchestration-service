package uk.gov.hmcts.reform.divorce.orchestration.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.GetCaseResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseCreationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.exception.CaseAlreadyExistsException;
import uk.gov.hmcts.reform.divorce.orchestration.exception.DuplicateCaseException;
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
import static org.junit.Assert.fail;
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
    private CaseController caseController;

    @Test
    public void whenSubmitCase_thenReturnCaseResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final Map<String, Object> serviceReturnData = new HashMap<>();
        serviceReturnData.put(ID, TEST_CASE_ID);
        when(caseService.postCase(caseData, AUTH_TOKEN)).thenReturn(serviceReturnData);

        final ResponseEntity<CaseCreationResponse> response = caseController.submitCase(AUTH_TOKEN, caseData);

        final CaseCreationResponse responseBody = response.getBody();
        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(responseBody, notNullValue());
        assertThat(responseBody.getCaseId(), equalTo(TEST_CASE_ID));
        assertThat(responseBody.getStatus(), equalTo(SUCCESS_STATUS));
    }

    @Test
    public void whenSubmitCase_givenDuplicateCase_thenReturnCaseAlreadyExistsException() throws CaseAlreadyExistsException {
        final Map<String, Object> caseData = Collections.emptyMap();
        final Map<String, Object> serviceReturnData = new HashMap<>();
        serviceReturnData.put(ID, TEST_CASE_ID);
        when(caseService.postCase(caseData, AUTH_TOKEN)).thenThrow(new CaseAlreadyExistsException("Existing case found"));

        CaseAlreadyExistsException caseAlreadyExistsException = assertThrows(CaseAlreadyExistsException.class,
            () -> caseController.submitCase(AUTH_TOKEN, caseData));

        assertThat(caseAlreadyExistsException.getMessage(), equalTo("Existing case found"));
        verify(caseService).postCase(caseData, AUTH_TOKEN);
    }

    @Test
    public void shouldPatchCase() {

        final Long id = 123456L;
        final Map<String, Object> data = new HashMap<>();
        final Map<String, Object> payload = new HashMap<>();
        payload.put("id", id);
        payload.put("data", data);

        caseController.updateCase(AUTH_TOKEN, payload);

        verify(caseService).patchCase(id.toString(), data, AUTH_TOKEN);
    }

    @Test
    public void shouldThrowBadRequestExceptionIfPayloadHasNoIdField() throws Exception {

        final Map<String, Object> data = new HashMap<>();
        final Map<String, Object> payload = new HashMap<>();
        payload.put("data", data);

        try {
            caseController.updateCase(AUTH_TOKEN, payload);
            fail();
        } catch (final ResponseStatusException e) {
            assertThat(e.getStatus(), is(BAD_REQUEST));
            assertThat(e.getMessage(), is("400 BAD_REQUEST \"Missing field 'id' in json payload.\""));
        }
    }

    @Test
    public void shouldThrowBadRequestExceptionIfPayloadHasNoDataField() throws Exception {

        final Map<String, Object> data = new HashMap<>();
        final Map<String, Object> payload = new HashMap<>();
        payload.put("id", 123456L);

        try {
            caseController.updateCase(AUTH_TOKEN, payload);
            fail();
        } catch (final ResponseStatusException e) {
            assertThat(e.getStatus(), is(BAD_REQUEST));
            assertThat(e.getMessage(), is("400 BAD_REQUEST \"Missing field 'data' in json payload.\""));
        }
    }

    @Test
    public void whenGetCaseFromCcd_thenReturnExpectedResponse() throws CaseNotFoundException {
        final GetCaseResponse getCaseResponse = GetCaseResponse.builder().build();

        when(caseService.getCase(AUTH_TOKEN)).thenReturn(getCaseResponse);

        ResponseEntity<GetCaseResponse> response = caseController.retrieveCase(AUTH_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(getCaseResponse, response.getBody());

        verify(caseService).getCase(AUTH_TOKEN);
    }

    @Test
    public void givenThrowsCaseNotFoundException_whenGetCase_thenReturnExpectedResponse() throws CaseNotFoundException {
        when(caseService.getCase(AUTH_TOKEN)).thenThrow(new CaseNotFoundException("No case found for user id someUserId"));

        CaseNotFoundException caseNotFoundException = assertThrows(CaseNotFoundException.class, () -> caseController.retrieveCase(AUTH_TOKEN));

        assertThat(caseNotFoundException.getMessage(), equalTo("No case found for user id someUserId"));

        verify(caseService).getCase(AUTH_TOKEN);
    }

    @Test
    public void givenThrowsDuplicateCaseException_whenGetCase_thenReturnExpectedResponse() throws CaseNotFoundException {
        when(caseService.getCase(AUTH_TOKEN)).thenThrow(new DuplicateCaseException("There are 2 cases for the user someUserId"));

        DuplicateCaseException duplicateCaseException = assertThrows(DuplicateCaseException.class, () -> caseController.retrieveCase(AUTH_TOKEN));

        assertThat(duplicateCaseException.getMessage(), equalTo("There are 2 cases for the user someUserId"));

        verify(caseService).getCase(AUTH_TOKEN);
    }
}