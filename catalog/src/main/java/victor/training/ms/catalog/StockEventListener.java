package victor.training.ms.catalog;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class StockEventListener {
  private final ProductRepo productRepo;

//  @ApplicationModuleListener
//  void onOutOfStock(OutOfStockEvent event) {
//    Product product = productRepo.findById(event.productId()).orElseThrow();
//    product.inStock(false);
//    productRepo.save(product); // not really needed due to @Transactional on @ApplicationModuleListener
//  }
//
//  @ApplicationModuleListener
//  void onBackInStock(BackInStockEvent event) {
//    Product product = productRepo.findById(event.productId()).orElseThrow();
//    product.inStock(true);
//    productRepo.save(product);
//  }
}
