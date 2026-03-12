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
    if (stockReservationRepo.existsByOrderId(orderId)) { // idempotent operation now
      throw new IllegalStateException("Already reserved: " + orderId);
    }
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
    Stock stock = loadAndCollapseStockRows(productId);
    stock.remove(count);
    stockRepo.save(stock);
  }

  private Stock loadAndCollapseStockRows(long productId) {
    List<Stock> rows = stockRepo.findAllByProductIdOrderByIdAsc(productId);
    if (rows.isEmpty()) {
      throw new IllegalArgumentException("No stock row for productId=" + productId);
    }
    if (rows.size() == 1) {
      return rows.get(0);
    }

    // Defensive cleanup for environments where old seed logic created duplicates.
    Stock primary = rows.get(0);
    int totalItems = rows.stream().mapToInt(Stock::items).sum();
    primary.items(totalItems);
    stockRepo.deleteAll(rows.subList(1, rows.size()));
    stockRepo.save(primary);
    log.warn("Collapsed {} duplicate stock rows for productId={} into stockId={} with items={}", rows.size() - 1, productId, primary.id(), totalItems);
    return primary;
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
