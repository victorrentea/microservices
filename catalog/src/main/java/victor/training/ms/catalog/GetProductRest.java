package victor.training.ms.catalog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GetProductRest {
  private final ProductRepo productRepo;
//  private final StockDoor stockDoor;

  public record GetProductResponse(long id,
                                   String name,
                                   String description,
//                            int stock, // TODO
                                   double price
                                    , boolean inStock) {}

  @GetMapping("catalog/{productId}")
  public GetProductResponse getProduct(@PathVariable long productId) {
    log.info("Getting product {}", productId);
    Product product = productRepo.findById(productId).orElseThrow();
//    int stock = stockDoor.getStock(productId);
    return new GetProductResponse(
        product.id(),
        product.name(),
        product.description(),
        /*stock,*/
        product.price(),
        product.inStock());
  }


}
