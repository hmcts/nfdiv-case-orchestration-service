package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.service.CaseFormatterService;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class FormatDivorceSessionToAosCaseDataTask implements Task<Map<String, Object>> {

    private final CaseFormatterService caseFormatterService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> sessionData) {
        return caseFormatterService.getAosCaseData(sessionData);
    }
}
