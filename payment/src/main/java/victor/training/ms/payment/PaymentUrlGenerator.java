package victor.training.ms.payment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentUrlGenerator {

  @GetMapping("payment/{orderId}/{total}")
  public String generatePaymentUrl(@PathVariable long orderId,
                                   @PathVariable double total){
    if (total <= 0) {
      throw new IllegalArgumentException("Total must be positive");
    }
    log.info("Request payment url for orderid: {}, total: {}", orderId, total);
    return paymentGatewayClient.generatePaymentLink("order/" + orderId + "/payment-accepted", total, "modulith-app");
  }

  private final PaymentGatewayClient paymentGatewayClient;

}
