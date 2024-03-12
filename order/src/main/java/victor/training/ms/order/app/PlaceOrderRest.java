package victor.training.ms.order.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import victor.training.ms.order.client.CatalogClient;
import victor.training.ms.order.client.InventoryClient;
import victor.training.ms.order.client.PaymentClient;
import victor.training.ms.order.entity.LineItem;
import victor.training.ms.order.entity.Order;
import victor.training.ms.order.repo.OrderRepo;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PlaceOrderRest {
  private final OrderRepo orderRepo;
  private final CatalogClient catalogClient;
  private final PaymentClient paymentClient;
  private final InventoryClient inventoryClient;

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
}
