package uk.gov.hmcts.reform.divorce.ccdcase;

import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.support.CcdClientSupport;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

public class PatchCaseInCCD extends IntegrationTest {

    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/maintenance/patch/";
    private static final String DRAFT_DIVORCE_SESSION_JSON_PATH = "initial-submit.json";
    private static final String PATCH_DIVORCE_SESSION_JSON_PATH = "patch.json";
    private static final String PATCH_DIVORCE_SESSION_JSON_PATH_NO_ID = "patch-no-id.json";

    @Autowired
    private CcdClientSupport ccdClientSupport;

    @Value("${case.orchestration.maintenance.case.context-path}")
    private String draftCaseContextPath;

    @Test
    public void givenDivorceSession_whenDraftUpdateIsCalled_caseIdIsReturned() throws Exception {
        UserDetails citizenUser = createCitizenUser();

        final Response response = submitDraftCase(citizenUser, DRAFT_DIVORCE_SESSION_JSON_PATH);
        final String caseId = response.getBody().path(CASE_ID_JSON_KEY);
        assertThat(response.getStatusCode(), is(OK.value()));

        final String patchWithIdJson =
            ResourceLoader.loadJson(PAYLOAD_CONTEXT_PATH + PATCH_DIVORCE_SESSION_JSON_PATH)
            .replace("caseId", caseId);

        final Response patchResponse = patchCase(citizenUser,  patchWithIdJson);

        assertEquals(HttpStatus.OK.value(), patchResponse.getStatusCode());
    }

    @Test
    public void givenDivorceSessionWithNoId_whenDraftUpdateIsCalled_400ErrorIsReturned() throws Exception {
        UserDetails citizenUser = createCitizenUser();

        final Response response = submitDraftCase(citizenUser, DRAFT_DIVORCE_SESSION_JSON_PATH);
        assertThat(response.getStatusCode(), is(OK.value()));

        final String patchWithOutIdJson =
            ResourceLoader.loadJson(PAYLOAD_CONTEXT_PATH + PATCH_DIVORCE_SESSION_JSON_PATH_NO_ID);

        final Response patchResponse = patchCase(citizenUser,  patchWithOutIdJson);

        assertEquals(400, patchResponse.getStatusCode());
    }

    private Response patchCase(UserDetails userDetails, String patchWithIdJson) throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        headers.put(HttpHeaders.AUTHORIZATION, userDetails.getAuthToken());

        return RestUtil.patchToRestService(
            serverUrl + draftCaseContextPath,
            headers,
            patchWithIdJson,
            Collections.emptyMap()
        );
    }

    private Response submitDraftCase(UserDetails userDetails, String fileName) throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        headers.put(HttpHeaders.AUTHORIZATION, userDetails.getAuthToken());

        String body = null;
        if (fileName != null) {
            body = ResourceLoader.loadJson(PAYLOAD_CONTEXT_PATH + fileName);
        }

        return RestUtil.postToRestService(
            serverUrl + draftCaseContextPath,
            headers,
            body
        );
    }
}
