package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CaseOrchestrationValuesTest {

    @Autowired
    private CaseOrchestrationValues caseOrchestrationValues;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @Test
    public void shouldLoadDefaultValues() {
        assertThat(caseOrchestrationValues.getAosOverdueGracePeriod(), is("0"));
    }

}