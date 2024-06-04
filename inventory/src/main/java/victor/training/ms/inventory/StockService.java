package victor.training.ms.inventory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
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
public class StockService {
  private final StockRepo stockRepo;
  private final StockReservationRepo stockReservationRepo;

  //  private final StreamBridge streamBridge;
  //  streamBridge.send("backInStockEvent-out-0", event);

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

  private void subtractStock(long productId, Integer count) {
    Stock stock = stockRepo.findByProductId(productId).orElseThrow();
    stock.remove(count);
    stockRepo.saveAndFlush(stock);
    if (stock.items()==0) {
      log.info("Sending message out");
      streamBridge.send("outOfStockEvent-out-0", new OutOfStockEvent(productId));
    }
  }
  private final StreamBridge streamBridge;
  private void createReservation(long orderId, long productId, Integer count) {
    StockReservation reservation = new StockReservation()
        .orderId(orderId)
        .productId(productId)
        .items(count);
    stockReservationRepo.save(reservation);
  }
  public record OutOfStockEvent(long productId){}
  @Transactional
  public void addStock(long productId, int items) {
    Stock stock = stockRepo.findByProductId(productId).orElse(new Stock().productId(productId));
    stock.add(items);
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
