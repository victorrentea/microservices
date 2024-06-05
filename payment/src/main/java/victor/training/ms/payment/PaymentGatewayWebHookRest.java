package victor.training.ms.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import victor.training.ms.shared.OrderStatus;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentGatewayWebHookRest { // call-back from the payment gateway to confirm the payment
  private final OrderClient orderClient;
  @PutMapping("payment/{orderId}/paid")
  public String confirmPayment(@PathVariable long orderId, @RequestBody boolean ok) {
    orderClient.onPayment(orderId, ok);
    return "Payment callback received";
  }
}
