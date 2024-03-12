package victor.training.ms.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("shipping")
public interface ShippingClient {
  @PostMapping("shipping")
  String  requestShipment(@RequestParam long orderId, @RequestParam String customerAddress);
}
