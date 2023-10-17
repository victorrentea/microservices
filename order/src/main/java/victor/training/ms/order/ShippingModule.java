package victor.training.ms.order;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("shipping")
public interface ShippingModule {
  @PostMapping("shipping")
  String requestShipment(@RequestParam String customerAddress);
}
