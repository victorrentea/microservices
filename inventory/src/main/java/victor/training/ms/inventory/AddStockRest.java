package victor.training.ms.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AddStockRest {
  private final StockService stockService;

  @PostMapping("stock/{productId}/add/{items}")
  public void addStock(@PathVariable long productId, @PathVariable int items) {
    stockService.addStock(productId, items);
  }
}
