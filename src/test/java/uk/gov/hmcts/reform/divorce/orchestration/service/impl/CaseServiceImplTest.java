package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.PatchCaseInCCDWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitDraftCaseToCCDWorkflow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_PIN;

@RunWith(MockitoJUnitRunner.class)
public class CaseServiceImplTest {

    @Mock
    private SubmitDraftCaseToCCDWorkflow submitDraftCaseToCCDWorkflow;

    @Mock
    private PatchCaseInCCDWorkflow updateDraftCaeInCCDWorkflow;

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

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
    public void givenCaseUpdateValid_whenSubmit_thenReturnPayload() throws Exception {
        when(updateDraftCaeInCCDWorkflow.run(requestPayload, AUTH_TOKEN))
            .thenReturn(requestPayload);

        Map<String, Object> actual = caseService.patchCase(requestPayload, AUTH_TOKEN);

        assertEquals(requestPayload, actual);

        verify(updateDraftCaeInCCDWorkflow).run(requestPayload, AUTH_TOKEN);
    }

    @Test
    public void givenCaseDataInvalid_whenUpdating_thenReturnListOfErrors() throws Exception {
        when(updateDraftCaeInCCDWorkflow.run(requestPayload, AUTH_TOKEN)).thenReturn(expectedPayload);
        Map<String, Object> errors = singletonMap("new_Error", "An Error");
        when(updateDraftCaeInCCDWorkflow.errors()).thenReturn(errors);

        Map<String, Object> actual = caseService.patchCase(requestPayload, AUTH_TOKEN);

        System.out.println("ACTUAL: " + actual);
        System.out.println("ERRORS: " + errors);
        assertEquals(errors, actual);

        verify(updateDraftCaeInCCDWorkflow).run(requestPayload, AUTH_TOKEN);
        verify(updateDraftCaeInCCDWorkflow, times(2)).errors();
    }

    @Test
    public void whenGetCase_thenProceedAsExpected() {
        final Map<String, Object> caseData = singletonMap(D_8_DIVORCE_UNIT, TEST_COURT);
        final CaseDetails cmsResponse =
            CaseDetails.builder()
                .caseData(caseData)
                .caseId(TEST_CASE_ID)
                .state(TEST_STATE)
                .build();

        when(caseMaintenanceClient.getCaseFromCcd(AUTH_TOKEN)).thenReturn(cmsResponse);

        CaseDataResponse actualResponse = caseService.getCase(AUTH_TOKEN);

        assertThat(actualResponse.getData(), is(caseData));
        assertThat(actualResponse.getCaseId(), is(TEST_CASE_ID));
        assertThat(actualResponse.getState(), is(TEST_STATE));
        assertThat(actualResponse.getCourt(), is(TEST_COURT));

        verify(caseMaintenanceClient).getCaseFromCcd(AUTH_TOKEN);
    }

    @Test
    public void givenNoCaseExists_whenGetCase_thenReturnThrowException() {
        Request request = Request.create(Request.HttpMethod.GET, "url",
            new HashMap<>(), null, new RequestTemplate());

        when(caseMaintenanceClient.getCaseFromCcd(AUTH_TOKEN)).thenThrow(new FeignException.NotFound("", request, null));

        FeignException feignException = assertThrows(FeignException.class, () -> caseService.getCase(AUTH_TOKEN));

        assertThat(feignException.status(), is(404));

        verify(caseMaintenanceClient).getCaseFromCcd(AUTH_TOKEN);
    }
}
