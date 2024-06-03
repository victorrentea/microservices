package victor.training.ms.order.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import victor.training.ms.order.client.ShippingClient;
import victor.training.ms.order.entity.Order;
import victor.training.ms.order.repo.OrderRepo;
import victor.training.ms.shared.OrderStatus;
import victor.training.ms.shared.PaymentResultEvent;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentGatewayWebHookRest { // call-back from the payment gateway to confirm the payment
  private final OrderRepo orderRepo;
  private final ShippingClient shippingClient;

  @PutMapping("order/{orderId}/paid")
  public String confirmPayment(@PathVariable long orderId, @RequestBody boolean ok) {
    onPayment(orderId, ok);
    return "Payment callback received";
  }

  private void onPayment(long orderId, boolean ok) {
    Order order = orderRepo.findById(orderId).orElseThrow();
    order.paid(ok);
    if (order.status() == OrderStatus.PAYMENT_APPROVED) {
      String trackingNumber = shippingClient.requestShipment(orderId, order.shippingAddress());
      order.scheduleForShipping(trackingNumber);
    }
    orderRepo.save(order);
    log.info("Order is now: " + order);
  }
}
