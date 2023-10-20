package victor.training.ms.shipping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@SpringBootApplication
@EnableFeignClients
@RequiredArgsConstructor
@RestController
public class ShippingApp {
  public static void main(String[] args) {
      SpringApplication.run(ShippingApp.class, args);
  }
  private final ShippingProviderClient shippingProviderClient;

  @PostMapping("shipping")
  public String requestShipment(@RequestParam String customerAddress) {
    log.info("Request shipping at " + customerAddress);
    return shippingProviderClient.requestShipment("our-warehouse", customerAddress);
  }

  // functional-style spring message endpoint
  @Bean/// un reply-message s-ar chema TrackingNumberReply
  public Function<RequestShipment, ShippingAcceptedEvent> requestShipmentListener() {
    return event -> {
      log.info("Request shipping via q at " + event.customerAddress());
      try {
        String trackingNumber = shippingProviderClient.requestShipment(
            "our-warehouse", event.customerAddress());
        System.out.println("Got tracking number: " + trackingNumber);
        return new ShippingAcceptedEvent(event.orderId(), trackingNumber);
      } catch (Exception e) {
        log.error("Shipping failed: " + e, e);
        return new ShippingAcceptedEvent(event.orderId, null);
      }
    };
  }

  record RequestShipment(long orderId, String customerAddress) {}

  record ShippingAcceptedEvent(long orderId, String trackingNumber) {}


}
