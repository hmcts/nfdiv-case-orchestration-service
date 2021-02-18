package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@Builder
@EqualsAndHashCode
public class GetCaseResponse {
    private String id;

    private String state;

    private Map<String, Object> data;
}