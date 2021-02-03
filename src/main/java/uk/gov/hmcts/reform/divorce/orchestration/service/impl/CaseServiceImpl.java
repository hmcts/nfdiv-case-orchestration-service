package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitCaseToCCDWorkflow;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseServiceImpl implements CaseService {

    private final SubmitCaseToCCDWorkflow submitCaseToCCDWorkflow;

    @Override
    public Map<String, Object> submitCase(Map<String, Object> divorceSession, String authToken) throws WorkflowException {
        Map<String, Object> payload = submitCaseToCCDWorkflow.run(divorceSession, authToken);

        if (submitCaseToCCDWorkflow.errors().isEmpty()) {
            log.info("Case with CASE ID: {} submitted", payload.get(ID));
            return payload;
        } else {
            log.info("Case with CASE ID: {} submit failed", payload.get(ID));
            return submitCaseToCCDWorkflow.errors();
        }
    }

}
