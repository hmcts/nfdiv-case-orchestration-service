package uk.gov.hmcts.reform.divorce.orchestration.service.ccd;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_JURISDICTION_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class CasePatchServiceTest {

    private static final String USER_ID = "1";
    private static final String PATCH_EVENT_ID = "patchCase";
    private static final String BEARER_AUTHORISATION = "Bearer authorisation";

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @InjectMocks
    private CasePatchService casePatchService;

    @Before
    public void setServiceValue() {
        setField(casePatchService, "jurisdictionId", "someJurisdictionId");
        setField(casePatchService, "caseType", "someCaseType");
        setField(casePatchService, "patchEventId", PATCH_EVENT_ID);
    }

    @Test
    public void shouldPatchCase() {

        final Map<String, Object> caseData = new HashMap<>();
        final StartEventResponse startEventResponse = mock(StartEventResponse.class);
        final CaseDetails caseDetails = mock(CaseDetails.class);
        final User user = new User(BEARER_AUTHORISATION, UserDetails.builder().id(USER_ID).build());

        final CaseDataContent caseDataContent = getBuild(
            caseData,
            startEventResponse
        );

        when(coreCaseDataApi
            .startEventForCitizen(
                "Bearer authorisation",
                TEST_SERVICE_TOKEN,
                USER_ID,
                TEST_JURISDICTION_ID,
                TEST_CASE_TYPE,
                TEST_CASE_ID,
                PATCH_EVENT_ID))
            .thenReturn(startEventResponse);

        when(coreCaseDataApi
            .submitEventForCitizen(
                BEARER_AUTHORISATION,
                TEST_SERVICE_TOKEN,
                USER_ID,
                TEST_JURISDICTION_ID,
                TEST_CASE_TYPE,
                TEST_CASE_ID,
                true,
                caseDataContent))
            .thenReturn(caseDetails);

        casePatchService.patchCase(TEST_CASE_ID, caseData, user, TEST_SERVICE_TOKEN);

        verify(coreCaseDataApi)
            .startEventForCitizen(
                BEARER_AUTHORISATION,
                TEST_SERVICE_TOKEN,
                USER_ID,
                TEST_JURISDICTION_ID,
                TEST_CASE_TYPE,
                TEST_CASE_ID,
                PATCH_EVENT_ID);
        verify(coreCaseDataApi)
            .submitEventForCitizen(
                BEARER_AUTHORISATION,
                TEST_SERVICE_TOKEN,
                USER_ID,
                TEST_JURISDICTION_ID,
                TEST_CASE_TYPE,
                TEST_CASE_ID,
                true,
                caseDataContent);
    }

    private CaseDataContent getBuild(final Map<String, Object> caseData,
                                     final StartEventResponse startEventResponse) {

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary("Divorce case patch event")
                    .description("Patching Divorce Case")
                    .build()
            ).data(caseData)
            .build();
    }
}