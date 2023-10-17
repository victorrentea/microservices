package victor.training.ms.inventory;

import static java.util.Objects.requireNonNull;

public record LineItem(long productId, int count) {
}
