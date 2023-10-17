package victor.training.ms.customer.resilience;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class Resilience {
  private final ServerClient server;

  @GetMapping("retry")
  @Retry(name = "retry")
  public String retry() {
    return server.call();
  }

  @GetMapping("rate")
  @RateLimiter(name = "rate")
  public String rate() {
    return server.call();
  }

  @GetMapping("bulkhead")
  @Bulkhead(name = "bulkhead")
  public String bulkhead() {
    return server.call();
  }

}
