package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitDraftCaseToCCDWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateDraftCaseInCCDWorkflow;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseServiceImpl implements CaseService {

    private final SubmitDraftCaseToCCDWorkflow submitDraftCaseToCCDWorkflow;
    private final UpdateDraftCaseInCCDWorkflow updateDraftCaseInCCDWorkflow;

    @Override
    public Map<String, Object> submitDraftCase(Map<String, Object> divorceSession, String authToken) throws WorkflowException {
        Map<String, Object> payload = submitDraftCaseToCCDWorkflow.run(divorceSession, authToken);

        if (submitDraftCaseToCCDWorkflow.errors().isEmpty()) {
            log.info("Case with CASE ID: {} submitted", payload.get(ID));
            return payload;
        } else {
            log.info("Case with CASE ID: {} submit failed", payload.get(ID));
            return submitDraftCaseToCCDWorkflow.errors();
        }
    }

    @Override
    public Map<String, Object> updateDraftCase(Map<String, Object> divorceSession,
                                      String authToken,
                                      String caseId) throws WorkflowException {
        Map<String, Object> payload = updateDraftCaseInCCDWorkflow.run(divorceSession, authToken, caseId);

        if (updateDraftCaseInCCDWorkflow.errors().isEmpty()) {
            log.info("Updated case with CASE ID: {}", payload.get(ID));
            return payload;
        } else {
            log.info("Case with CASE ID: {} update failed", payload.get(ID));
            return updateDraftCaseInCCDWorkflow.errors();
        }

    }

}
