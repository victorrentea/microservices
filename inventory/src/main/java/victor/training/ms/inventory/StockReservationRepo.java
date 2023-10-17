package victor.training.ms.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface StockReservationRepo extends JpaRepository<StockReservation, Long> {
  @Transactional
  void deleteAllByOrderId(long orderId);
}
