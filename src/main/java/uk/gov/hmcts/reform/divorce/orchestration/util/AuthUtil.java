package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.client.IdamClient;

@SuppressWarnings("squid:S1118")
@Component
@Slf4j
public class AuthUtil {

    private static final String BEARER = "Bearer ";

    @Value("${idam.client.redirect_uri}")
    private String authRedirectUrl;

    @Value("${idam.client.id}")
    private String authClientId;

    @Value("${idam.client.secret}")
    private String authClientSecret;

    @Value("${idam.citizen.username}")
    private String citizenUserName;

    @Value("${idam.citizen.password}")
    private String citizenPassword;

    @Value("${idam.caseworker.username}")
    private String caseworkerUserName;

    @Value("${idam.caseworker.password}")
    private String caseworkerPassword;

    private final IdamClient idamClient;

    @Autowired
    public AuthUtil(IdamClient idamClient) {
        this.idamClient = idamClient;
    }

    public String getCitizenToken() {
        return getIdamOauth2Token(citizenUserName, citizenPassword);
    }

    public String getCaseworkerToken() {
        return getIdamOauth2Token(caseworkerUserName, caseworkerPassword);
    }

    private String getIdamOauth2Token(String username, String password) {
        log.info(citizenUserName);
        log.info(citizenPassword);
        log.info(authClientSecret);
        log.info(authRedirectUrl);
        log.info(authClientId);
        return idamClient.getAccessTokenResponse(username, password).accessToken;
    }

    public String getBearerToken(String token) {
        if (StringUtils.isBlank(token)) {
            return token;
        }

        return token.startsWith(BEARER) ? token : BEARER.concat(token);
    }

}
