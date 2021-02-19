package uk.gov.hmcts.reform.divorce.ccdcase;

import io.restassured.response.Response;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtConstants.REASON_FOR_DIVORCE_KEY;

public class GetCaseFromCcdTest extends CcdSubmissionSupport {

    @Value("${case.orchestration.maintenance.case.context-path}")
    private String getCaseContextPath;

    @Value("${case.orchestration.amend-petition.context-path}")
    private String amendPetitionContextPath;

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void givenCaseExists_whenRetrieveCase_thenReturnResponse() {
        UserDetails userDetails = createCitizenUser();

        CaseDetails caseDetails = submitCase("submit-complete-case.json", userDetails,
            Pair.of(D_8_PETITIONER_EMAIL, userDetails.getEmailAddress()));

        Response cosResponse = getCase(userDetails.getAuthToken());

        assertEquals(HttpStatus.OK.value(), cosResponse.getStatusCode());
        assertEquals(String.valueOf(caseDetails.getId()), cosResponse.path(ID));
        assertEquals(AWAITING_PAYMENT, cosResponse.path(STATE_CCD_FIELD));
    }

    @Test
    public void givenMultipleSubmittedCaseInCcd_whenGetCase_thenReturn300() {
        UserDetails userDetails = createCitizenUser();

        submitCase("submit-complete-case.json", userDetails,
            Pair.of(D_8_PETITIONER_EMAIL, userDetails.getEmailAddress()));
        submitCase("submit-complete-case.json", userDetails,
            Pair.of(D_8_PETITIONER_EMAIL, userDetails.getEmailAddress()));

        Response cosResponse = getCase(userDetails.getAuthToken());

        assertEquals(HttpStatus.MULTIPLE_CHOICES.value(), cosResponse.getStatusCode());
        assertEquals(cosResponse.getBody().asString(), "");
    }

    @Test
    public void givenNoCaseExistsInCcd_whenGetCase_thenReturn404() {
        UserDetails userDetails = createCitizenUser();

        Response cosResponse = getCase(userDetails.getAuthToken());

        assertEquals(HttpStatus.NOT_FOUND.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenAmendedAndDraftCaseExists_whenGetCase_thenReturnDraftCaseOnly() {
        UserDetails userDetails = createCitizenUser();

        // given case submitted
        CaseDetails caseDetails = submitCase("submit-complete-case.json", userDetails,
            Pair.of(D_8_PETITIONER_EMAIL, userDetails.getEmailAddress()));

        String caseId = String.valueOf(caseDetails.getId());
        assertThat(caseId, notNullValue());

        // and amended which results in new draft case and old case in amended state
        // before amending case needs to be in state as configured in precondition
        updateCaseForCitizen(caseId, null, TEST_AOS_STARTED_EVENT_ID, userDetails);
        updateCaseForCitizen(caseId, null, AOS_RECEIVED_NO_ADMIT_EVENT_ID, userDetails);

        Response amendCaseResponse = amendCase(userDetails.getAuthToken(), caseId);

        assertThat(amendCaseResponse.getStatusCode(), is(HttpStatus.OK.value()));

        String draftSubmittedCaseId = submitDraftCase(userDetails, amendCaseResponse);

        // when get case
        Response cosResponse = getCase(userDetails.getAuthToken());

        // then return only draft case and filter amended state case
        assertThat(cosResponse.getStatusCode(), is(HttpStatus.OK.value()));
        assertThat(cosResponse.path(ID), equalTo(draftSubmittedCaseId));
    }

    private String submitDraftCase(UserDetails userDetails, Response amendCaseResponse) {
        //Fill in mandatory data that's removed from original case
        Map<String, Object> newDraftDocument = amendCaseResponse.getBody().as(Map.class);

        newDraftDocument.put(REASON_FOR_DIVORCE_KEY, "unreasonable-behaviour");
        newDraftDocument.put(REASON_FOR_DIVORCE_BEHAVIOUR_DETAILS, singletonList("my partner did unreasonable things"));
        newDraftDocument.put(CLAIMS_COSTS, YES_VALUE);
        newDraftDocument.put(CONFIRM_PRAYER, YES_VALUE);

        //Submit amended case
        Map<String, Object> submittedCase = cosApiClient.submitCase(userDetails.getAuthToken(), newDraftDocument);
        String caseId = (String) submittedCase.get(CASE_ID_JSON_KEY);
        assertThat(submittedCase.get(CASE_ID_JSON_KEY), is(notNullValue()));
        return caseId;
    }

    private Response getCase(String userToken) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        headers.put(HttpHeaders.AUTHORIZATION, userToken);

        return RestUtil.getFromRestService(
            serverUrl + getCaseContextPath,
            headers
        );
    }

    private Response amendCase(String userToken, String caseId) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        return RestUtil.putToRestService(
            serverUrl + amendPetitionContextPath + "/" + caseId,
            headers,
            null,
            new HashMap<>()
        );
    }
}