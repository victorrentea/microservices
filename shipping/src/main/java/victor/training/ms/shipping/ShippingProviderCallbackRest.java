package victor.training.ms.shipping;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;
import victor.training.ms.shared.ShippingResultEvent;

@RestController
@RequiredArgsConstructor
public class ShippingProviderCallbackRest {
  private final ApplicationEventPublisher publisher;
  private final StreamBridge streamBridge;

  @PutMapping("shipping/{orderId}/status")
  public String shippedStatus(@PathVariable long orderId, @RequestBody boolean ok) {
//    publisher.publishEvent(new ShippingResultEvent(orderId, ok));
    streamBridge.send("ShippingResultEvent-out", new ShippingResultEvent(orderId, ok));
    return "Shipping callback received";
  }
}
