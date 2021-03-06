package uk.gov.hmcts.reform.divorce.orchestration.controller.generalreferral;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.controller.MockedFunctionalTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.time.LocalDate.now;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.GeneralReferralUtil.buildCallbackRequest;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.GeneralReferralUtil.buildCaseDataWithGeneralReferralFee;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class GeneralReferralTest extends MockedFunctionalTest {

    private static final String API_URL = "/general-referral";
    private static final String EXISTING_PREVIOUS_STATE = "existingPreviousState";

    @Autowired
    private MockMvc webClient;

    private CcdCallbackRequest ccdCallbackRequest;

    @Test
    public void givenCaseData_whenGeneralReferralFee_Yes_ThenState_Is_AwaitingGeneralReferralPayment() throws Exception {
        ccdCallbackRequest = buildCallbackRequest(
            buildCaseDataWithGeneralReferralFee(YES_VALUE),
            TEST_STATE);

        performRequestAndValidateReferralFeeAndState(YES_VALUE, TEST_STATE, CcdStates.AWAITING_GENERAL_REFERRAL_PAYMENT);
    }

    @Test
    public void givenCaseData_whenGeneralReferralFee_No_ThenState_Is_AwaitingGeneralConsideration() throws Exception {
        ccdCallbackRequest = buildCallbackRequest(
            buildCaseDataWithGeneralReferralFee(NO_VALUE),
            TEST_STATE);

        performRequestAndValidateReferralFeeAndState(NO_VALUE, TEST_STATE, CcdStates.AWAITING_GENERAL_CONSIDERATION);
    }

    @Test
    public void givenState_AwaitingGeneralReferralPayment_WhenFee_No_ThenState_Is_AwaitingGeneralConsideration_And_PreviousStateFieldIsNotUpdated()
        throws Exception {
        Map<String, Object> caseData = buildCaseDataWithGeneralReferralFee(NO_VALUE);
        caseData.put(CcdFields.GENERAL_REFERRAL_PREVIOUS_CASE_STATE, EXISTING_PREVIOUS_STATE);
        ccdCallbackRequest = buildCallbackRequest(
            caseData,
            CcdStates.AWAITING_GENERAL_REFERRAL_PAYMENT);

        performRequestAndValidateReferralFeeAndState(NO_VALUE, EXISTING_PREVIOUS_STATE, CcdStates.AWAITING_GENERAL_CONSIDERATION);
    }

    @Test
    public void givenState_AwaitingGeneralConsideration_WhenFee_Yes_ThenState_Is_AwaitingGeneralReferralPayment_And_PreviousStateFieldIsNotUpdated()
        throws Exception {
        Map<String, Object> caseData = buildCaseDataWithGeneralReferralFee(YES_VALUE);
        caseData.put(CcdFields.GENERAL_REFERRAL_PREVIOUS_CASE_STATE, EXISTING_PREVIOUS_STATE);
        ccdCallbackRequest = buildCallbackRequest(
            caseData,
            CcdStates.AWAITING_GENERAL_CONSIDERATION);

        performRequestAndValidateReferralFeeAndState(YES_VALUE, EXISTING_PREVIOUS_STATE, CcdStates.AWAITING_GENERAL_REFERRAL_PAYMENT);
    }

    private void performRequestAndValidateReferralFeeAndState(
        String referralFeeValue, String previousCaseState, String newCaseState) throws Exception {
        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(
                allOf(
                    isJson(),
                    hasJsonPath("$.data.GeneralReferralFee", is(referralFeeValue)),
                    hasJsonPath(
                        "$.data.GeneralApplicationAddedDate",
                        is(DateUtils.formatDateFromLocalDate(now()))
                    ),
                    hasJsonPath("$.state", is(newCaseState)),
                    hasJsonPath("$.data.StateBeforeGeneralReferral", is(previousCaseState)))
            ));
    }

}
