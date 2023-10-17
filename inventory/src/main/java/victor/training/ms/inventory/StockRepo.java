package victor.training.ms.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

public interface StockRepo extends JpaRepository<Stock, Long> {
  @Lock(PESSIMISTIC_WRITE) // todo test
  Optional<Stock> findByProductId(long productId);
}
