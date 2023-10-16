package victor.training.ms.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Reservation {
  @Id
  @GeneratedValue
  private Long id;
  private String reservationName;

  private Reservation() {
  }

  public Reservation(String reservationName) {
    this.reservationName = reservationName;
  }

}
