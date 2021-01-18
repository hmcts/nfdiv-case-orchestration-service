package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.utils.UserDetailsProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticateRespondentTaskTest {
    private static final String AUTH_TOKEN = "some token";
    private static final String BEARER_AUTH_TOKEN = "Bearer some token";
    private static final String ID_TOKEN = "some token";
    private static final Boolean PAYLOAD = false;

    private final TaskContext context = new DefaultTaskContext();

    @Mock
    private UserDetailsProvider detailsProvider;

    @InjectMocks
    private AuthenticateRespondentTask classUnderTest;

    @Before
    public void setup() {
        context.setTransientObject(ID_TOKEN_JSON_KEY, ID_TOKEN);
    }

    @Test
    public void givenUserDetailsIsNull_whenExecute_thenReturnFalse() {
        Mockito.when(detailsProvider.getUserDetails(ID_TOKEN)).thenReturn(Optional.empty());

        assertFalse(classUnderTest.execute(context, PAYLOAD));
    }

    @Test
    public void givenRolesIsNull_whenExecute_thenReturnFalse() {
        UserDetails details = UserDetails.builder().build();
        Mockito.when(detailsProvider.getUserDetails(ID_TOKEN)).thenReturn(Optional.of(details));

        assertFalse(classUnderTest.execute(context, PAYLOAD));
    }

    @Test
    public void givenRolesIsEmpty_whenExecute_thenReturnFalse() {
        UserDetails details = UserDetails
            .builder()
            .roles(Collections.emptyList())
            .build();

        Mockito
            .when(detailsProvider.getUserDetails(ID_TOKEN))
            .thenReturn(Optional.of(details));

        assertFalse(classUnderTest.execute(context, PAYLOAD));
    }

    @Test
    public void givenRolesDoesNotContainLetterHolderRole_whenExecute_thenReturnFalse() {
        UserDetails details = UserDetails
            .builder()
            .roles(Collections.singletonList("letter-loa1"))
            .build();

        Mockito
            .when(detailsProvider.getUserDetails(ID_TOKEN))
            .thenReturn(Optional.of(details));

        assertFalse(classUnderTest.execute(context, PAYLOAD));
    }

    @Test
    public void givenRolesAreEmptyOrBlank_whenExecute_thenReturnFalse() {
        UserDetails details = UserDetails
            .builder()
            .roles(Arrays.asList("", " " ))
            .build();

        Mockito
            .when(detailsProvider.getUserDetails(ID_TOKEN))
            .thenReturn(Optional.of(details));

        assertFalse(classUnderTest.execute(context, PAYLOAD));
    }

    @Test
    public void givenRolesContainsLetterHolderRole_whenExecute_thenReturnTrue() {
        UserDetails details = UserDetails
            .builder()
            .roles(Arrays.asList("letter-holder", "letter-loa1"))
            .build();

        Mockito
            .when(detailsProvider.getUserDetails(ID_TOKEN))
            .thenReturn(Optional.of(details));

        assertTrue(classUnderTest.execute(context, PAYLOAD));
    }
}