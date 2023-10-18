package victor.training.ms.order;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import victor.training.ms.shared.OrderStatusChangedEvent;

import java.util.Map;

@SpringBootApplication
@EnableFeignClients
//@EnableDiscoveryClient // needed if using RestTemplate
@RequiredArgsConstructor
public class OrderApp {
  private final OrderRepo orderRepo;

  public static void main(String[] args) {
    SpringApplication.run(OrderApp.class, args);
  }

  @EventListener(ApplicationStartedEvent.class)
  @Transactional
  public void initialData() {
    Order order = orderRepo.save(new Order()
        .customerId("margareta")
        .shippingAddress("shipping address")
        .total(100d)
        .items(Map.of(1L, 2)));
    order.paid(true);
    order.scheduleForShipping("tracking#");
    order.shipped(true);
  }

  private final StreamBridge streamBridge;
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onOrderStatusChanged(OrderStatusChangedEvent event) {
    streamBridge.send("OrderStatusChangedEvent-out", event);
  }
}