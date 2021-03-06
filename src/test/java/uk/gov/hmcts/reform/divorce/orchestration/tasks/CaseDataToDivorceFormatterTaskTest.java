package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.service.CaseFormatterService;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_INCOMING_PAYLOAD;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PAYLOAD_TO_RETURN;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataToDivorceFormatterTaskTest {

    @Mock
    private CaseFormatterService caseFormatterService;

    @InjectMocks
    private CaseDataToDivorceFormatterTask classUnderTest;

    @Test
    public void whenFormatData_thenReturnExpectedData() {

        final DefaultTaskContext context = new DefaultTaskContext();

        final Map<String, Object> expectedResults = TEST_PAYLOAD_TO_RETURN;
        when(caseFormatterService.transformToDivorceSession(TEST_INCOMING_PAYLOAD)).thenReturn(expectedResults);

        Map<String, Object> returnedCaseData = classUnderTest.execute(context, TEST_INCOMING_PAYLOAD);

        assertThat(returnedCaseData, is(expectedResults));
        verify(caseFormatterService).transformToDivorceSession(TEST_INCOMING_PAYLOAD);
    }
}
