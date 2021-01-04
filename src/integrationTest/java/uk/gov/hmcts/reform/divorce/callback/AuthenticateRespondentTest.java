package uk.gov.hmcts.reform.divorce.callback;

import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID_TOKEN_HEADER;

public class AuthenticateRespondentTest extends IntegrationTest {

    @Value("${case.orchestration.authenticate-respondent.context-path}")
    private String contextPath;

    @Test
    public void givenUserHasLetterHolderRole_whenAuthenticateUser_thenReturnOk() {
        Response cosResponse = authenticateUser(createCitizenUser("letter-holder"));

        assertEquals(HttpStatus.OK.value(), cosResponse.getStatusCode());
    }

    private Response authenticateUser(UserDetails user) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        headers.put(HttpHeaders.AUTHORIZATION, user.getAuthToken());
        headers.put(ID_TOKEN_HEADER, user.getIdToken());

        return
            RestUtil.postToRestService(
                serverUrl + contextPath,
                headers,
                null
            );
    }
}
