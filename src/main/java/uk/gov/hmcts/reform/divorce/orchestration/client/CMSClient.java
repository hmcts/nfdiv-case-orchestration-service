package uk.gov.hmcts.reform.divorce.orchestration.client;

import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

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

    @ApiOperation("Update Draft Case")
    @PostMapping(value = "/case/{caseId}",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    Map<String, Object> updateDraftCase(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
        @PathVariable("caseId") String caseId,
        @RequestBody Map<String, Object> requestBody
    );

}
