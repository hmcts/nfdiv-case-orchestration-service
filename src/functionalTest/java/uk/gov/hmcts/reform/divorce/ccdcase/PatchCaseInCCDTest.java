package uk.gov.hmcts.reform.divorce.ccdcase;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJson;

public class PatchCaseInCCDTest extends IntegrationTest {

    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/maintenance/patch/";

    @Value("${case.orchestration.ccd.case.context-path}")
    private String casePath;

    @Test
    public void shouldPatchCaseFields() throws Exception {

        final UserDetails citizenUser = createCitizenUser();

        final Response response = submitDraftCase(citizenUser, "initial-case-for-patch.json");
        assertThat(response.getStatusCode(), is(OK.value()));

        final String caseId = response.getBody().path(CASE_ID_JSON_KEY);
        final String patchWithIdJson = loadJson(PAYLOAD_CONTEXT_PATH + "patch-case.json")
            .replace("caseId", caseId);

        final Response beforePatchResponse = getCase(citizenUser.getAuthToken());
        assertThat(beforePatchResponse.getStatusCode(), is(OK.value()));

        assertThat(beforePatchResponse.getBody().asString(),
            isJson(allOf(
                withJsonPath("state", is("Draft")),
                withJsonPath("data.D8ScreenHasMarriageBroken", is("YES")),
                withoutJsonPath("data.D8PetitionerFirstName"),
                withoutJsonPath("data.D8PetitionerLastName"),
                withoutJsonPath("data.D8DerivedPetitionerCurrentFullName"),
                withJsonPath("data.D8FinancialOrderFor", contains("petitioner", "children"))
            )));

        final Response patchResponse = patchCase(citizenUser, patchWithIdJson);
        assertThat(patchResponse.getStatusCode(), is(OK.value()));

        final Response afterPatchResponse = getCase(citizenUser.getAuthToken());
        assertThat(afterPatchResponse.getStatusCode(), is(OK.value()));

        assertThat(afterPatchResponse.getBody().asString(),
            isJson(allOf(
                withJsonPath("state", is("Draft")),
                withJsonPath("data.D8ScreenHasMarriageBroken", is("YES")),
                withJsonPath("data.D8PetitionerFirstName", is("John")),
                withJsonPath("data.D8PetitionerLastName", is("Jones")),
                withJsonPath("data.D8DerivedPetitionerCurrentFullName", is("John Jones")),
                withJsonPath("data.D8FinancialOrderFor", contains("petitioner"))
            )));
    }

    @Test
    public void shouldRespondWithBadRequestIfNoIdFieldPresent() throws Exception {

        final String patchWithOutIdJson = loadJson(PAYLOAD_CONTEXT_PATH + "patch-no-id.json");

        final Response patchResponse = patchCase(createCitizenUser(), patchWithOutIdJson);

        assertThat(patchResponse.getStatusCode(), is(BAD_REQUEST.value()));
        assertThat(patchResponse.getBody().asString(), containsString("Missing field 'id' in json payload."));
    }

    @Test
    public void shouldRespondWithBadRequestIfNoDataFieldPresent() throws Exception {

        final String patchWithOutIdJson = loadJson(PAYLOAD_CONTEXT_PATH + "patch-no-data.json");

        final Response patchResponse = patchCase(createCitizenUser(), patchWithOutIdJson);

        assertThat(patchResponse.getStatusCode(), is(BAD_REQUEST.value()));
        assertThat(patchResponse.getBody().asString(), containsString("Missing field 'data' in json payload."));
    }

    @Test
    public void shouldRespondWithBadRequestIfIncorrectFieldPresent() throws Exception {

        final String patchWithOutIdJson = loadJson(PAYLOAD_CONTEXT_PATH + "patch-bad-data.json");

        final Response patchResponse = patchCase(createCitizenUser(), patchWithOutIdJson);

        assertThat(patchResponse.getStatusCode(), is(BAD_REQUEST.value()));
        assertThat(patchResponse.getBody().asString(), containsString("Case reference is not valid"));
    }

    private Response patchCase(UserDetails userDetails, String patchWithIdJson) throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, APPLICATION_JSON.toString());
        headers.put(AUTHORIZATION, userDetails.getAuthToken());

        return RestUtil.patchToRestService(
            serverUrl + casePath,
            headers,
            patchWithIdJson,
            Collections.emptyMap()
        );
    }

    private Response submitDraftCase(UserDetails userDetails, String fileName) throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, APPLICATION_JSON.toString());
        headers.put(AUTHORIZATION, userDetails.getAuthToken());

        String body = null;
        if (fileName != null) {
            body = loadJson(PAYLOAD_CONTEXT_PATH + fileName);
        }

        return RestUtil.postToRestService(
            serverUrl + casePath,
            headers,
            body
        );
    }

    private Response getCase(final String userToken) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, APPLICATION_JSON.toString());
        headers.put(AUTHORIZATION, userToken);

        return RestUtil.getFromRestService(
            serverUrl + casePath,
            headers
        );
    }
}
