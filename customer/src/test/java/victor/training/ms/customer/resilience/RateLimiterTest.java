package victor.training.ms.customer.resilience;

import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.ResponseEntity;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
public class RateLimiterTest {
  @Autowired
  private TestRestTemplate testRestTemplate;

  @RepeatedTest(50)
  void call_then_sleep() throws InterruptedException {
    stubFor(get("/server/call").willReturn(ok("ok")));

    ResponseEntity<String> response = testRestTemplate.getForEntity("/rate", String.class);

    Thread.sleep(100);
    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
  }

}
