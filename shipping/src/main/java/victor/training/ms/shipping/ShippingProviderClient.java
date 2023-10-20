package victor.training.ms.shipping;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "shipping-provider")
public interface ShippingProviderClient {
  @PostMapping("create-shipping")
  String requestShipment(@RequestParam String pickupAddress,
                         @RequestParam String deliveryAddress);
}
