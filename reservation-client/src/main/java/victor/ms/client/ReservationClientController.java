package victor.ms.client;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ReservationClientController {
  private static final AtomicInteger fragileAtomic = new AtomicInteger();
  private static final AtomicInteger retryAtomic = new AtomicInteger();
  private final RestTemplate rest;
  private final StreamBridge streamBridge;
  private final ReservationFeignClient feignClient;

  public List<String> fallbackResponse(Exception e) {
    log.error("Recovered from error: " + e);
    return asList("Basescu");
  }

  @GetMapping("reservations")
  @CircuitBreaker(name = "get-reservations", fallbackMethod = "fallbackResponse")
  public List<String> getReservationNames() {
    log.info("Get");
    ResponseEntity<List<ReservationDto>> entity = rest.exchange("http://reservation-service/reservations",
        HttpMethod.GET, null, new ParameterizedTypeReference<>() {
        });

    return entity.getBody().stream()
        .map(ReservationDto::getReservationName)
        .collect(toList());
  }

  @GetMapping("fragile")
  @Bulkhead(name = "fragile")
  public String fragile() {
    int id = fragileAtomic.incrementAndGet();
    log.info("Sending id:" + id);
    String data = rest.getForObject("http://reservation-service/fragile", String.class);
    log.info("Received id:" + id);
    return data;
  }

  @GetMapping("rate-limited")
  @RateLimiter(name = "rate-limited", fallbackMethod = "rateLimitReached")
  public String rateLimited() {
    log.info("Sending");
    return rest.getForObject("http://reservation-service/rate-limited", String.class);
  }

  public String rateLimitReached(Exception e) {
    return "Rate Limit Reached: " + e;
  }

  @GetMapping("flaky")
  @Retry(name = "flaky")
  public String flaky() {
    log.info("Request #" + retryAtomic.incrementAndGet());
    try {
//      return feignClient.flaky();
      return rest.getForObject("http://reservation-service/flaky", String.class);
    } catch (RestClientException e) {
      log.error("Thrown: " + e);
      throw e;
    }
  }

  @GetMapping("flaky-slow")
  @Retry(name = "flaky-slow")
  public String flakySlow() {
    log.info("Request #" + retryAtomic.incrementAndGet());
    try {
      return rest.getForObject("http://reservation-service/flaky-slow", String.class);
//      return feignClient.flakySlow();
    } catch (RestClientException e) {
      log.error("Thrown: " + e);
      throw e;
    }
  }

  @PostMapping("reservation")
  public void createReservation(@RequestBody ReservationDto reservation) {
    streamBridge.send("createReservation", reservation.getReservationName());
    log.info("Message Sent");
  }
}
