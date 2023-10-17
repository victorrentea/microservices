package victor.training.ms.customer.resilience;

import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.ResponseEntity;
import victor.training.ms.customer.resilience.RetryTest.StubResponse.OKDelayed;

import java.util.Objects;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static victor.training.ms.customer.resilience.RetryTest.StubResponse.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
public class RetryTest {
  @Autowired
  private TestRestTemplate testRestTemplate;


  public static Stream<TestData> retryTestData() {
    return Stream.of(
        new TestData(200, new OK()),
        new TestData(500, new OKDelayed(1000)),
        new TestData(200, new KO(503), new OK()),
        new TestData(200, new KO(503), new KO(503), new OK()),
        new TestData(500, new KO(503)),
        new TestData(200, new KODelayed(503, 100), new OK()),
        new TestData(200, new KODelayed(503, 500), new OK())
    );
  }

  record TestData(int expectedStatus, StubResponse... responses) {
    public String toString() {
      return expectedStatus + " <- " + Stream.of(responses).map(Objects::toString).collect(joining(","));
    }
  }

  sealed interface StubResponse {
    default int millis() {
      return 0;
    }

    default int status() {
      return 200;
    }

    record OK() implements StubResponse {
      public String toString() {
        return "OK";
      }
    }

    record OKDelayed(int millis) implements StubResponse {
      public String toString() {
        return "OK[Δt=" + millis + "ms]";
      }
    }

    record KO(int status) implements StubResponse {
      public String toString() {
        return "KO[" + status + ']';
      }
    }

    record KODelayed(int status, int millis) implements StubResponse {
      public String toString() {
        return "KO[" + status + ",Δt=" + millis + "ms]";
      }
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("retryTestData")
  void retry(TestData testData) {
    setupWiremock(testData);

    ResponseEntity<String> response = testRestTemplate.getForEntity("/retry", String.class);

    assertThat(response.getStatusCode().value()).isEqualTo(testData.expectedStatus);
  }
  
  private void setupWiremock(TestData testData) {
    reset();
    String lastScenarioState = Scenario.STARTED;
    for (StubResponse response : testData.responses) {
      String nextScenario = response == testData.responses[testData.responses.length - 1] ?
          lastScenarioState : (lastScenarioState + "x");

      stubFor(get("/server/call")
          .inScenario("MY")
          .whenScenarioStateIs(lastScenarioState)
          .willReturn(status(response.status()).withFixedDelay(response.millis()))
          .willSetStateTo(nextScenario))
      ;
      lastScenarioState = nextScenario;
    }
  }
}
