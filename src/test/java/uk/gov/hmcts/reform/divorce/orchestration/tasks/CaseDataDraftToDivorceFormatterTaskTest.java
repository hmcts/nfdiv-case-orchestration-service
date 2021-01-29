package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders.GeneralOrdersFilterTask;
import uk.gov.hmcts.reform.divorce.service.CaseFormatterService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PAYLOAD_TO_RETURN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.IS_DRAFT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithToken;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataDraftToDivorceFormatterTaskTest {

    @Mock
    private CaseFormatterService caseFormatterService;

    @Mock
    private GeneralOrdersFilterTask generalOrdersFilterTask;

    @InjectMocks
    private CaseDataDraftToDivorceFormatterTask classUnderTest;

    private Map<String, Object> caseData;
    private TaskContext taskContext;

    @Before
    public void setUp() {
        taskContext = contextWithToken();
        caseData = new HashMap<>();
    }

    @Test
    public void shouldTransformIfCaseDataIsNotDraft() {
        caseData.put(IS_DRAFT_KEY, false);
        when(generalOrdersFilterTask.execute(taskContext, caseData)).thenReturn(caseData);
        when(caseFormatterService.transformToDivorceSession(caseData)).thenReturn(TEST_PAYLOAD_TO_RETURN);

        Map<String, Object> returnedCaseData = classUnderTest.execute(taskContext, caseData);

        verify(caseFormatterService).transformToDivorceSession(caseData);
        verify(generalOrdersFilterTask).execute(taskContext, caseData);
        assertThat(returnedCaseData, is(TEST_PAYLOAD_TO_RETURN));
    }

    @Test
    public void shouldTransformIfDraftInfoIsNotSpecified() {
        when(generalOrdersFilterTask.execute(taskContext, caseData)).thenReturn(caseData);
        when(caseFormatterService.transformToDivorceSession(caseData)).thenReturn(TEST_PAYLOAD_TO_RETURN);

        Map<String, Object> returnedCaseData = classUnderTest.execute(taskContext, caseData);

        verify(caseFormatterService).transformToDivorceSession(caseData);
        verify(generalOrdersFilterTask).execute(taskContext, caseData);
        assertThat(returnedCaseData, is(TEST_PAYLOAD_TO_RETURN));
    }

    @Test
    public void shouldNotTransformDraft() {
        caseData.put(IS_DRAFT_KEY, true);

        Map<String, Object> returnedCaseData = classUnderTest.execute(taskContext, caseData);

        verifyNoInteractions(generalOrdersFilterTask, caseFormatterService);
        assertThat(returnedCaseData, is(caseData));
    }

}