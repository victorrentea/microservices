package victor.training.ms.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import victor.training.ms.shared.OrderStatus;
import victor.training.ms.shared.OrderStatusChangedEvent;

import java.util.function.Consumer;

import static victor.training.ms.notification.CustomerClient.*;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
@EnableFeignClients
public class NotificationApp {
  public static void main(String[] args) {
      SpringApplication.run(NotificationApp.class, args);
  }
  private final CustomerClient customerClient;

  @Bean
  public Consumer<OrderStatusChangedEvent> onOrderStatusChanged() {
    return event -> {
      CustomerDto customer = customerClient.getCustomer(event.customerId());
      if (event.status() == OrderStatus.PAYMENT_APPROVED) {
        log.info("Sending 📧 'Order {} Confirmed' email to {}", event.orderId(), customer.email());
      }
      if (event.status() == OrderStatus.SHIPPING_IN_PROGRESS) {
        log.info("Sending 📧 'Order {} Shipped' email to {}", event.orderId(), customer.email());
      }
    };
  }

}
