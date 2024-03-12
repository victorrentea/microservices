package victor.training.ms.payment;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class GeneratePaymentUrlRest {
  private final PaymentGatewayClient paymentGatewayClient;

  @PostMapping("payment")
  public String generatePaymentUrl(@RequestParam Long orderId, @RequestParam @NotNull Double total) {
    log.info("Request payment url for orderid: {}, total: {}", orderId, total);
    return paymentGatewayClient.generatePaymentLink("order/" + orderId + "/payment-accepted", total, "modulith-app");
  }
  
}
