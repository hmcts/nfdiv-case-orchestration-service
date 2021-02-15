package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.client.CMSClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseService;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseServiceImpl implements CaseService {

    @Autowired
    private final CMSClient cmsClient;

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
        final CaseDetails caseDetails = cmsClient.getCaseFromCcd(authorizationToken);

        log.info("Successfully retrieved case with id {} and state {}", caseDetails.getCaseId(), caseDetails.getState());

        final Map<String, Object> caseData = caseDetails.getCaseData();

        return CaseDataResponse.builder()
            .caseId(caseDetails.getCaseId())
            .state(caseDetails.getState())
            .court(String.valueOf(caseData.get(D_8_DIVORCE_UNIT)))
            .data(caseData)
            .build();
    }
}
