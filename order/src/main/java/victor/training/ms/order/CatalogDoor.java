package victor.training.ms.order;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.Map;

@FeignClient("catalog")
public interface CatalogDoor {
  @GetMapping("catalog/prices")
  Map<Long, Double> getManyPrices(@RequestParam Collection<Long> ids);
}
