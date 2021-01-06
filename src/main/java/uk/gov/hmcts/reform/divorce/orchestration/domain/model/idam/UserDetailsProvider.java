package uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam;

import java.util.Optional;

public interface UserDetailsProvider {

    Optional<UserDetails> getUserDetails(String token);

}
