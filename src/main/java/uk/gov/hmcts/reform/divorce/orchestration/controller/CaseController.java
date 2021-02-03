package uk.gov.hmcts.reform.divorce.orchestration.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseCreationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseService;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.*;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_STATUS;

@Slf4j
@RestController
public class CaseController {

    @Autowired
    private CaseService caseService;

    @PostMapping(path = "/case", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Create case in CCD - called by Petitioner Frontend")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Submit was successful and a case was created in CCD",
            response = CaseCreationResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CaseCreationResponse> submitCase(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorizationToken,
        @RequestBody @ApiParam("Divorce Session") Map<String, Object> payload) throws WorkflowException {

        ResponseEntity<CaseCreationResponse> endpointResponse;

        Map<String, Object> serviceResponse = caseService.submitCase(payload, authorizationToken);

        if (serviceResponse.containsKey(VALIDATION_ERROR_KEY)) {
            log.error("Bad request. Found this validation error: {}", serviceResponse.get(VALIDATION_ERROR_KEY));
            return endpointResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        }

        CaseCreationResponse caseCreationResponse = new CaseCreationResponse();
        caseCreationResponse.setCaseId(String.valueOf(serviceResponse.get(ID)));
        caseCreationResponse.setStatus(SUCCESS_STATUS);
        return ResponseEntity.ok(caseCreationResponse);

    }
}
