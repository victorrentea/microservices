package victor.training.ms.order;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("inventory")
public interface InventoryDoor {
   @PostMapping("inventory/stock/reserve")
   void reserveStock(@RequestParam long orderId, @RequestBody List<LineItem> items);
}
