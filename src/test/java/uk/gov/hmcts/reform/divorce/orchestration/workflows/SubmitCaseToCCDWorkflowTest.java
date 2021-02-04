package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SubmitCaseToCCD;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

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


        when(submitCaseToCCD.execute(any(), eq(incomingPayload))).thenReturn(incomingPayload);

        Map<String, Object> actual = submitCaseToCCDWorkflow.run(incomingPayload, AUTH_TOKEN);
        assertThat(actual, hasEntry(equalTo("returnedKey"), equalTo("returnedValue")));

        verify(submitCaseToCCD).execute(any(), eq(incomingPayload));

    }

    @Test(expected = WorkflowException.class)
    public void whenSubmittingCaseToCCDWorkflow_ShouldThrowException() throws WorkflowException {
        final DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        Map<String, Object> incomingPayload = new HashMap<>();

        when(submitCaseToCCD.execute(context, incomingPayload)).thenThrow(TaskException.class);

        submitCaseToCCDWorkflow.run(incomingPayload, AUTH_TOKEN);

        verify(submitCaseToCCD).execute(context, incomingPayload);
    }

}
