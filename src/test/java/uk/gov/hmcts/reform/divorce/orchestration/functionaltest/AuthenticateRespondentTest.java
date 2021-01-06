package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetailsProvider;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ID_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID_TOKEN_HEADER;

public class AuthenticateRespondentTest extends MockedFunctionalTest {
    private static final String API_URL = "/authenticate-respondent";

    @Autowired
    private MockMvc webClient;

    @MockBean
    private UserDetailsProvider detailsProvider;

    @Test
    public void givenAuthTokenIsNull_whenAuthenticateRespondent_thenReturnBadRequest() throws Exception {
        webClient.perform(post(API_URL)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenInvalidUserToken_whenAuthenticateRespondent_thenReturnUnauthorized() throws Exception {
        final String errorMessage = "";

        webClient.perform(post(API_URL)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(ID_TOKEN_HEADER, TEST_ID_TOKEN))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(containsString(errorMessage)));
    }

    @Test
    public void givenUserDoesNotHaveAnyRole_whenAuthenticateRespondent_thenReturnUnauthorized() throws Exception {

        webClient.perform(post(API_URL)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(ID_TOKEN_HEADER, TEST_ID_TOKEN))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void givenUserDoesNotHaveLetterHolderRole_whenAuthenticateRespondent_thenReturnUnauthorized()
        throws Exception {

        webClient.perform(post(API_URL)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(ID_TOKEN_HEADER, TEST_ID_TOKEN))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void givenUserHasLetterHolderRole_whenAuthenticateRespondent_thenReturnOk() throws Exception {
        final UserDetails details = UserDetails
            .builder()
            .roles(Arrays.asList("letter-holder", "letter-loa1"))
            .build();

        Mockito
            .when(detailsProvider.getUserDetails(TEST_ID_TOKEN))
            .thenReturn(Optional.of(details));

        webClient.perform(post(API_URL)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(ID_TOKEN_HEADER, TEST_ID_TOKEN))
            .andExpect(status().isOk());
    }
}
