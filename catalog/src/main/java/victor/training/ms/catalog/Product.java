package victor.training.ms.catalog;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

@Entity
@Data
public class Product {
  @Id
  @GeneratedValue
  private Long id;

  private String name;

  private String description;

  private boolean inStock;

  private Double price;
}
