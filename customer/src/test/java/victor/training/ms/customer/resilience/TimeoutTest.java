package victor.training.ms.customer.resilience;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatusCode;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
public class TimeoutTest {
  @Autowired
  private TestRestTemplate rest;

  @ParameterizedTest
  @ValueSource(ints = {50,100,150,200,250,280,290,295,299,300,310,350,400})
  void timeout(int serverDelay) throws InterruptedException {
    stubFor(get("/server/call").willReturn(ok("ok").withFixedDelay(serverDelay)));

    HttpStatusCode code = rest.getForEntity("/timeout", String.class).getStatusCode();
    assertThat(code.is2xxSuccessful()).isTrue();
  }

}
