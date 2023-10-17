package victor.training.ms.shared;

public record PaymentResultEvent(long orderId, boolean ok) {
}
