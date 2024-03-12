package victor.training.ms.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

  @PostMapping("inventory/stock/reserve")
  public void reserveStock(@RequestParam long orderId, @RequestBody List<LineItem> items) {
    reserveStockService.reserveStock(orderId, items);
  }

  @EventListener(ApplicationStartedEvent.class)
  void initialData() {
    stockRepo.save(new Stock()
        .productId(1L)
        .items(10));
  }
}
