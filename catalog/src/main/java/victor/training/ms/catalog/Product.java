package victor.training.ms.catalog;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Product {
  @Id
  @GeneratedValue
  private Long id;

  private String name;

  private String description;

  private Double price;

}
