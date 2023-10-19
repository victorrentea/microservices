package victor.training.ms.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import victor.training.ms.shared.OrderStatus;
import victor.training.ms.shared.PaymentResultEvent;
import victor.training.ms.shared.ShippingResultEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toMap;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PlaceOrderRest {
  private final OrderRepo orderRepo;
  private final CatalogClient catalogClient;
  private final PaymentClient paymentClient;
  private final InventoryClient inventoryClient;
  private final ShippingModule shippingDoor;

  public record PlaceOrderRequest(
      @Schema(example = "margareta") String customerId,
      List<LineItem> items,
      String shippingAddress) {
  }

  @PostMapping("order")
  public String placeOrder(@RequestBody PlaceOrderRequest request) {
    List<Long> productIds = request.items().stream().map(LineItem::productId).toList();
    Map<Long, Double> prices = catalogClient.getManyPrices(productIds);
    if (!prices.keySet().containsAll(productIds)) {
      throw new IllegalArgumentException("Some product ids not found! requested:"+productIds + " found:"+prices.keySet());
    }
    log.info("Got prices: {}", prices);
    Map<Long, Integer> items = request.items.stream().collect(toMap(LineItem::productId, LineItem::count));
    double totalPrice = request.items.stream().mapToDouble(e -> e.count() * prices.get(e.productId())).sum();
    Order order = new Order()
        .items(items)
        .shippingAddress(request.shippingAddress)
        .customerId(request.customerId)
        .total(totalPrice);
    orderRepo.save(order);

    inventoryClient.reserveStock(order.id(), request.items); //10ms - 1s - 5s: astepti cu 1/200 de th tomcat
    // cu 1MB de RAM (thread stack) blocat
    // cu 1 thread blocat din cele 200 default ale Tomcatului
    // din java 21 in sus, tocmat poate sa aiba 500k de threaduri, fiecare punct in care blochezi tine doar 200b~ ocupati

//    CompletableFuture.runAsync(() ->
//      sendMessage(new ReserveStockCommand(order.id(), request.items)); // mesajul pleaca, iar eu nu sunt blocat
    // va trebui sa suspend executia pana cand primesc un raspuns pe o coada de reply
    // si cand vine acel mesaj sa continui cu linia de mai jos
    return paymentClient.generatePaymentUrl(order.id(), order.total()) + "&orderId=" + order.id();
  }


private final StreamBridge streamBridge;

  @Bean
  public Consumer<PaymentResultEvent> onPaymentResultEvent() {
    return event -> {
      log.info("Received: {}", event);
      Order order = orderRepo.findById(event.orderId()).orElseThrow();
      order.paid(event.ok());
      if (order.status() == OrderStatus.PAYMENT_APPROVED) {
        String trackingNumber = shippingDoor.requestShipment(order.shippingAddress());

        streamBridge.send("requestTrackingNumber-out", order.shippingAddress());

        order.scheduleForShipping(trackingNumber);
      }
      orderRepo.save(order);
    };
  }

  @Bean
  public Consumer<ShippingResultEvent> onShippingResultEvent() {
    return event -> {
      log.info("Received: {}", event);
      long orderId = event.orderId(); // acts as CORRELATION ID
      Order order = orderRepo.findById(orderId).orElseThrow();
      order.shipped(event.ok());
      orderRepo.save(order);
    };
  }
}
