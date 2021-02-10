package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitDraftCaseToCCDWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateDraftCaseInCCDWorkflow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_PIN;

@RunWith(MockitoJUnitRunner.class)
public class CaseServiceImplTest {

    @Mock
    private SubmitDraftCaseToCCDWorkflow submitDraftCaseToCCDWorkflow;

    @Mock
    private UpdateDraftCaseInCCDWorkflow updateDraftCaeInCCDWorkflow;

    @InjectMocks
    private CaseServiceImpl caseService;

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
        when(submitDraftCaseToCCDWorkflow.run(requestPayload, AUTH_TOKEN)).thenReturn(expectedPayload);
        when(submitDraftCaseToCCDWorkflow.errors()).thenReturn(Collections.emptyMap());

        Map<String, Object> actual = caseService.submitDraftCase(requestPayload, AUTH_TOKEN);

        assertThat(actual.get("returnedKey"), is("returnedValue"));

        verify(submitDraftCaseToCCDWorkflow).run(requestPayload, AUTH_TOKEN);
        verify(submitDraftCaseToCCDWorkflow).errors();
    }

    @Test
    public void givenDraftCaseDataInvalid_whenSubmit_thenReturnListOfErrors() throws Exception {
        when(submitDraftCaseToCCDWorkflow.run(requestPayload, AUTH_TOKEN)).thenReturn(expectedPayload);
        Map<String, Object> errors = singletonMap("new_Error", "An Error");
        when(submitDraftCaseToCCDWorkflow.errors()).thenReturn(errors);

        Map<String, Object> actual = caseService.submitDraftCase(requestPayload, AUTH_TOKEN);

        assertEquals(errors, actual);

        verify(submitDraftCaseToCCDWorkflow).run(requestPayload, AUTH_TOKEN);
        verify(submitDraftCaseToCCDWorkflow, times(2)).errors();
    }

    @Test
    public void givenDraftCaseUpdateValid_whenSubmit_thenReturnPayload() throws Exception {
        when(updateDraftCaeInCCDWorkflow.run(requestPayload, AUTH_TOKEN))
            .thenReturn(requestPayload);

        Map<String, Object> actual = caseService.updateDraftCase(requestPayload, AUTH_TOKEN);

        assertEquals(requestPayload, actual);

        verify(updateDraftCaeInCCDWorkflow).run(requestPayload, AUTH_TOKEN);
    }

    @Test
    public void givenDraftCaseDataInvalid_whenUpdating_thenReturnListOfErrors() throws Exception {
        when(updateDraftCaeInCCDWorkflow.run(requestPayload, AUTH_TOKEN)).thenReturn(expectedPayload);
        Map<String, Object> errors = singletonMap("new_Error", "An Error");
        when(updateDraftCaeInCCDWorkflow.errors()).thenReturn(errors);

        Map<String, Object> actual = caseService.updateDraftCase(requestPayload, AUTH_TOKEN);

        System.out.println("ACTUAL: " + actual);
        System.out.println("ERRORS: " + errors);
        assertEquals(errors, actual);

        verify(updateDraftCaeInCCDWorkflow).run(requestPayload, AUTH_TOKEN);
        verify(updateDraftCaeInCCDWorkflow, times(2)).errors();
    }
}
