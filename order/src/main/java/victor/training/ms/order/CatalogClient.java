package victor.training.ms.order;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.Map;

@FeignClient("catalog")
public interface CatalogClient {
  @GetMapping("catalog/prices/many")
  Map<Long, Double> getManyPrices(@RequestParam Collection<Long> ids);
}

//  @GetMapping("catalog/prices/many") // http://localhost:port/catalog/prices/many?ids=1,2,100
//  public Map<Long, Double> getManyPrices(@RequestParam Collection<Long> ids)