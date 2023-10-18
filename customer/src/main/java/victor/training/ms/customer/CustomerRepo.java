package victor.training.ms.customer;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepo extends JpaRepository<Customer, String> {
//  @Bulkhead(name = "query-gras")
//  List<tot> exportuDe10GBDeFinalDeLuna();
}
