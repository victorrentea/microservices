package victor.training.ms.order.client;


import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import victor.training.ms.order.entity.LineItem;

import java.util.List;

@FeignClient(value = "inventory", configuration = InventoryClient.Config.class)
public interface InventoryClient {

  @PostMapping("inventory/stock/reserve")
  void reserveStock(@RequestParam long orderId, @RequestBody List<LineItem> items);

  class Config {
    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
      return new BasicAuthRequestInterceptor("admin", "admin");
    }
  }
}
