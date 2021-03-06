package uk.gov.hmcts.reform.divorce.orchestration.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.GetCaseResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseCreationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.exception.CaseAlreadyExistsException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseService;

import java.util.Map;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_STATUS;

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
        @RequestBody @ApiParam("Divorce Session") Map<String, Object> payload) throws CaseAlreadyExistsException {
        Map<String, Object> serviceResponse = caseService.postCase(payload, authorizationToken);

        CaseCreationResponse caseCreationResponse = new CaseCreationResponse();
        caseCreationResponse.setCaseId(String.valueOf(serviceResponse.get(ID)));
        caseCreationResponse.setStatus(SUCCESS_STATUS);
        return ResponseEntity.ok(caseCreationResponse);
    }

    @SuppressWarnings("unchecked")
    @PatchMapping(path = "/case", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Patch case in CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Update was successful and case was updated in CCD"),
        @ApiResponse(code = 400, message = "Json payload has missing field")})
    public void updateCase(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorizationToken,
        @RequestBody @ApiParam("Divorce Session") Map<String, Object> payload) {

        final Object id = Optional.ofNullable(payload.get("id"))
            .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Missing field 'id' in json payload."));
        final Map<String, Object> data = Optional.ofNullable((Map<String, Object>) payload.get("data"))
            .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Missing field 'data' in json payload."));

        caseService.patchCase(id.toString(), data, authorizationToken);
    }

    @GetMapping(path = "/case", produces = APPLICATION_JSON)
    @ApiOperation(value = "Provides case details to Frontend Applications")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Case details fetched successfully",
            response = GetCaseResponse.class),
        @ApiResponse(code = 300, message = "Multiple Cases found"),
        @ApiResponse(code = 404, message = "No Case found"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<GetCaseResponse> retrieveCase(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorizationToken
    ) throws CaseNotFoundException {
        return ResponseEntity.ok(caseService.getCase(authorizationToken));
    }
}
