package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.client.CMSClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseService;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseServiceImpl implements CaseService {

    @Autowired
    private final CMSClient cmsClient;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private AuthUtil authUtil;

    @Value("${ccd.jurisdictionid}")
    private String jurisdictionId;

    @Value("${ccd.casetype}")
    private String caseType;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private IdamClient idamClient;

    @Override
    public Map<String, Object> submitDraftCase(final Map<String, Object> caseData, final String authToken) {

        final Map<String, Object> payload = cmsClient.submitDraftCase(caseData, authToken);

        log.info("Case with CASE ID: {} submitted", payload.get(ID));

        return payload;
    }

    @Override
    public Map<String, Object> patchCase(final Map<String, Object> caseData, final String authToken) {

        final Map<String, Object> payload = cmsClient.patchCase(caseData, authToken);

        log.info("Updated case with CASE ID: {}", payload.get(ID));

        return payload;
    }

    @Override
    public CaseDataResponse getCase(final String authorizationToken) {
        User userDetails = getUser(authorizationToken);

        List<CaseDetails> caseDetailsList = getCaseListForUser(userDetails);

        if (CollectionUtils.isEmpty(caseDetailsList)) {
            return null;
        }

        if (caseDetailsList.size() > 1) {
            throw new DuplicateCaseException(String.format("There are [%d] cases for the user [%s]",
                caseDetailsList.size(), userDetails.getUserDetails().getId()));
        }

        CaseDetails caseDetails = caseDetailsList.get(0);

        log.info("Successfully retrieved case with id {} and state {}", caseDetails.getId(), caseDetails.getState());

        final Map<String, Object> caseData = caseDetails.getData();

        return CaseDataResponse.builder()
            .caseId(String.valueOf(caseDetails.getId()))
            .state(caseDetails.getState())
            .court(String.valueOf(caseData.get(D_8_DIVORCE_UNIT)))
            .data(caseData)
            .build();
    }

    private List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> getCaseListForUser(User user) {
        return Optional.ofNullable(
            coreCaseDataApi.searchForCitizen(
                authUtil.getBearerToken(user.getAuthToken()),
                authTokenGenerator.generate(),
                user.getUserDetails().getId(),
                jurisdictionId,
                caseType,
                Collections.emptyMap())
        ).orElse(Collections.emptyList());
    }

    private User getUser(String authorisation) {
        final String bearerToken = authUtil.getBearerToken(authorisation);
        final UserDetails userDetails = idamClient.getUserDetails(bearerToken);

        log.info("Successfully retrieved user id from Idam {}", userDetails.getId());

        return new User(bearerToken, userDetails);
    }
}
