package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.draftcase.UpdateDraftCaseInCCD;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_DATA_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDraftCaseInCCDWorkflowTest {

    @Mock
    private UpdateDraftCaseInCCD updateDraftCaseInCCD;

    @InjectMocks
    private UpdateDraftCaseInCCDWorkflow updateDraftCaseInCCDWorkflow;

    private Map<String, Object> eventData;
    private Map<String, Object> testData;
    private TaskContext context;

    @Before
    public void setup() {
        eventData = new HashMap<>();
        testData = Collections.emptyMap();
        eventData.put(CASE_EVENT_DATA_JSON_KEY, testData);

        context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
    }

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {
        Map<String, Object> resultData = Collections.singletonMap("Hello", "World");

        when(updateDraftCaseInCCD.execute(context, testData)).thenReturn(resultData);

        assertEquals(resultData, updateDraftCaseInCCDWorkflow.run(eventData, AUTH_TOKEN));

        verify(updateDraftCaseInCCD).execute(context, testData);
    }
}
