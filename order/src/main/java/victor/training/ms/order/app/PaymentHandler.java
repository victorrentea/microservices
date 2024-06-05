package victor.training.ms.order.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import victor.training.ms.order.client.ShippingClient;
import victor.training.ms.order.entity.Order;
import victor.training.ms.order.repo.OrderRepo;
import victor.training.ms.shared.OrderStatus;

@RestController
@Slf4j
@RequiredArgsConstructor
public class PaymentHandler {
  private final OrderRepo orderRepo;
  private final ShippingClient shippingClient;

  @PutMapping("order/{orderId}/paid")
  public void onPayment(@PathVariable long orderId, @RequestBody boolean ok) {
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
