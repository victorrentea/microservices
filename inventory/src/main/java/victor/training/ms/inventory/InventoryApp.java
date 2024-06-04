package victor.training.ms.inventory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

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
  private final StockService stockService;
  private final StockRepo stockRepo;

  @PutMapping("inventory/stock/reserve/{orderId}")
  public void reserveStock(@PathVariable long orderId, @RequestBody List<LineItem> items) {
    stockService.reserveStock(orderId, items);
  }

  @EventListener(ApplicationStartedEvent.class)
  void initialData() {
    stockRepo.save(new Stock()
        .productId(1L)
        .items(1));
  }

}
