package victor.training.ms.order.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.AbstractAggregateRoot;
import victor.training.ms.shared.OrderStatus;
import victor.training.ms.shared.OrderStatusChangedEvent;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static victor.training.ms.shared.OrderStatus.*;

@ToString
@Entity
@Table(name = "ORDERS")
@Getter(onMethod = @__(@JsonProperty))
public class Order extends AbstractAggregateRoot<Order> {
  @Id
  @GeneratedValue
  private Long id;
  private LocalDate placedOn = LocalDate.now();
  @Setter
  @NotNull
  private String customerId;
  @Setter
  private String shippingAddress;
  @Setter
  @NotNull
  private Double total;
  private OrderStatus status = AWAITING_PAYMENT;
  private String shippingTrackingNumber;
  @ElementCollection
  @Setter
  @NotNull
  @NotEmpty
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private Map<Long, Integer> items = new HashMap<>();

  private void requireStatus(OrderStatus... allowed) {
    if (!List.of(allowed).contains(status)) {
      throw new IllegalArgumentException("Illegal state: " + status + ". Expected one of: " + Arrays.toString(allowed));
    }
  }

  public Order paid(boolean ok) {
    requireStatus(AWAITING_PAYMENT);
    status = ok ? PAYMENT_APPROVED : PAYMENT_FAILED;
    registerEvent(new OrderStatusChangedEvent(id, status, customerId));
    return this;
  }

  public Order scheduleForShipping(String trackingNumber) {
    requireStatus(PAYMENT_APPROVED);
    status = SHIPPING_IN_PROGRESS;
    shippingTrackingNumber = trackingNumber;
    registerEvent(new OrderStatusChangedEvent(id, status, customerId));
    return this;
  }

  public Order shipped(boolean ok) {
    requireStatus(SHIPPING_IN_PROGRESS);
    status = ok ? SHIPPING_COMPLETED : SHIPPING_FAILED;
    registerEvent(new OrderStatusChangedEvent(id, status, customerId));
    return this;
  }

  public void shippingRejected() {
    status = SHIPPING_REJECTED;
  }
}
