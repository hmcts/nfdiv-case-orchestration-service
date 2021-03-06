package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CREATED_DATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_CENTRE_SITEID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class SetSolicitorCourtDetailsTaskTest {

    private static final String FIXED_DATE = "2019-05-11";

    @InjectMocks
    private SetSolicitorCourtDetailsTask setSolicitorCourtDetailsTask;

    @Mock
    private CcdUtil ccdUtil;

    private Map<String, Object> testData;
    private TaskContext context;

    @Before
    public void setup() {
        when(ccdUtil.getCurrentDateCcdFormat()).thenReturn(FIXED_DATE);

        testData = new HashMap<>();
        context = new DefaultTaskContext();
    }

    @Test
    public void executeShouldSetDateAndCourtDetailsOnPayload_solicitorReleaseFeatureOff() {
        ReflectionTestUtils.setField(setSolicitorCourtDetailsTask, "featureToggleRespSolicitor", false);
        Map<String, Object> resultData = new HashMap<>();
        resultData.put(CREATED_DATE_JSON_KEY, FIXED_DATE);
        resultData.put(DIVORCE_UNIT_JSON_KEY, CourtEnum.EASTMIDLANDS.getId());
        resultData.put(DIVORCE_CENTRE_SITEID_JSON_KEY, CourtEnum.EASTMIDLANDS.getSiteId());

        assertEquals(resultData, setSolicitorCourtDetailsTask.execute(context, testData));
    }

    @Test
    public void executeShouldSetDateAndCourtDetailsOnPayload_solicitorReleaseFeatureOn() {
        ReflectionTestUtils.setField(setSolicitorCourtDetailsTask, "featureToggleRespSolicitor", true);
        Map<String, Object> resultData = new HashMap<>();
        resultData.put(CREATED_DATE_JSON_KEY, FIXED_DATE);
        resultData.put(DIVORCE_UNIT_JSON_KEY, CourtEnum.SERVICE_CENTER.getId());
        resultData.put(DIVORCE_CENTRE_SITEID_JSON_KEY, CourtEnum.SERVICE_CENTER.getSiteId());

        assertEquals(resultData, setSolicitorCourtDetailsTask.execute(context, testData));
    }
}
