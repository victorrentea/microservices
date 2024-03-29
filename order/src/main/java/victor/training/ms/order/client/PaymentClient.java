package victor.training.ms.order.client;

import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("payment")
public interface PaymentClient {
  @PostMapping("payment")
  String generatePaymentUrl(@RequestParam Long orderId, @RequestParam @NotNull Double total);
}
