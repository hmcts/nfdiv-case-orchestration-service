package uk.gov.hmcts.reform.divorce.orchestration.client;

import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "formatter-service-client", url = "${case.formatter.service.api.baseurl}")
public interface CaseFormatterClient {

    @ApiOperation("Transform data to Divorce format")
    @PostMapping(value = "/caseformatter/version/1/to-divorce-format",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    Map<String, Object> transformToDivorceFormat(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
        @RequestBody Map<String, Object> transformToDivorceFormat
    );
}
