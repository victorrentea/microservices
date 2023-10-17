package victor.training.ms.notification;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("customer")
public interface CustomerClient {
  record CustomerDto(String id, String email, String address) {
  }

  @GetMapping("customer/{customerId}")
  CustomerDto getCustomer(@PathVariable String customerId);
}
