package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.service.CaseFormatterService;

import java.util.Map;

@Component
public class RemoveMiniPetitionDraftDocumentsTask implements Task<Map<String, Object>> {

    private final CaseFormatterService caseFormatterService;

    @Autowired
    public RemoveMiniPetitionDraftDocumentsTask(final CaseFormatterService caseFormatterService) {
        this.caseFormatterService = caseFormatterService;
    }

    @Override
    public Map<String, Object> execute(final TaskContext context, final Map<String, Object> caseData) {
        return caseFormatterService.removeAllPetitionDocuments(caseData);
    }
}
