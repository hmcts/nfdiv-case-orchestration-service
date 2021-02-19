package uk.gov.hmcts.reform.divorce.orchestration.service.ccd;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CasePatchService {

    private static final String DIVORCE_CASE_PATCH_EVENT_SUMMARY = "Divorce case patch event";
    private static final String DIVORCE_CASE_PATCH_EVENT_DESCRIPTION = "Patching Divorce Case";

    @Value("${ccd.jurisdictionid}")
    private String jurisdictionId;

    @Value("${ccd.casetype}")
    private String caseType;

    @Value("${ccd.eventid.patch}")
    private String patchEventId;

    @Autowired
    private final CoreCaseDataApi coreCaseDataApi;

    public void patchCase(final String caseId,
                          final Map<String, Object> caseData,
                          final User user,
                          final String serviceAuthToken) {

        final String authToken = user.getAuthToken();
        final String userId = user.getUserDetails().getId();

        log.debug("Attempting to start patch case event for User:{}, Case:{}", userId, caseId);

        final StartEventResponse startEventResponse = coreCaseDataApi.startEventForCitizen(
            authToken,
            serviceAuthToken,
            userId,
            jurisdictionId,
            caseType,
            caseId,
            patchEventId
        );

        log.info("Patch case event started for User:{}, Case:{}", userId, caseId);

        final CaseDataContent caseDataContent = buildCaseDataContentWith(
            caseData,
            startEventResponse
        );

        log.debug("Attempting to submit patch case event for User:{}, Case:{}", userId, caseId);

        coreCaseDataApi.submitEventForCitizen(
            authToken,
            serviceAuthToken,
            userId,
            jurisdictionId,
            caseType,
            caseId,
            true,
            caseDataContent
        );

        log.info("Patch case event submitted for User:{}, Case:{}", userId, caseId);
    }

    private CaseDataContent buildCaseDataContentWith(final Map<String, Object> caseData,
                                                     final StartEventResponse startEventResponse) {
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(CasePatchService.DIVORCE_CASE_PATCH_EVENT_SUMMARY)
                    .description(CasePatchService.DIVORCE_CASE_PATCH_EVENT_DESCRIPTION)
                    .build()
            ).data(caseData)
            .build();
    }
}
