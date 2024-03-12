package victor.training.ms.order.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import victor.training.ms.order.entity.Order;

public interface OrderRepo extends JpaRepository<Order, Long> {
}
