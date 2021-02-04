package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.draftcase.SubmitDraftCaseToCcd;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Slf4j
@Component
public class SubmitDraftCaseToCCDWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private SubmitDraftCaseToCcd submitDraftCaseToCcd;

    public Map<String, Object> run(Map<String, Object> payload, String authToken) throws WorkflowException {
        Map<String, Object> returnFromExecution = this.execute(
            new Task[]{
                submitDraftCaseToCcd
            },
            payload,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken)
        );

        return new HashMap<>(returnFromExecution);

    }

}