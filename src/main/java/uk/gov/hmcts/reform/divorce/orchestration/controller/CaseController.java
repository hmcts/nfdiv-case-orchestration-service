package uk.gov.hmcts.reform.divorce.orchestration.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseCreationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseService;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.VALIDATION_ERROR_KEY;

@Slf4j
@RestController
public class CaseController {

    @Autowired
    private CaseService caseService;

    @PostMapping(path = "/case", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Create case in CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Submit was successful and a case was created in CCD",
            response = CaseCreationResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CaseCreationResponse> submitCase(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorizationToken,
        @RequestBody @ApiParam("Divorce Session") Map<String, Object> payload) throws WorkflowException {

        Map<String, Object> serviceResponse = caseService.submitDraftCase(payload, authorizationToken);

        if (serviceResponse.containsKey(VALIDATION_ERROR_KEY)) {
            log.error("Bad request. Found this validation error: {}", serviceResponse.get(VALIDATION_ERROR_KEY));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        CaseCreationResponse caseCreationResponse = new CaseCreationResponse();
        caseCreationResponse.setCaseId(String.valueOf(serviceResponse.get(ID)));
        caseCreationResponse.setStatus(SUCCESS_STATUS);
        return ResponseEntity.ok(caseCreationResponse);

    }

    @PatchMapping(path = "/case", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Patch case in CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Update was successful and case was updated in CCD",
            response = CaseResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CaseResponse> updateCase(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorizationToken,
        @RequestBody @ApiParam("Divorce Session") Map<String, Object> payload) throws WorkflowException {

        Map<String, Object> serviceResponse = caseService.patchCase(payload, authorizationToken);

        if (serviceResponse.containsKey(VALIDATION_ERROR_KEY)) {
            log.error("Bad request. Found this validation error: {}", serviceResponse.get(VALIDATION_ERROR_KEY));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        CaseResponse caseResponse = new CaseResponse();
        caseResponse.setCaseId(String.valueOf(serviceResponse.get(ID)));
        caseResponse.setStatus(SUCCESS_STATUS);
        return ResponseEntity.ok(caseResponse);
    }

    @GetMapping(path = "/case", produces = APPLICATION_JSON)
    @ApiOperation(value = "Provides case details to Frontend Applications")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Case details fetched successfully",
            response = CaseDataResponse.class),
        @ApiResponse(code = 300, message = "Multiple Cases found"),
        @ApiResponse(code = 404, message = "No Case found"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CaseDataResponse> retrieveCase(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorizationToken
    ) throws CaseNotFoundException {
        return ResponseEntity.ok(caseService.getCase(authorizationToken));
    }
}
