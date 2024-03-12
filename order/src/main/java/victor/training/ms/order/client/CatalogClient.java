package victor.training.ms.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.Map;

@FeignClient("catalog")
public interface CatalogClient {
  @GetMapping("catalog/{productId}/price")
  double getPrice(@RequestParam long productId);

//  @GetMapping("catalog/prices/many")
//  Map<Long, Double> getManyPrices(@RequestParam Collection<Long> ids);
}