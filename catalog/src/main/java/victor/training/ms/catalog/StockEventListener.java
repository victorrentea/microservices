package victor.training.ms.catalog;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class StockEventListener {
  private final ProductRepo productRepo;

  // ce-ar fi daca as vrea sa arat la search cate items am / produs
// OptB: propagi stockul din inventory catre catalog prin Eventuri

  // onStockChangedEvent(StockChangedEvent) {
  //     1) Notification: StockChangedEvent{productId} ma obliga sa GET stock => :( presiune pe inventory
  //     2) Stateful "Fat" Event: StockChangedEvent{productId, int newStock [,int previousStock]} => :(
  //          HOT SALE: + listener does not have to persist so much data anymore
  //     <> AggregateEvent: ManyProductsSoldEvent{ sale: [{productId:, itemsSoldOverTheLasyHour: 10}, ..]}
  // }


   // TEMA: actualizeaza product.inStock pe baza unui event trimis prin rabbit de inventory microservice
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
