package victor.training.ms.order.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import victor.training.ms.order.entity.Order;
import victor.training.ms.order.repo.OrderRepo;
import victor.training.ms.shared.ShippingResultEvent;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Service
public class ShippingResultEventListener {
  private final OrderRepo orderRepo;

  @Bean
  public Consumer<ShippingResultEvent> onShippingResultEvent() {
    return event -> {
      log.info("Received: {}", event);
      long orderId = event.orderId(); // acts as CORRELATION ID
      Order order = orderRepo.findById(orderId).orElseThrow();
      order.shipped(event.ok());
      orderRepo.save(order);
      log.info("Saved order: " + order);
    };
  }
}
