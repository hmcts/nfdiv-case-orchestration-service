package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AuthenticateRespondentTask;

import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ID_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticateRespondentTaskWorkflowTest {

    @Mock
    private AuthenticateRespondentTask authenticateRespondentTask;

    @InjectMocks
    private AuthenticateRespondentWorkflow classUnderTest;

    private DefaultTaskContext defaultTaskContext;

    @Before
    public void setup() {
        defaultTaskContext = new DefaultTaskContext();
        defaultTaskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        defaultTaskContext.setTransientObject(ID_TOKEN_JSON_KEY, TEST_ID_TOKEN);
    }

    @Test
    public void whenRun_thenProceedAsExpected() throws Exception {
        final boolean expected = true;

        Mockito.when(authenticateRespondentTask.execute(defaultTaskContext, null)).thenReturn(expected);

        classUnderTest.run(AUTH_TOKEN, TEST_ID_TOKEN);

        Mockito.verify(authenticateRespondentTask).execute(defaultTaskContext, null);
    }
}