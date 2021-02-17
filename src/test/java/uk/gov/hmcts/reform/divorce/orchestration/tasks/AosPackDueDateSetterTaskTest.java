package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

public class AosPackDueDateSetterTaskTest extends DueDateSetterTaskTest {

    @Autowired
    private AosPackDueDateSetterTask aosPackDueDateSetterTask;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    private static final Integer DUE_DATE_OFFSET = 30;

    @Override
    protected Integer getDaysBeforeOverdue() {
        return DUE_DATE_OFFSET;
    }

    @Override
    protected DueDateSetterTask getDueDateSetterTaskInstance() {
        return aosPackDueDateSetterTask;
    }
}
