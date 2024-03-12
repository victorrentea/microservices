package victor.training.ms.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.web.bind.annotation.*;
import victor.training.ms.shared.PaymentResultEvent;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentGatewayWebHookRest { // call-back from the payment gateway to confirm the payment
  private final StreamBridge streamBridge;

  @PutMapping("payment/{orderId}/status")
  public String confirmPayment(@PathVariable long orderId, @RequestBody boolean ok) {
//    orderClient.confirmPayment(orderId, ok);
    PaymentResultEvent event = new PaymentResultEvent(orderId, ok);
    streamBridge.send("PaymentResultEvent-out", event);
    log.info("Sent event {}", event);
    return "Payment callback received";
  }
}
