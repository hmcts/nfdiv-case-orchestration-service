package uk.gov.hmcts.reform.divorce.orchestration.client;

import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

import java.util.Map;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "maintenance-service-client", url = "${case.maintenance.service.api.baseurl}")
public interface CMSClient {

    @ApiOperation("Submit Draft Case")
    @PostMapping(value = "/case",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    Map<String, Object> submitDraftCase(
        @RequestBody Map<String, Object> draftCase,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken
    );

    @ApiOperation("Patch Case")
    @PatchMapping(value = "/case",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    Map<String, Object> patchCase(
        @RequestBody Map<String, Object> requestBody,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken
    );

    @ApiOperation("Get Case from Ccd without any role or state filtering")
    @GetMapping(value = "/case")
    CaseDetails getCaseFromCcd(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken);
}