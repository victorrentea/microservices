package victor.training.ms.customer.resilience;

import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "server", url = "http://localhost:${wiremock.server.port:9999}")
public interface ServerClient {
  @GetMapping("server/call")
//  @Timed // expose call duration metric on /actuator/prometheus
//  @Observed
  String call(); // call duration is automatically monitored by HttpClient used by @FeignClient
}
