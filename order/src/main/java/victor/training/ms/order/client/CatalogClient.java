package victor.training.ms.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@FeignClient("catalog") // ~ Spring Data peste Rest
// RestTemplate, WebClient, HttpClient, clienti generati de OpenAPI?
public interface CatalogClient {
  // http://catalog/1/price foloseste Eureka pentru a adresa req unei instante de catalog
  @GetMapping("catalog/{productId}/price")
  double getPrice(@RequestParam long productId);


  @GetMapping("catalog/prices/many")
  Map<Long, Double> getManyPrices(@RequestParam Collection<Long> ids);
}