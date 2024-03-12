package victor.training.ms.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepo extends JpaRepository<Product, Long> {
  List<Product> searchByNameLikeIgnoreCase(String namePart);
//  List<Product> searchByNameLikeIgnoreCaseAndInStockTrue(String namePart);
}
