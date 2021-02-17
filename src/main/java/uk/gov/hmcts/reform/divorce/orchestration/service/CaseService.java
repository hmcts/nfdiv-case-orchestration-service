package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

public interface CaseService {

    Map<String, Object> submitDraftCase(Map<String, Object> divorceSession, String authToken) throws TaskException;

    Map<String, Object> patchCase(Map<String, Object> divorceEventSession, String authToken) throws WorkflowException;

    CaseDataResponse getCase(String authorizationToken);
}
