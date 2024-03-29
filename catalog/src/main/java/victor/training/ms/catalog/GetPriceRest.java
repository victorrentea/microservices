package victor.training.ms.catalog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GetPriceRest {
  private final ProductRepo productRepo;

  @GetMapping("catalog/{productId}/price")
  public double getPrice(@RequestParam long productId) {
    return productRepo.findById(productId).orElseThrow().price();
  }


    @GetMapping("catalog/prices/many")
  // http://localhost:port/catalog/prices/many?ids=1,2,100
    public Map<Long, Double> getManyPrices(@RequestParam Collection<Long> ids) {
      log.info("Returing prices for products: {}", ids);
      return productRepo.findAllById(ids).stream()
          .collect(toMap(Product::id, Product::price));
    }

}
