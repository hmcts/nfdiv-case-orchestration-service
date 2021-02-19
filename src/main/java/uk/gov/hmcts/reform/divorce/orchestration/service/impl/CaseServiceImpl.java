package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.client.CMSClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.GetCaseResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.exception.CaseAlreadyExistsException;
import uk.gov.hmcts.reform.divorce.orchestration.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseService;
import uk.gov.hmcts.reform.divorce.orchestration.service.ccd.CasePatchService;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AMEND_PETITION_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseServiceImpl implements CaseService {

    @Autowired
    private final CMSClient cmsClient;

    @Autowired
    private final CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private final AuthUtil authUtil;

    @Autowired
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    private final IdamClient idamClient;

    @Autowired
    private final CasePatchService casePatchService;

    @Value("${ccd.jurisdictionid}")
    private String jurisdictionId;

    @Value("${ccd.casetype}")
    private String caseType;

    @Override
    public Map<String, Object> postCase(final Map<String, Object> caseData, final String authToken) throws CaseAlreadyExistsException {

        User user = getUser(authToken);

        if (isEmpty(findExistingCase(user))) {
            final Map<String, Object> payload = cmsClient.submitDraftCase(caseData, authToken);
            log.info("Case with case id: {} submitted", payload.get(ID));
            return payload;
        } else {
            log.trace("Existing case found for user id: {}", user.getUserDetails().getId());
            throw new CaseAlreadyExistsException("Existing case found");
        }
    }

    @Override
    public void patchCase(final String caseId, final Map<String, Object> caseData, final String authorizationToken) {
        casePatchService.patchCase(caseId, caseData, getUser(authorizationToken), authTokenGenerator.generate());
    }

    @Override
    public GetCaseResponse getCase(final String authorizationToken) throws CaseNotFoundException {
        User user = getUser(authorizationToken);

        List<CaseDetails> caseDetailsList = getCaseListForUser(user);

        log.info("Case list size {} after retrieving case for user id {}",
            caseDetailsList.size(),
            user.getUserDetails().getId()
        );

        caseDetailsList = filterOutAmendedCases(caseDetailsList);

        log.info("Case list size {} after filtering amended cases for user id {}",
            caseDetailsList.size(),
            user.getUserDetails().getId()
        );

        if (isEmpty(caseDetailsList)) {
            throw new CaseNotFoundException("No case found for user id " + user.getUserDetails().getId());
        }

        if (caseDetailsList.size() > 1) {
            throw new DuplicateCaseException(String.format("There are [%d] cases for the user [%s]",
                caseDetailsList.size(), user.getUserDetails().getId()));
        }

        CaseDetails caseDetails = caseDetailsList.get(0);

        log.info("Successfully retrieved case for user id {} with case id {} and state {}",
            user.getUserDetails().getId(),
            caseDetails.getId(),
            caseDetails.getState()
        );

        return GetCaseResponse.builder()
            .id(String.valueOf(caseDetails.getId()))
            .state(caseDetails.getState())
            .data(caseDetails.getData())
            .build();
    }

    private List<CaseDetails> getCaseListForUser(User user) {
        return Optional.ofNullable(
            coreCaseDataApi.searchForCitizen(
                user.getAuthToken(),
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

    private List<CaseDetails> filterOutAmendedCases(List<CaseDetails> caseDetailsList) {
        caseDetailsList = Optional.ofNullable(caseDetailsList)
            .orElse(Collections.emptyList())
            .stream()
            .filter(caseDetails -> !AMEND_PETITION_STATE.equals(caseDetails.getState()))
            .collect(toList());
        return caseDetailsList;
    }

    private List<CaseDetails> findExistingCase(final User user) {
        List<CaseDetails> caseDetailsList = getCaseListForUser(user);
        return filterOutAmendedCases(caseDetailsList);
    }

}
