package uk.gov.hmcts.reform.divorce.orchestration.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmailClientTest {

    @Value("${uk.gov.notify.api.key}")
    private String apiKey;

    @Value("${uk.gov.notify.api.baseUrl}")
    private String baseUrl;

    @Autowired
    private EmailClient emailClient;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @Test
    public void shouldHaveTheCorrectProperties() {

        assertEquals(apiKey, emailClient.getApiKey());
        assertEquals(baseUrl, emailClient.getBaseUrl());
    }
}
