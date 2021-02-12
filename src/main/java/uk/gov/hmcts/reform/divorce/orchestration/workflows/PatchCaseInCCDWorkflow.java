package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ccdcase.PatchCaseInCCD;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_DATA_JSON_KEY;

@Component
public class PatchCaseInCCDWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private PatchCaseInCCD patchCaseInCCD;

    @SuppressWarnings("unchecked")
    public Map<String, Object> run(Map<String, Object> divorceEvent,
                                   String authToken) throws WorkflowException {

        return this.execute(
            new Task[] {
                patchCaseInCCD
            },
            (Map<String, Object>) divorceEvent.get(CASE_EVENT_DATA_JSON_KEY),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken)
        );
    }
}