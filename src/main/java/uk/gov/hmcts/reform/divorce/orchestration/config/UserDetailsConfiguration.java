package uk.gov.hmcts.reform.divorce.orchestration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.OidcUserDetailsProvider;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetailsProvider;

@Configuration
public class UserDetailsConfiguration {
    @Bean
    public UserDetailsProvider rolesProvider() {
        return new OidcUserDetailsProvider();
    }
}
