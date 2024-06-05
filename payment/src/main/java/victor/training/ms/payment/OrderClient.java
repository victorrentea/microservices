package victor.training.ms.payment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("order")
public interface OrderClient {
  @PutMapping("order/{orderId}/paid")
  public void onPayment(@PathVariable long orderId, @RequestBody boolean ok);
}
