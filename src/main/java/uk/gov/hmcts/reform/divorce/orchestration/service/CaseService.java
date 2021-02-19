package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.GetCaseResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.CaseNotFoundException;

import java.util.Map;

public interface CaseService {

    Map<String, Object> submitDraftCase(Map<String, Object> divorceSession, String authToken);

    void patchCase(final String caseId, Map<String, Object> divorceEventSession, String authToken);

    GetCaseResponse getCase(String authorizationToken) throws CaseNotFoundException;
}
