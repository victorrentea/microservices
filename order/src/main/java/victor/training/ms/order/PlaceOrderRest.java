package victor.training.ms.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import victor.training.ms.shared.OrderStatus;
import victor.training.ms.shared.PaymentResultEvent;
import victor.training.ms.shared.ShippingResultEvent;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toMap;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PlaceOrderRest {
  private final OrderRepo orderRepo;
  private final CatalogDoor catalogDoor;
  private final PaymentModule paymentModule;
  private final InventoryDoor inventoryDoor;
  private final ShippingModule shippingDoor;

  public record PlaceOrderRequest(
      @Schema(example = "margareta") String customerId,
      List<LineItem> items,
      String shippingAddress) {
  }

  @PostMapping("order")
  public String placeOrder(@RequestBody PlaceOrderRequest request) {
    List<Long> productIds = request.items().stream().map(LineItem::productId).toList();
    Map<Long, Double> prices = catalogDoor.getManyPrices(productIds);
    if (!prices.keySet().containsAll(productIds)) {
      throw new IllegalArgumentException("Some product ids not found! requested:"+productIds + " found:"+prices.keySet());
    }
    Map<Long, Integer> items = request.items.stream().collect(toMap(LineItem::productId, LineItem::count));
    double totalPrice = request.items.stream().mapToDouble(e -> e.count() * prices.get(e.productId())).sum();
    Order order = new Order()
        .items(items)
        .shippingAddress(request.shippingAddress)
        .customerId(request.customerId)
        .total(totalPrice);
    orderRepo.save(order);
    inventoryDoor.reserveStock(order.id(), request.items);
    return paymentModule.generatePaymentUrl(order.id(), order.total()) + "&orderId=" + order.id();
  }

  @Bean
  public Consumer<PaymentResultEvent> onPaymentResultEvent() {
    return event -> {
      log.info("Payment event: {}", event);
      Order order = orderRepo.findById(event.orderId()).orElseThrow();
      order.paid(event.ok());
      if (order.status() == OrderStatus.PAYMENT_APPROVED) {
        String trackingNumber = shippingDoor.requestShipment(order.shippingAddress());
        order.scheduleForShipping(trackingNumber);
      }
      orderRepo.save(order);
    };
  }

  @Bean
  public Consumer<ShippingResultEvent> onShippingResultEvent() {
    return event -> {
      log.info("Shipping event: {}", event);
      Order order = orderRepo.findById(event.orderId()).orElseThrow();
      order.shipped(event.ok());
      orderRepo.save(order);
    };
  }
}
