package victor.training.ms.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;
import victor.training.ms.shared.PaymentResultEvent;

@RestController
@RequiredArgsConstructor
public class PaymentGatewayCallbackRest {
  private final ApplicationEventPublisher eventPublisher;
  private final StreamBridge streamBridge;

  @PutMapping("payment/{orderId}/status")
  public String confirmPayment(@PathVariable long orderId, @RequestBody boolean ok) {
//    eventPublisher.publishEvent(new PaymentResultEvent(orderId, ok));
    streamBridge.send("PaymentResultEvent-out", new PaymentResultEvent(orderId, ok));
    return "Payment callback received";
  }
}
