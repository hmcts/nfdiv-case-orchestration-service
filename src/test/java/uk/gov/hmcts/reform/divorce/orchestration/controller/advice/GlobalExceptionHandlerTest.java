package uk.gov.hmcts.reform.divorce.orchestration.controller.advice;

import feign.FeignException;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.AuthenticationError;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.ValidationException;
import uk.gov.hmcts.reform.divorce.orchestration.exception.BaseException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.MULTIPLE_CHOICES;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ERROR;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ERROR_CONTENT;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    public void whenHandleFeignException_thenReturnUnderLyingError() {
        final FeignException feignException = getFeignException();

        ResponseEntity<Object> actual = globalExceptionHandler.handleFeignException(feignException);

        assertEquals(BAD_REQUEST, actual.getStatusCode());
        assertEquals(TEST_ERROR + " - " + TEST_ERROR_CONTENT, actual.getBody());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldHandleBaseException() {

        final BaseException baseException = mock(BaseException.class);
        final ResponseEntity<Object> responseEntity = mock(ResponseEntity.class);

        when(baseException.getMessage()).thenReturn("Exception message");
        when(baseException.getResponse()).thenReturn(responseEntity);

        final ResponseEntity<Object> actualResponse = globalExceptionHandler.handleBaseException(baseException);

        assertThat(actualResponse, is(responseEntity));
    }

    @Test
    public void givenNoCause_whenHandleWorkFlowException_thenReturnInternalServerError() {
        final WorkflowException workflowException = new WorkflowException(TEST_ERROR);

        ResponseEntity<Object> actual = globalExceptionHandler.handleWorkFlowException(workflowException);

        assertEquals(INTERNAL_SERVER_ERROR, actual.getStatusCode());
        assertEquals(TEST_ERROR, actual.getBody());
    }

    @Test
    public void givenCauseInstanceOfFeignException_whenHandleWorkFlowException_thenReturnFeignStatus() {
        final FeignException feignException = getFeignException();

        final WorkflowException workflowException = new WorkflowException(TEST_ERROR, feignException);

        ResponseEntity<Object> actual = globalExceptionHandler.handleWorkFlowException(workflowException);

        assertEquals(BAD_REQUEST, actual.getStatusCode());
        assertEquals(TEST_ERROR + " - " + TEST_ERROR_CONTENT, actual.getBody());
    }

    @Test
    public void givenCauseForMultiCases_whenHandleWorkFlowException_thenReturnFeignStatus() {
        final FeignException feignException = getMultiFeignException();

        final WorkflowException workflowException = new WorkflowException("", feignException);

        ResponseEntity<Object> actual = globalExceptionHandler.handleWorkFlowException(workflowException);

        assertEquals(MULTIPLE_CHOICES, actual.getStatusCode());
        assertNull(actual.getBody());
    }

    @Test
    public void givenCauseInstanceOfTaskException_whenHandleWorkFlowException_thenReturnInternalServerError() {
        final TaskException taskException = new TaskException(TEST_ERROR);

        final WorkflowException workflowException = new WorkflowException(TEST_ERROR, taskException);

        ResponseEntity<Object> actual = globalExceptionHandler.handleWorkFlowException(workflowException);

        assertEquals(INTERNAL_SERVER_ERROR, actual.getStatusCode());
        assertEquals(TEST_ERROR, actual.getBody());
    }

    @Test
    public void givenCauseTaskExceptionContainsFeignException_whenHandleWorkFlowException_thenReturnFeignStatus() {
        final FeignException feignException = getFeignException();

        final TaskException taskException = new TaskException(TEST_ERROR, feignException);

        final WorkflowException workflowException = new WorkflowException(TEST_ERROR, taskException);

        ResponseEntity<Object> actual = globalExceptionHandler.handleWorkFlowException(workflowException);

        assertEquals(BAD_REQUEST, actual.getStatusCode());
        assertEquals(TEST_ERROR + " - " + TEST_ERROR_CONTENT, actual.getBody());
    }

    @Test
    public void givenCauseTaskExceptionContainsNoFeignNoAuthException_whenHandleWorkFlowException_thenReturnFeignStatus() {
        final Exception exception = new Exception(TEST_ERROR);

        final TaskException taskException = new TaskException(TEST_ERROR, exception);

        final WorkflowException workflowException = new WorkflowException(TEST_ERROR, taskException);

        ResponseEntity<Object> actual = globalExceptionHandler.handleWorkFlowException(workflowException);

        assertEquals(INTERNAL_SERVER_ERROR, actual.getStatusCode());
        assertEquals(TEST_ERROR, actual.getBody());
    }

    @Test
    public void givenCauseTaskExceptionContainsAuthorizationError_whenHandleWorkFlowException_thenReturnUnAuthorized() {
        final AuthenticationError authenticationError = new AuthenticationError(TEST_ERROR);

        final TaskException taskException = new TaskException(TEST_ERROR, authenticationError);

        final WorkflowException workflowException = new WorkflowException(TEST_ERROR, taskException);

        ResponseEntity<Object> actual = globalExceptionHandler.handleWorkFlowException(workflowException);

        assertEquals(UNAUTHORIZED, actual.getStatusCode());
        assertEquals(TEST_ERROR, actual.getBody());
    }

    @Test
    public void givenCauseTaskExceptionContainsCaseNotFoundException_whenHandleWorkFlowException_thenReturnNotFound() {
        final CaseNotFoundException caseNotFoundException = new CaseNotFoundException(TEST_ERROR);

        final TaskException taskException = new TaskException(TEST_ERROR, caseNotFoundException);

        final WorkflowException workflowException = new WorkflowException(TEST_ERROR, taskException);

        ResponseEntity<Object> actual = globalExceptionHandler.handleWorkFlowException(workflowException);

        assertEquals(NOT_FOUND, actual.getStatusCode());
        assertEquals(TEST_ERROR, actual.getBody());
    }

    @Test
    public void givenTaskExceptionCausedByValidationException_whenHandleWorkFlowException_thenReturnBadRequest() {
        final ValidationException validationException = new ValidationException(TEST_ERROR);

        final TaskException taskException = new TaskException(TEST_ERROR, validationException);

        final WorkflowException workflowException = new WorkflowException(TEST_ERROR, taskException);

        ResponseEntity<Object> actual = globalExceptionHandler.handleWorkFlowException(workflowException);

        assertThat(actual.getStatusCode(), is(BAD_REQUEST));
        assertThat(actual.getBody(), is(TEST_ERROR));
    }


    private FeignException getFeignException() {
        final FeignException feignException = mock(FeignException.class);

        when(feignException.status()).thenReturn(BAD_REQUEST.value());
        when(feignException.getMessage()).thenReturn(TEST_ERROR);
        when(feignException.contentUTF8()).thenReturn(TEST_ERROR_CONTENT);
        return feignException;
    }

    private FeignException getMultiFeignException() {
        final FeignException feignException = mock(FeignException.class);

        when(feignException.status()).thenReturn(MULTIPLE_CHOICES.value());
        when(feignException.getMessage()).thenReturn("");
        return feignException;
    }
}
