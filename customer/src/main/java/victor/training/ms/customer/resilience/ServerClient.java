package victor.training.ms.customer.resilience;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "server", url = "http://localhost:${wiremock.server.port}")
public interface ServerClient {
  @GetMapping("server/call")
  String call();
}
