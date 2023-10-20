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
  private TestRestTemplate rest;

  public static Stream<TestCase> retryTestData() {
    return Stream.of(
        new TestCase(200, 1, new OK()),
        new TestCase(200, 1, new OKDelayed(200)),
        new TestCase(200, 1, new OKDelayed(290)),
        new TestCase(500, 3, new OKDelayed(400)),
        new TestCase(200, 2, new KO(503), new OK()),
        new TestCase(200, 3, new KO(503), new KO(503), new OK()),
        new TestCase(500, 3, new KO(503)),
        new TestCase(200, 2, new KODelayed(503, 100), new OK()),
        new TestCase(200, 2, new KODelayed(503, 400), new OK())
    );
  }

  record TestCase(int expectedStatus, int expectedApiCalls, StubResponse... responses) {
    public String toString() {
      String responseStr = Stream.of(responses).map(Objects::toString).collect(joining(", "));
      return "Returns " + expectedStatus + " after " + expectedApiCalls + " calls to API: " + responseStr + "...";
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
        return "OK[after " + millis + "ms]";
      }
    }

    record KO(int status) implements StubResponse {
      public String toString() {
        return "KO[" + status + ']';
      }
    }

    record KODelayed(int status, int millis) implements StubResponse {
      public String toString() {
        return "KO[" + status + " after " + millis + "ms]";
      }
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("retryTestData")
  void retryAndTimeout(TestCase testCase) {
    setupWiremock(testCase.responses);

    ResponseEntity<String> response = rest.getForEntity("/retry", String.class);

    assertThat(response.getStatusCode().value()).isEqualTo(testCase.expectedStatus);
    verify(testCase.expectedApiCalls, getRequestedFor(urlEqualTo("/server/call")));
  }
  
  private void setupWiremock(StubResponse[] responses) {
    reset();
    String lastScenarioState = Scenario.STARTED;
    for (StubResponse response : responses) {
      String nextScenario = response == responses[responses.length - 1] ?
          lastScenarioState : (lastScenarioState + "x");

      stubFor(get("/server/call")
          .inScenario("MY")
          .whenScenarioStateIs(lastScenarioState)
          .willReturn(status(response.status())
              .withFixedDelay(response.millis()))
          .willSetStateTo(nextScenario))
      ;
      lastScenarioState = nextScenario;
    }
  }
}
