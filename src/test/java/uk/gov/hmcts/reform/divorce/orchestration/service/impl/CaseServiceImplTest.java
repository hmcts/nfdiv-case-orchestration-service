package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitCaseToCCDWorkflow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.*;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_PIN;

@RunWith(MockitoJUnitRunner.class)
public class CaseServiceImplTest {

    @Mock
    private SubmitCaseToCCDWorkflow submitCaseToCCDWorkflow;

    @InjectMocks
    private CaseServiceImpl classUnderTest;

    private CcdCallbackRequest ccdCallbackRequest;

    private Map<String, Object> requestPayload;

    private Map<String, Object> expectedPayload;

    @Before
    public void setUp() {
        requestPayload = singletonMap("requestPayloadKey", "requestPayloadValue");
        ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(
                CaseDetails.builder()
                    .caseData(requestPayload)
                    .caseId(TEST_CASE_ID)
                    .state(TEST_STATE)
                    .build())
            .eventId(TEST_EVENT_ID)
            .token(TEST_TOKEN)
            .build();
        expectedPayload = Collections.singletonMap(RESPONDENT_PIN, TEST_PIN);
    }

    @Test
    public void givenDraftCaseDataValid_whenSubmit_thenReturnPayload() throws Exception {
        Map<String, Object> expectedPayload = new HashMap<>();
        expectedPayload.put("returnedKey", "returnedValue");
        when(submitCaseToCCDWorkflow.run(requestPayload, AUTH_TOKEN)).thenReturn(expectedPayload);
        when(submitCaseToCCDWorkflow.errors()).thenReturn(Collections.emptyMap());

        Map<String, Object> actual = classUnderTest.submitCase(requestPayload, AUTH_TOKEN);

        assertThat(actual.get("returnedKey"), is("returnedValue"));

        verify(submitCaseToCCDWorkflow).run(requestPayload, AUTH_TOKEN);
        verify(submitCaseToCCDWorkflow).errors();
    }

    @Test
    public void givenDraftCaseDataInvalid_whenSubmit_thenReturnListOfErrors() throws Exception {
        when(submitCaseToCCDWorkflow.run(requestPayload, AUTH_TOKEN)).thenReturn(expectedPayload);
        Map<String, Object> errors = singletonMap("new_Error", "An Error");
        when(submitCaseToCCDWorkflow.errors()).thenReturn(errors);

        Map<String, Object> actual = classUnderTest.submitCase(requestPayload, AUTH_TOKEN);

        assertEquals(errors, actual);

        verify(submitCaseToCCDWorkflow).run(requestPayload, AUTH_TOKEN);
        verify(submitCaseToCCDWorkflow, times(2)).errors();
    }
}
