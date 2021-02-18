package uk.gov.hmcts.reform.divorce.orchestration.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class BadRequestException extends BaseException {

    public BadRequestException(final String message) {
        super(message, BAD_REQUEST);
    }
}
