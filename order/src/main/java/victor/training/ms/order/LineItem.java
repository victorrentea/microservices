package victor.training.ms.order;

import static java.util.Objects.requireNonNull;

public record LineItem(long productId, int count) {
}
