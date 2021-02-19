package uk.gov.hmcts.reform.divorce.orchestration.exception;

import org.springframework.http.HttpStatus;

public class CaseAlreadyExistsException extends BaseException {
    public CaseAlreadyExistsException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
