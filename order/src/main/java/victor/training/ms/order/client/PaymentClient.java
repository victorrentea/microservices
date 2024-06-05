package victor.training.ms.order.client;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.annotation.TimedSet;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("payment")
public interface PaymentClient {
  @Timed
  @GetMapping("payment/{orderId}/{total}")
  public String generatePaymentUrl(@PathVariable long orderId,
                                   @PathVariable double total);
}
