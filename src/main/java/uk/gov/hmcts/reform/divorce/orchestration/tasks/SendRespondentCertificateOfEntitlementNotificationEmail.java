package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ofPattern;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_DATE_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_CLAIM_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LIMIT_DATE_TO_CONTACT_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_OPTIONAL_TEXT_NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_OPTIONAL_TEXT_YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PERIOD_BEFORE_HEARING_DATE_TO_CONTACT_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.getRelationshipTermByGender;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DateUtils.formatDateWithCustomerFacingFormat;

@Component
@Slf4j
public class SendRespondentCertificateOfEntitlementNotificationEmail implements Task<Map<String, Object>> {

    private static final String EMAIL_DESCRIPTION = "Respondent Notification - Certificate of Entitlement";
    private static final String EMAIL_ADDRESS = "email address";

    @Autowired
    private TaskCommons taskCommons;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseDataPayload) throws TaskException {
        log.info("Executing task to notify respondent about certificate of entitlement. Case id: {}",
            (String) context.getTransientObject(CASE_ID_JSON_KEY));

        String respondentEmail = getMandatoryPropertyValueAsString(caseDataPayload, RESPONDENT_EMAIL_ADDRESS);
        String familyManCaseId = getMandatoryPropertyValueAsString(caseDataPayload, D_8_CASE_REFERENCE);
        String respondentFirstName = getMandatoryPropertyValueAsString(caseDataPayload, RESP_FIRST_NAME_CCD_FIELD);
        String respondentLastName = getMandatoryPropertyValueAsString(caseDataPayload, RESP_LAST_NAME_CCD_FIELD);
        String petitionerInferredGender = getMandatoryPropertyValueAsString(caseDataPayload,
                D_8_INFERRED_PETITIONER_GENDER);
        String petitionerRelationshipToRespondent = getRelationshipTermByGender(petitionerInferredGender);

        LocalDate dateOfHearing = LocalDate.parse(getMandatoryPropertyValueAsString(caseDataPayload, DATE_OF_HEARING_CCD_FIELD),
            ofPattern(CCD_DATE_FORMAT));
        LocalDate limitDateToContactCourt = dateOfHearing.minus(PERIOD_BEFORE_HEARING_DATE_TO_CONTACT_COURT);

        Map<String, String> templateParameters = new HashMap<>();

        templateParameters.put(EMAIL_ADDRESS, respondentEmail);
        templateParameters.put(NOTIFICATION_CASE_NUMBER_KEY, familyManCaseId);
        templateParameters.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, respondentFirstName);
        templateParameters.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, respondentLastName);
        templateParameters.put(NOTIFICATION_HUSBAND_OR_WIFE, petitionerRelationshipToRespondent);
        templateParameters.put(DATE_OF_HEARING, formatDateWithCustomerFacingFormat(dateOfHearing));
        templateParameters.put(LIMIT_DATE_TO_CONTACT_COURT,
            formatDateWithCustomerFacingFormat(limitDateToContactCourt));

        if (wasCostsClaimGranted(caseDataPayload)) {
            templateParameters.put(COSTS_CLAIM_GRANTED, NOTIFICATION_OPTIONAL_TEXT_YES_VALUE);
        } else {
            templateParameters.put(COSTS_CLAIM_GRANTED, NOTIFICATION_OPTIONAL_TEXT_NO_VALUE);
        }

        try {
            taskCommons.sendEmail(EmailTemplateNames.RESPONDENT_CERTIFICATE_OF_ENTITLEMENT_NOTIFICATION,
                EMAIL_DESCRIPTION,
                respondentEmail,
                    templateParameters);
            log.info("Respondent notification sent for case {}", (String) context.getTransientObject(CASE_ID_JSON_KEY));
        } catch (TaskException exception) {
            log.error("Failed to send Respondent notification for case {}", (String) context.getTransientObject(CASE_ID_JSON_KEY));
            throw exception;
        } catch (Exception exception) {
            log.error("Failed to send Respondent notification for case {}", (String) context.getTransientObject(CASE_ID_JSON_KEY));
            throw new TaskException(exception.getMessage(), exception);
        }

        return caseDataPayload;
    }

    private boolean wasCostsClaimGranted(Map<String, Object> payload) {
        return Optional.ofNullable(payload.get(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD))
            .map(String.class::cast)
            .map(YES_VALUE::equalsIgnoreCase)
            .orElse(false);
    }
}