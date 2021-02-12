package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders.GeneralOrdersFilterTask;
import uk.gov.hmcts.reform.divorce.service.CaseFormatterService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.DraftHelper.isDraft;

@Component
@RequiredArgsConstructor
public class CaseDataDraftToDivorceFormatterTask implements Task<Map<String, Object>> {

    private final GeneralOrdersFilterTask generalOrdersFilterTask;

    private final CaseFormatterService caseFormatterService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        Map<String, Object> caseDataToReturn = new HashMap<>(caseData);

        if (!isDraft(caseDataToReturn)) {
            //This is not a draft - this means the case data is in CCD format
            caseDataToReturn = generalOrdersFilterTask.execute(context, caseDataToReturn);

            caseDataToReturn = caseFormatterService.transformToDivorceSession(
                caseDataToReturn
            );
            caseDataToReturn.remove("expires");
        }

        return caseDataToReturn;
    }
}