package uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;

import java.text.ParseException;
import java.util.Optional;

/**
 * Extracts the user details from an OIDC ID token.
 */
public class OidcUserDetailsProvider implements UserDetailsProvider {

    @Override
    public Optional<UserDetails> getUserDetails(String token) {
        try {
            final JWTClaimsSet jwt = JWTParser.parse(token).getJWTClaimsSet();
            final UserDetails details = new UserDetails(
                jwt.getStringClaim("uid"),
                jwt.getSubject(),
                jwt.getStringClaim("given_name"),
                jwt.getStringClaim("family_name"),
                token,
                jwt.getStringListClaim("roles")
            );

            return Optional.of(details);
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

}
