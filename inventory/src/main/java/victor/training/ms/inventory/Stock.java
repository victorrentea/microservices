package victor.training.ms.inventory;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.domain.AbstractAggregateRoot;

@Data
@Entity
public class Stock extends AbstractAggregateRoot<Stock> {
  @Id
  @GeneratedValue
  private Long id;

  @NotNull
  private Long productId;

  @NotNull
  private Integer items = 0;

  public void add(int n) {
    if (n <= 0) {
      throw new IllegalArgumentException("Negative: " + n);
    }
    items += n;
  }

  public void remove(Integer n) {
    if (n <= 0) {
      throw new IllegalArgumentException("Negative: " + n);
    }
    if (n > items) {
      throw new IllegalArgumentException("Not enough stock to remove: " + n);
    }
    items -= n;
  }
}
