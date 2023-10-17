package victor.training.ms.payment;

import jakarta.validation.constraints.NotNull;
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
@EnableFeignClients
@SpringBootApplication
@RestController
@RequiredArgsConstructor
public class PaymentApp {
  public static void main(String[] args) {
      SpringApplication.run(PaymentApp.class, args);
  }
  private final PaymentGatewayClient paymentGatewayClient;

  @PostMapping("payment")
  public String generatePaymentUrl(@RequestParam Long orderId, @RequestParam @NotNull Double total) {
    log.info("Request payment url for orderid: " + orderId);
    return paymentGatewayClient.generatePaymentLink("order/" + orderId + "/payment-accepted", total, "modulith-app");
  }

}
