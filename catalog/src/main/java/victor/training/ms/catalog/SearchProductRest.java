package victor.training.ms.catalog;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SearchProductRest {
  private final ProductRepo productRepo;

  public record ProductSearchResult(long id, String name, int stock) {}

  @GetMapping("catalog/search")
  public List<ProductSearchResult> search(@RequestParam String name) {
    // SELECT PRODUS FROM .. WHERE NAME LIKE %name% AND STOCK > 0
    // TODO only return items in stock
    return productRepo.searchByNameLikeIgnoreCaseAndInStockTrue(name).stream()
        .map(e -> new ProductSearchResult(e.id(), e.name(), 0))
        .toList();
  }
}
