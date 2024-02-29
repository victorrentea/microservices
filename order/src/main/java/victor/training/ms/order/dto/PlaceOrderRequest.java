package victor.training.ms.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import victor.training.ms.order.LineItem;

import java.util.List;

public record PlaceOrderRequest(
      @Schema(example = "margareta") String customerId,
      List<LineItem> items,
      String shippingAddress) {
  }