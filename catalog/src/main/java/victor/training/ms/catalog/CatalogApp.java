package victor.training.ms.catalog;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@SpringBootApplication
@RestController
@RequiredArgsConstructor
public class CatalogApp {
  public static void main(String[] args) {
      SpringApplication.run(CatalogApp.class, args);
  }
  private final ProductRepo productRepo;

  @GetMapping("catalog/prices")
  public Map<Long, Double> getManyPrices(@RequestParam List<Long> ids) {
    return productRepo.findAllById(ids).stream()
        .collect(Collectors.toMap(Product::id, Product::price));
  }

  @EventListener(ApplicationStartedEvent.class)
  public void atStartup() {
    productRepo.save(new Product()
        .name("iPhone")
        .description("Hipster Phone")
        .inStock(true)
        .price(1000d));
  }
}
