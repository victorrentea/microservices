package victor.training.ms.order.entity;

import static java.util.Objects.requireNonNull;

public record LineItem(long productId, int count) {
}
