package victor.training.ms.catalog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;


@Slf4j
@RequiredArgsConstructor
@Service
public class StockEventListener {
  private final ProductRepo productRepo;

  //@Bean
  //  public Consumer<OutOfStockEvent> onOutOfStock() {
  //    return event -> { handle event }
  //  }
}
