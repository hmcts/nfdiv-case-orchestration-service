package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.service.CaseFormatterService;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FormatDivorceSessionToAosCaseDataTaskTest {

    @Mock
    private CaseFormatterService caseFormatterService;

    @InjectMocks
    private FormatDivorceSessionToAosCaseDataTask classUnderTest;

    @SuppressWarnings("unchecked")
    @Test
    public void whenExecute_thenProceedAsExpected() {
        final Map<String, Object> sessionData = mock(Map.class);
        final Map<String, Object> expectedOutput = mock(Map.class);
        final DefaultTaskContext context = new DefaultTaskContext();

        when(caseFormatterService.getAosCaseData(sessionData)).thenReturn(expectedOutput);

        assertEquals(expectedOutput, classUnderTest.execute(context, sessionData));

        verify(caseFormatterService).getAosCaseData(sessionData);
    }
}