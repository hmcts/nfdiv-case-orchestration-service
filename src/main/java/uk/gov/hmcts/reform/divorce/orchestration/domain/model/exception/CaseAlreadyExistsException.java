package uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception;

public class CaseAlreadyExistsException extends Exception {
    public CaseAlreadyExistsException(String message) {
        super(message);
    }
}
