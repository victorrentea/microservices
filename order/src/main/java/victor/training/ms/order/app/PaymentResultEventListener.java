package victor.training.ms.order.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import victor.training.ms.order.client.ShippingClient;
import victor.training.ms.order.entity.Order;
import victor.training.ms.order.repo.OrderRepo;
import victor.training.ms.shared.OrderStatus;
import victor.training.ms.shared.PaymentResultEvent;

import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentResultEventListener {
  private final OrderRepo orderRepo;
  private final ShippingClient shippingClient;

  @Bean
  public Consumer<PaymentResultEvent> onPaymentResultEvent() {
    return event -> {
      log.info("Received: {}", event);
      Order order = orderRepo.findById(event.orderId()).orElseThrow();
      order.paid(event.ok());
      if (order.status() == OrderStatus.PAYMENT_APPROVED) {
        String trackingNumber = shippingClient.requestShipment(event.orderId(), order.shippingAddress());
        order.scheduleForShipping(trackingNumber);
      }
      orderRepo.save(order);
      log.info("Order is now: " + order);
    };
  }
}

