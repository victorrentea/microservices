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

  record ProductSearchResult(long id, String name) {}

  @GetMapping("catalog/search")
  public List<ProductSearchResult> search(@RequestParam String name) { // or @RequestBody SearchCriteria
    return productRepo.searchByNameLikeIgnoreCaseAndInStockTrue(name).stream()
        .map(e -> new ProductSearchResult(e.id(), e.name()))
        .toList();
  }
}
