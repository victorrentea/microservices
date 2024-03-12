package victor.training.ms.order.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import victor.training.ms.order.client.CatalogClient;
import victor.training.ms.order.client.InventoryClient;
import victor.training.ms.order.client.PaymentClient;
import victor.training.ms.order.client.ShippingClient;
import victor.training.ms.order.entity.LineItem;
import victor.training.ms.order.entity.Order;
import victor.training.ms.order.repo.OrderRepo;
import victor.training.ms.shared.OrderStatus;
import victor.training.ms.shared.PaymentResultEvent;
import victor.training.ms.shared.ShippingResultEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  private final ShippingClient shippingClient;

  public record PlaceOrderRequest(
      @Schema(example = "margareta") String customerId,
      List<LineItem> items,
      String shippingAddress) {
  }

  @PostMapping("order")
  public String placeOrder(@RequestBody PlaceOrderRequest request) {
    List<Long> productIds = request.items().stream().map(LineItem::productId).toList();
    Map<Long, Double> prices = catalogClient.getManyPrices(productIds); // batching
    if (prices.size() != productIds.size()) {
      throw new IllegalArgumentException("Not all products have a price");
    }

    log.info("Got prices: {}", prices);
    Map<Long, Integer> items = request.items().stream().collect(toMap(LineItem::productId, LineItem::count));
    double totalPrice = request.items().stream().mapToDouble(e -> e.count() * prices.get(e.productId())).sum();
    Order order = new Order()
        .items(items)
        .shippingAddress(request.shippingAddress())
        .customerId(request.customerId())
        .total(totalPrice);
    orderRepo.save(order);

    // reservation
    inventoryClient.reserveStock(order.id(), request.items());

    log.info("Created order: {}", order);
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
        String trackingNumber = shippingClient.requestShipment(event.orderId(), order.shippingAddress());
        order.scheduleForShipping(trackingNumber);
      }
      orderRepo.save(order);
      log.info("Order is now: " + order);
    };
  }

  record ShippingAcceptedEvent(long orderId, String trackingNumber) {
  }

  @Bean
  public Consumer<ShippingAcceptedEvent> onShippingAcceptedEvent() {
    return event -> {
      Order order = orderRepo.findById(event.orderId()).orElseThrow();
      if (event.trackingNumber() != null) {
        log.info("Got tracking number: " + event.trackingNumber());
        order.scheduleForShipping(event.trackingNumber());
        orderRepo.save(order);
      } else {
        log.warn("Shipping Rejected!");
        // compensare
//        streamBridge.send("revertPayment-out", order.id()); // TODO tema undo payment
//        inventoryClient.releaseStock(order.id());// TODO tema pt extra eficienta ?
        order.shippingRejected();
        orderRepo.save(order);
      }
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
      log.info("Saved order: " + order);
    };
  }
}
