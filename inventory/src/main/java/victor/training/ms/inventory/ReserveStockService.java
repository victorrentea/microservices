package victor.training.ms.inventory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import victor.training.ms.shared.OrderStatus;
import victor.training.ms.shared.OrderStatusChangedEvent;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReserveStockService {
  private final StockRepo stockRepo;
  private final StockReservationRepo stockReservationRepo;

  @Transactional
  public void reserveStock(long orderId, List<LineItem> items) {
    for (var item : items) {
      subtractStock(item.productId(), item.count());
      createReservation(orderId, item.productId(), item.count());
    }
  }

  private void createReservation(long orderId, long productId, Integer count) {
    StockReservation reservation = new StockReservation()
        .orderId(orderId)
        .productId(productId)
        .items(count);
    stockReservationRepo.save(reservation);
  }

  private void subtractStock(long productId, Integer count) {
    Stock stock = stockRepo.findByProductId(productId).orElseThrow();
    stock.remove(count);
    stockRepo.save(stock);
  }

  @Bean
  public Consumer<OrderStatusChangedEvent> onOrderStatusChangedEvent() {
    return event -> {
      if (event.status() == OrderStatus.PAYMENT_APPROVED) {
        log.info("Stock reservation confirmed: " + event);
        stockReservationRepo.deleteAllByOrderId(event.orderId());
      }
    };
  }


}
