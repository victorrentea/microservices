package victor.training.ms.shared;

public record OrderStatusChangedEvent(long orderId, OrderStatus status, String customerId) {
}
