package uk.gov.hmcts.reform.divorce.ccdcase;

import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.support.cos.RetrieveCaseSupport;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

public class PostCaseToCCDTest extends RetrieveCaseSupport {

    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/maintenance/submit/";
    private static final String DRAFT_DIVORCE_SESSION_JSON_PATH = "draft-divorce-session.json";

    @Value("${case.orchestration.maintenance.case.context-path}")
    private String draftCaseCreationContextPath;

    @Test
    public void givenNoExistingCase_whenSubmitIsCalled_CaseIsCreated() throws Exception {
        UserDetails userDetails = createCitizenUser();
        Response submissionResponse = submitCase(userDetails, DRAFT_DIVORCE_SESSION_JSON_PATH);

        ResponseBody caseCreationResponseBody = submissionResponse.getBody();
        assertThat(submissionResponse.getStatusCode(), is(HttpStatus.OK.value()));
        assertThat(caseCreationResponseBody.path(CASE_ID_JSON_KEY), is(not(emptyOrNullString())));
    }

    @Test
    public void givenExistingCase_whenSubmitIsCalled_ExistingCaseExceptionThrown() throws Exception {
        UserDetails userDetails = createCitizenUser();
        Response firstSubmissionResponse = submitCase(userDetails, DRAFT_DIVORCE_SESSION_JSON_PATH);
        Response secondSubmissionResponse = submitCase(userDetails, DRAFT_DIVORCE_SESSION_JSON_PATH);

        ResponseBody firstCaseCreationResponseBody = firstSubmissionResponse.getBody();
        assertThat(firstSubmissionResponse.getStatusCode(), is(HttpStatus.OK.value()));
        assertThat(firstCaseCreationResponseBody.path(CASE_ID_JSON_KEY), is(not(emptyOrNullString())));

        assertThat(secondSubmissionResponse.getStatusCode(), is(HttpStatus.BAD_REQUEST.value()));
    }

    private Response submitCase(UserDetails userDetails, String fileName) throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        headers.put(HttpHeaders.AUTHORIZATION, userDetails.getAuthToken());

        String body = ResourceLoader.loadJson(PAYLOAD_CONTEXT_PATH + fileName);

        return RestUtil.postToRestService(
                serverUrl + draftCaseCreationContextPath,
                headers,
                body
        );
    }
}