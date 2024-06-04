package victor.training.ms.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties.Restclient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import victor.training.ms.shared.OrderStatus;
import victor.training.ms.shared.OrderStatusChangedEvent;

import java.util.function.Consumer;

import static victor.training.ms.notification.CustomerClient.*;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
@EnableFeignClients
@RestController
public class NotificationApp {
  public static void main(String[] args) {
      SpringApplication.run(NotificationApp.class, args);
  }
  private final CustomerClient customerClient;

  @Bean
  @LoadBalanced
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public Consumer<OrderStatusChangedEvent> onOrderStatusChanged() {
    return event -> {
      CustomerDto customer = customerClient.getCustomer(event.customerId());
      if (event.status() == OrderStatus.PAYMENT_APPROVED) {

        String body = "Sending 📧 'Order %s Confirmed' email to %s".formatted(event.orderId(), customer.email());
        sendEmail(body);
      }
      if (event.status() == OrderStatus.SHIPPING_IN_PROGRESS) {
        String body = "Sending 📧 'Order %s Shipped' email to %s".formatted(event.orderId(), customer.email());
        sendEmail(body);
      }
    };
  }

//  @RateLimited() // max 10/s
  private void sendEmail(String body) {
    log.info(body);
  }

}
