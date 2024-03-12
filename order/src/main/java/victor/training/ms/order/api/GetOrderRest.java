package victor.training.ms.order.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import victor.training.ms.order.entity.Order;
import victor.training.ms.order.repo.OrderRepo;

@RestController
@RequiredArgsConstructor
public class GetOrderRest {
  private final OrderRepo orderRepo;
  @GetMapping("order/{orderId}")
  public Order getOrder(@PathVariable long orderId) {
    return orderRepo.findById(orderId).orElseThrow();
  }
}
