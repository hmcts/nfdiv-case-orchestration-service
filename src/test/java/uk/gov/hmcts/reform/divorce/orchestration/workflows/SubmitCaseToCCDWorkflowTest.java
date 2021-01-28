package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SubmitCaseToCCD;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtConstants.ALLOCATED_COURT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtConstants.REASON_FOR_DIVORCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow.SELECTED_COURT;

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
        assertThat(actual.get("returnedKey"), is("returnedValue"));

        verify(submitCaseToCCDWorkflow).run(incomingPayload, AUTH_TOKEN);
        verify(submitCaseToCCDWorkflow).errors();

        verify(formatDivorceSessionToCaseDataTask).execute(any(), incomingPayload);
    }

}
