package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.client.CMSClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.GetCaseResponse;
import uk.gov.hmcts.reform.divorce.orchestration.exception.CaseAlreadyExistsException;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_JURISDICTION_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AMEND_PETITION_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;

@RunWith(MockitoJUnitRunner.class)
public class CaseServiceImplTest {

    private static final String USER_ID = "someUserId";

    private static final UserDetails USER_DETAILS = UserDetails.builder()
        .id(USER_ID)
        .email(TEST_USER_EMAIL)
        .build();

    private static final Long CASE_ID_1 = 1L;

    @Mock
    private CMSClient cmsClient;

    @Mock
    private IdamClient idamClient;

    @Mock
    private AuthUtil authUtil;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @InjectMocks
    private CaseServiceImpl caseService;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(caseService, "jurisdictionId", "someJurisdictionId");
        ReflectionTestUtils.setField(caseService, "caseType", "someCaseType");
    }

    @Test
    public void givenDraftCaseDataValid_whenSubmit_thenReturnPayload() throws CaseAlreadyExistsException {
        final Map<String, Object> requestPayload = singletonMap("requestPayloadKey", "requestPayloadValue");

        when(authUtil.getBearerToken(AUTH_TOKEN)).thenReturn(BEARER_AUTH_TOKEN);
        when(idamClient.getUserDetails(BEARER_AUTH_TOKEN)).thenReturn(USER_DETAILS);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(BEARER_AUTH_TOKEN, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE, emptyMap())
        ).thenReturn(emptyList());

        when(cmsClient.submitDraftCase(requestPayload, AUTH_TOKEN)).thenReturn(requestPayload);

        final Map<String, Object> actual = caseService.submitDraftCase(requestPayload, AUTH_TOKEN);

        assertThat(actual.get("requestPayloadKey"), is("requestPayloadValue"));
        verify(cmsClient).submitDraftCase(requestPayload, AUTH_TOKEN);
    }

    @Test
    public void givenExistingCase_whenSubmitCase_thenReturnException() throws CaseNotFoundException {
        final Map<String, Object> requestPayload = singletonMap("requestPayloadKey", "requestPayloadValue");
        CaseDetails caseDetails = createCaseDetails(CASE_ID_1, TEST_STATE);

        when(authUtil.getBearerToken(AUTH_TOKEN)).thenReturn(BEARER_AUTH_TOKEN);
        when(idamClient.getUserDetails(BEARER_AUTH_TOKEN)).thenReturn(USER_DETAILS);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(BEARER_AUTH_TOKEN, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE, emptyMap())
        ).thenReturn(List.of(caseDetails));

        final CaseAlreadyExistsException caseAlreadyExistsException = assertThrows(CaseAlreadyExistsException.class,
            () -> caseService.submitDraftCase(requestPayload, AUTH_TOKEN));

        assertThat(caseAlreadyExistsException.getMessage(), is("Existing case found"));
    }

    @Test
    public void givenCaseUpdateValid_whenSubmit_thenReturnPayload() {

        final Map<String, Object> requestPayload = singletonMap("requestPayloadKey", "requestPayloadValue");

        when(cmsClient.patchCase(requestPayload, AUTH_TOKEN)).thenReturn(requestPayload);

        final Map<String, Object> actual = caseService.patchCase(requestPayload, AUTH_TOKEN);

        assertEquals(requestPayload, actual);
        verify(cmsClient).patchCase(requestPayload, AUTH_TOKEN);
    }

    @Test
    public void whenGetCase_thenProceedAsExpected() throws CaseNotFoundException {
        CaseDetails caseDetails = createCaseDetails(CASE_ID_1, TEST_STATE);

        final Map<String, Object> caseData = singletonMap(D_8_PETITIONER_EMAIL, TEST_USER_EMAIL);

        when(authUtil.getBearerToken(AUTH_TOKEN)).thenReturn(BEARER_AUTH_TOKEN);
        when(idamClient.getUserDetails(BEARER_AUTH_TOKEN)).thenReturn(USER_DETAILS);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(BEARER_AUTH_TOKEN, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE, emptyMap())
        ).thenReturn(List.of(caseDetails));

        final GetCaseResponse getCaseResponse = caseService.getCase(AUTH_TOKEN);

        assertThat(getCaseResponse.getData(), is(caseData));
        assertThat(getCaseResponse.getId(), is("1"));
        assertThat(getCaseResponse.getState(), is(TEST_STATE));

        verify(authUtil).getBearerToken(AUTH_TOKEN);
        verify(idamClient).getUserDetails(BEARER_AUTH_TOKEN);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(BEARER_AUTH_TOKEN, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE, emptyMap());
    }

    @Test
    public void givenNoCaseExists_whenGetCase_thenReturnThrowException() {
        when(authUtil.getBearerToken(AUTH_TOKEN)).thenReturn(BEARER_AUTH_TOKEN);
        when(idamClient.getUserDetails(BEARER_AUTH_TOKEN)).thenReturn(USER_DETAILS);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(BEARER_AUTH_TOKEN, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE, emptyMap())
        ).thenReturn(emptyList());

        final CaseNotFoundException caseNotFoundException = assertThrows(CaseNotFoundException.class, () -> caseService.getCase(AUTH_TOKEN));

        assertThat(caseNotFoundException.getMessage(), equalTo("No case found for user id " + USER_ID));

        verify(authUtil).getBearerToken(AUTH_TOKEN);
        verify(idamClient).getUserDetails(BEARER_AUTH_TOKEN);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(BEARER_AUTH_TOKEN, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE, emptyMap());
    }

    @Test
    public void givenMultipleCasesExists_whenGetCase_thenReturnThrowException() {
        CaseDetails caseDetails1 = createCaseDetails(1L, TEST_STATE);
        CaseDetails caseDetails2 = createCaseDetails(2L, TEST_STATE);

        when(authUtil.getBearerToken(AUTH_TOKEN)).thenReturn(BEARER_AUTH_TOKEN);
        when(idamClient.getUserDetails(BEARER_AUTH_TOKEN)).thenReturn(USER_DETAILS);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(BEARER_AUTH_TOKEN, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE, emptyMap())
        ).thenReturn(List.of(caseDetails1, caseDetails2));

        final DuplicateCaseException duplicateCaseException = assertThrows(DuplicateCaseException.class, () -> caseService.getCase(AUTH_TOKEN));

        assertThat(duplicateCaseException.getMessage(), equalTo("There are [2] cases for the user [someUserId]"));

        verify(authUtil).getBearerToken(AUTH_TOKEN);
        verify(idamClient).getUserDetails(BEARER_AUTH_TOKEN);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(BEARER_AUTH_TOKEN, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE, emptyMap());
    }

    @Test
    public void givenOnlyAmendCaseExists_whenGetCase_thenReturn404() throws CaseNotFoundException {
        CaseDetails caseDetails = createCaseDetails(1L, AMEND_PETITION_STATE);

        when(authUtil.getBearerToken(AUTH_TOKEN)).thenReturn(BEARER_AUTH_TOKEN);
        when(idamClient.getUserDetails(BEARER_AUTH_TOKEN)).thenReturn(USER_DETAILS);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(BEARER_AUTH_TOKEN, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE, emptyMap())
        ).thenReturn(List.of(caseDetails));

        final CaseNotFoundException caseNotFoundException = assertThrows(CaseNotFoundException.class, () -> caseService.getCase(AUTH_TOKEN));

        assertThat(caseNotFoundException.getMessage(), equalTo("No case found for user id " + USER_ID));

        verify(authUtil).getBearerToken(AUTH_TOKEN);
        verify(idamClient).getUserDetails(BEARER_AUTH_TOKEN);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(BEARER_AUTH_TOKEN, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE, emptyMap());
    }

    @Test
    public void givenAmendedAndSubmittedCasesExists_whenGetCase_thenReturnSubmittedStateCase() throws CaseNotFoundException {
        CaseDetails caseDetails1 = createCaseDetails(CASE_ID_1, TEST_STATE);
        CaseDetails caseDetails2 = createCaseDetails(CASE_ID_1, AMEND_PETITION_STATE);

        final Map<String, Object> caseData = singletonMap(D_8_PETITIONER_EMAIL, TEST_USER_EMAIL);

        when(authUtil.getBearerToken(AUTH_TOKEN)).thenReturn(BEARER_AUTH_TOKEN);
        when(idamClient.getUserDetails(BEARER_AUTH_TOKEN)).thenReturn(USER_DETAILS);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(BEARER_AUTH_TOKEN, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE, emptyMap())
        ).thenReturn(List.of(caseDetails1, caseDetails2));

        final GetCaseResponse getCaseResponse = caseService.getCase(AUTH_TOKEN);

        assertThat(getCaseResponse.getData(), is(caseData));
        assertThat(getCaseResponse.getId(), is("1"));
        assertThat(getCaseResponse.getState(), is(TEST_STATE));

        verify(authUtil).getBearerToken(AUTH_TOKEN);
        verify(idamClient).getUserDetails(BEARER_AUTH_TOKEN);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(BEARER_AUTH_TOKEN, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE, emptyMap());
    }

    private CaseDetails createCaseDetails(Long id, String state) {
        return CaseDetails.builder()
            .id(id)
            .state(state)
            .createdDate(LocalDateTime.now())
            .data(ImmutableMap.of(D_8_PETITIONER_EMAIL, TEST_USER_EMAIL))
            .build();
    }
}