package victor.training.ms.inventory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import victor.training.ms.shared.BackInStockEvent;
import victor.training.ms.shared.OutOfStockEvent;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@SpringBootApplication
@RestController
public class InventoryApp {
  public static void main(String[] args) {
      SpringApplication.run(InventoryApp.class, args);
  }
  private final ReserveStockService reserveStockService;
  private final StockRepo stockRepo;

  @PutMapping("inventory/stock/reserve/{orderId}")
  public void reserveStock(@PathVariable long orderId, @RequestBody List<LineItem> items) {
    reserveStockService.reserveStock(orderId, items);
  }

  @Transactional
  @EventListener(ApplicationStartedEvent.class)
  void initialData() {
    List<Stock> existing = stockRepo.findAllByProductIdOrderByIdAsc(1L);
    if (existing.isEmpty()) {
      stockRepo.save(new Stock()
          .productId(1L)
          .items(20));
      return;
    }

    Stock primary = existing.get(0);
    int totalItems = existing.stream().mapToInt(Stock::items).sum();
    primary.items(totalItems);
    stockRepo.save(primary);

    if (existing.size() > 1) {
      stockRepo.deleteAll(existing.subList(1, existing.size()));
      log.warn("Collapsed {} duplicate stock rows for productId=1 into stockId={} with items={}", existing.size() - 1, primary.id(), totalItems);
    }
  }

  private final StreamBridge streamBridge;
  @EventListener
  public void onOutOfStock(OutOfStockEvent event) {
    log.info("Sending: {}", event);
    streamBridge.send("outOfStockEvent-out-0", event);
  }
  @EventListener
  public void onBackInStock(BackInStockEvent event) {
    log.info("Sending: {}", event);
    streamBridge.send("backInStockEvent-out-0", event);
  }
}
