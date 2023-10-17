package victor.training.ms.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GetStockRest {
  private final StockRepo stockRepo;

  @GetMapping("stock/{productId}")
  @Transactional
  public Integer addStock(@PathVariable long productId) {
    return stockRepo.findByProductId(productId).map(Stock::items).orElse(0);
  }
}
