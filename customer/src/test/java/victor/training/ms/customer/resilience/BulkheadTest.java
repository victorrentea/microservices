package victor.training.ms.customer.resilience;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatusCode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
public class BulkheadTest {
  @Autowired
  private TestRestTemplate rest;

  @Test
  void call_then_sleep() throws InterruptedException {
    stubFor(get("/server/call").willReturn(ok("ok").withFixedDelay(200)));

    try (ExecutorService pool = Executors.newCachedThreadPool()) {
      IntStream.range(0, 4).forEach(i -> pool.submit(() -> {
        log.info("Call #" + i);
        HttpStatusCode code = rest.getForEntity("/bulkhead", String.class).getStatusCode();
        assertThat(code.is2xxSuccessful()).isTrue();
        log.info("Call #" + i + " COMPLETED OK");
      }));
      pool.shutdown();
      pool.awaitTermination(1, TimeUnit.SECONDS);
    }

    verify(2, getRequestedFor(urlEqualTo("/server/call")));
  }

}
