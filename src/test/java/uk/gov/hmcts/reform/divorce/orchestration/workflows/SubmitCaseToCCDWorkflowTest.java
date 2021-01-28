package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SubmitCaseToCCD;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class SubmitCaseToCCDWorkflowTest {

    @Mock
    private FormatDivorceSessionToCaseDataTask formatDivorceSessionToCaseDataTask;

    @Mock
    private SubmitCaseToCCD submitCaseToCCD;

    @InjectMocks
    private SubmitCaseToCCDWorkflow submitCaseToCCDWorkflow;

    @Test
    public void whenSubmittingCaseToCCDWorkflow_ShouldExecuteTasks_AndReturnPayload() throws Exception {

        Map<String, Object> incomingPayload = singletonMap("returnedKey", "returnedValue");


        when(formatDivorceSessionToCaseDataTask.execute(any(), eq(incomingPayload))).thenReturn(incomingPayload);
        when(submitCaseToCCD.execute(any(), eq(incomingPayload))).thenReturn(incomingPayload);

        Map<String, Object> actual = submitCaseToCCDWorkflow.run(incomingPayload, AUTH_TOKEN);
        assertThat(actual, hasEntry(equalTo("returnedKey"), equalTo("returnedValue")));

        verify(formatDivorceSessionToCaseDataTask).execute(any(), eq(incomingPayload));
        verify(submitCaseToCCD).execute(any(), eq(incomingPayload));

    }

}
