package victor.training.ms.shipping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
  

}
