package victor.training.ms.customer.resilience;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("hard")
public class HardTest {
  @Autowired
  private TestRestTemplate rest;


  @Test
  void retry() throws InterruptedException {
    stubFor(get("/server/call").willReturn(status(503)));

    callMyEndpoint();
    assertExternalApiCalledTimes(1);

    callMyEndpoint();
    assertExternalApiCalledTimes(2);

    callMyEndpoint();
    assertExternalApiCalledTimes(3);

    // in OPEN STATE
    callMyEndpoint();
    assertExternalApiCalledTimes(3);

    Thread.sleep(1001);

    // in HALF-OPEN STATE
    callMyEndpoint();
    assertExternalApiCalledTimes(4);

    // in CLOSED
    callMyEndpoint();
    assertExternalApiCalledTimes(5);

//    rest.getForEntity("/hard", String.class);
//    verify(6, getRequestedFor(urlEqualTo("/server/call")));
  }

  private void callMyEndpoint() {
    rest.getForEntity("/hard", String.class);
  }

  private void assertExternalApiCalledTimes(int count) {
    verify(count, getRequestedFor(urlEqualTo("/server/call")));
  }

}
