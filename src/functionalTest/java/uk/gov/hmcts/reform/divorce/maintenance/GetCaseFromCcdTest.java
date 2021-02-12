package uk.gov.hmcts.reform.divorce.maintenance;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;

public class GetCaseFromCcdTest extends CcdSubmissionSupport {

    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/retrieve-case/";

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${case.orchestration.maintenance.get-case.context-path}")
    private String getCaseContextPath;

    @Test
    public void givenCaseExists_whenRetrieveCase_thenReturnResponse() throws Exception {

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

    private Response getCase(String userToken) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        headers.put(HttpHeaders.AUTHORIZATION, userToken);

        return RestUtil.getFromRestService(
            serverUrl + getCaseContextPath,
            headers
        );
    }
}