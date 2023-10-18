package victor.training.ms.catalog;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;

import java.util.Collection;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@SpringBootApplication
@RequiredArgsConstructor
public class CatalogApp {
  public static void main(String[] args) {
      SpringApplication.run(CatalogApp.class, args);
  }
  private final ProductRepo productRepo;

  @EventListener(ApplicationStartedEvent.class)
  void initialData() {
    productRepo.save(new Product()
        .name("iPhone")
        .description("Hipster Phone")
        .inStock(true)
        .price(1000d));
  }

}
