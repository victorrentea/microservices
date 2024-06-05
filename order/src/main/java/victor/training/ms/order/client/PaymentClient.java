package victor.training.ms.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("payment")
public interface PaymentClient {
  @GetMapping("payment/{orderId}/{total}")
  public String generatePaymentUrl(@PathVariable long orderId,
                                   @PathVariable double total);
}
