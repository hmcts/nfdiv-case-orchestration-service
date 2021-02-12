package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.PatchCaseInCCDWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitDraftCaseToCCDWorkflow;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseServiceImpl implements CaseService {

    private final SubmitDraftCaseToCCDWorkflow submitDraftCaseToCCDWorkflow;
    private final PatchCaseInCCDWorkflow patchCaseInCCDWorkflow;

    private final CaseMaintenanceClient caseMaintenanceClient;

    @Override
    public Map<String, Object> submitDraftCase(
        Map<String, Object> divorceSession,
        String authToken
    ) throws WorkflowException {
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
    public Map<String, Object> patchCase(
        Map<String, Object> divorceSession,
        String authToken
    ) throws WorkflowException {
        Map<String, Object> payload = patchCaseInCCDWorkflow.run(divorceSession, authToken);

        if (patchCaseInCCDWorkflow.errors().isEmpty()) {
            log.info("Updated case with CASE ID: {}", payload.get(ID));
            return payload;
        } else {
            log.info("Case with CASE ID: {} update failed", payload.get(ID));
            return patchCaseInCCDWorkflow.errors();
        }

    }

    @Override
    public CaseDataResponse getCase(String authorizationToken) {
        CaseDetails caseDetails = caseMaintenanceClient.getCaseFromCcd(authorizationToken);

        log.info("Successfully retrieved case with id {} and state {}", caseDetails.getCaseId(), caseDetails.getState());

        Map<String, Object> caseData = caseDetails.getCaseData();

        return CaseDataResponse.builder()
            .caseId(caseDetails.getCaseId())
            .state(caseDetails.getState())
            .court(String.valueOf(caseData.get(D_8_DIVORCE_UNIT)))
            .data(caseData)
            .build();
    }
}
