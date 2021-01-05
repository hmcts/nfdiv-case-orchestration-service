package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.caseformatter.CaseFormatterService;

import java.util.Map;

@Component
public class FormatDivorceSessionToAosCaseData implements Task<Map<String, Object>> {

    private final CaseFormatterService caseFormatterService;

    @Autowired
    public FormatDivorceSessionToAosCaseData(CaseFormatterService caseFormatterService) {
        this.caseFormatterService = caseFormatterService;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> sessionData) {
        return caseFormatterService.getAosCaseData(sessionData);
    }
}
