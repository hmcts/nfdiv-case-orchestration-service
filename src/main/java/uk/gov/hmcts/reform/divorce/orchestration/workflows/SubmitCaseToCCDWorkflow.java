package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DuplicateCaseValidationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SubmitCaseToCCD;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Slf4j
@Component
public class SubmitCaseToCCDWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private FormatDivorceSessionToCaseDataTask formatDivorceSessionToCaseDataTask;

    @Autowired
    private SubmitCaseToCCD submitCaseToCCD;

    public Map<String, Object> run(Map<String, Object> payload, String authToken) throws WorkflowException {
        Map<String, Object> returnFromExecution = this.execute(
            new Task[]{
                formatDivorceSessionToCaseDataTask,
                submitCaseToCCD
            },
            payload,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken)
        );

        Map<String, Object> response = new HashMap<>(returnFromExecution);

        return response;
    }

}