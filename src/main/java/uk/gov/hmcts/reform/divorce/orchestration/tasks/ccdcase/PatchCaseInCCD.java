package uk.gov.hmcts.reform.divorce.orchestration.tasks.ccdcase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CMSClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Component
public class PatchCaseInCCD implements Task<Map<String, Object>> {

    @Autowired
    private CMSClient caseMaintenanceServiceClient;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        return caseMaintenanceServiceClient.patchCase(
            context.getTransientObject(AUTH_TOKEN_JSON_KEY),
            caseData
        );
    }
}