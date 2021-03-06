package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.utils.UserDetailsProvider;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID_TOKEN_JSON_KEY;


@Component
@RequiredArgsConstructor
public class AuthenticateRespondentTask implements Task<Boolean> {

    @Autowired
    private UserDetailsProvider userDetailsProvider;

    @Override
    public Boolean execute(TaskContext context, Boolean payload) {
        return userDetailsProvider
            .getUserDetails(context.getTransientObject(ID_TOKEN_JSON_KEY).toString())
            .map(details -> isRespondentUser(details))
            .orElse(false);
    }

    private boolean isRespondentUser(UserDetails userDetails) {
        return userDetails != null
            && CollectionUtils.isNotEmpty(userDetails.getRoles())
            && userDetails.getRoles()
            .stream()
            .anyMatch(this::isLetterHolderRole);
    }

    private boolean isLetterHolderRole(String role) {
        return StringUtils.isNotBlank(role)
            && role.startsWith("letter")
            && !role.endsWith("loa1");
    }
}
