package victor.training.ms.customer.resilience;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Cache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class Resilience {
  private final ServerClient server;

  @GetMapping("timeout")
  public String timeout() {
    return server.call();
  }

  @GetMapping("retry")
  @Retry(name = "retry")
  public String retry() {
    return server.call(); // RPC call: gRPC, REST, SOAP/xml, RMI, CORBA, drivere pt semafoare
  }

  @GetMapping("rate")
  @RateLimiter(name = "rate")
  public String rate() {
    return server.call();
  }

  @GetMapping("circuit")
  @Retry(name = "retry") // order = 1
  @CircuitBreaker(name = "circuit", fallbackMethod = "circuitFallback") // order=2
  @Cacheable("circuit")
  public String circuit() {
    return server.call();
  }

  public String circuitFallback() {
    return "din cache";
  }

  @GetMapping("hard")
  @Retry(name = "hard") // order = 1
  @CircuitBreaker(name = "hard") // order=2
  public String hard() {
    return server.call();
  }

  @GetMapping("bulkhead")
  @Bulkhead(name = "bulkhead")
  public String bulkhead() {
    return server.call();
  }

}
