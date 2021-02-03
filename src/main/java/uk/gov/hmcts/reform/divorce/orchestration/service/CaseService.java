package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;

import java.util.Map;

public interface CaseService {

    Map<String, Object> submitCase(Map<String, Object> divorceSession, String authToken) throws WorkflowException;

}
