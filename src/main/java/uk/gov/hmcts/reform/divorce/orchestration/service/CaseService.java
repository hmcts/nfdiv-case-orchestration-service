package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;

import java.util.Map;

public interface CaseService {

    Map<String, Object> submitDraftCase(Map<String, Object> divorceSession, String authToken) throws WorkflowException;

    Map<String, Object> patchCase(Map<String, Object> divorceEventSession, String authToken) throws WorkflowException;

    CaseDataResponse getCase(String authorizationToken) throws CaseNotFoundException;
}
