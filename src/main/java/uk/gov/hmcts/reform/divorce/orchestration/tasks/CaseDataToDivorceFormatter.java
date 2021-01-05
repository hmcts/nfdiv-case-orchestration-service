package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.caseformatter.CaseFormatterService;

import java.util.Map;

@Component
public class CaseDataToDivorceFormatter implements Task<Map<String, Object>> {

    private final CaseFormatterService caseFormatterService;

    @Autowired
    public CaseDataToDivorceFormatter(CaseFormatterService caseFormatterService) {
        this.caseFormatterService = caseFormatterService;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        return caseFormatterService.transformToDivorceSession(caseData);
    }

}