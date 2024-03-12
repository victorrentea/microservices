package victor.training.ms.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.web.bind.annotation.*;
import victor.training.ms.shared.PaymentResultEvent;

@RestController
@RequiredArgsConstructor
public class PaymentGatewayWebHookRest {
  private final StreamBridge streamBridge;

  @PutMapping("payment/{orderId}/status")
  public String confirmPayment(@PathVariable long orderId, @RequestBody boolean ok) {
    streamBridge.send("PaymentResultEvent-out", new PaymentResultEvent(orderId, ok));
    return "Payment callback received";
  }
}
