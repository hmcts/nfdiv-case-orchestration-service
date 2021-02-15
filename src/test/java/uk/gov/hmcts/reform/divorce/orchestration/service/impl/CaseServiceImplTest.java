package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CMSClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;

@RunWith(MockitoJUnitRunner.class)
public class CaseServiceImplTest {

    @Mock
    private CMSClient cmsClient;

    @InjectMocks
    private CaseServiceImpl caseService;

    @Test
    public void givenDraftCaseDataValid_whenSubmit_thenReturnPayload() throws Exception {

        final Map<String, Object> requestPayload = singletonMap("requestPayloadKey", "requestPayloadValue");
        final Map<String, Object> expectedPayload = new HashMap<>();
        expectedPayload.put("returnedKey", "returnedValue");

        when(cmsClient.submitDraftCase(requestPayload, AUTH_TOKEN)).thenReturn(expectedPayload);

        final Map<String, Object> actual = caseService.submitDraftCase(requestPayload, AUTH_TOKEN);

        assertThat(actual.get("returnedKey"), is("returnedValue"));
        verify(cmsClient).submitDraftCase(requestPayload, AUTH_TOKEN);
    }

    @Test
    public void givenCaseUpdateValid_whenSubmit_thenReturnPayload() throws Exception {

        final Map<String, Object> requestPayload = singletonMap("requestPayloadKey", "requestPayloadValue");

        when(cmsClient.patchCase(requestPayload, AUTH_TOKEN)).thenReturn(requestPayload);

        final Map<String, Object> actual = caseService.patchCase(requestPayload, AUTH_TOKEN);

        assertEquals(requestPayload, actual);
        verify(cmsClient).patchCase(requestPayload, AUTH_TOKEN);
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

        when(cmsClient.getCaseFromCcd(AUTH_TOKEN)).thenReturn(cmsResponse);

        final CaseDataResponse actualResponse = caseService.getCase(AUTH_TOKEN);

        assertThat(actualResponse.getData(), is(caseData));
        assertThat(actualResponse.getCaseId(), is(TEST_CASE_ID));
        assertThat(actualResponse.getState(), is(TEST_STATE));
        assertThat(actualResponse.getCourt(), is(TEST_COURT));

        verify(cmsClient).getCaseFromCcd(AUTH_TOKEN);
    }

    @Test
    public void givenNoCaseExists_whenGetCase_thenReturnThrowException() {
        final Request request = Request.create(Request.HttpMethod.GET, "url",
            new HashMap<>(), null, new RequestTemplate());

        when(cmsClient.getCaseFromCcd(AUTH_TOKEN)).thenThrow(new FeignException.NotFound("", request, null));

        final FeignException feignException = assertThrows(FeignException.class, () -> caseService.getCase(AUTH_TOKEN));

        assertThat(feignException.status(), is(404));

        verify(cmsClient).getCaseFromCcd(AUTH_TOKEN);
    }
}
