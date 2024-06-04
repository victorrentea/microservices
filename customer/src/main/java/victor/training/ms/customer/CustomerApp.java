package victor.training.ms.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@EnableFeignClients
@RestController
@SpringBootApplication
@RequiredArgsConstructor
public class CustomerApp {
  public static void main(String[] args) {
      SpringApplication.run(CustomerApp.class, args);
  }
  private final CustomerRepo customerRepo;

  public record CustomerDto(String id, String email, String address) {}

  @EventListener(ApplicationStartedEvent.class)
  void initialData() {
    customerRepo.save(new Customer(
        "margareta",
        "Margareta",
        "Bucharest",
        "margareta@example.com"));
  }

  @GetMapping("customer/{customerId}")
  public CustomerDto getCustomer(@PathVariable String customerId) {
    log.info("Get customer {}", customerId);
    Customer customer = customerRepo.findById(customerId).orElseThrow();
    return new CustomerDto(customer.id(), customer.email(), customer.address());
  }
}
