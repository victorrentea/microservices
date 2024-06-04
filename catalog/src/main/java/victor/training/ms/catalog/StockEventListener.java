package victor.training.ms.catalog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import victor.training.ms.shared.OutOfStockEvent;

import java.util.function.Consumer;


@Slf4j
@RequiredArgsConstructor
@Service
public class StockEventListener {
  private final ProductRepo productRepo;

  @Bean
  public Consumer<OutOfStockEvent> onOutOfStock() {
    return event -> {
      log.info("out of stock listener");
      Product product = productRepo.findById(event.productId()).orElseThrow();
      product.inStock(false);
      productRepo.save(product);
    };
  }
}
