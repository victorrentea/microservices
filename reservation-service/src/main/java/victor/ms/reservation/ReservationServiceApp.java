package victor.ms.reservation;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
@IntegrationComponentScan
@RestController
public class ReservationServiceApp {

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApp.class, args);
	}

	@Autowired
	private ReservationRepo repo;

	@EventListener(ApplicationStartedEvent.class)
	public void insertDummyData() {
		Stream.of("Bianca", "Marian", "Victor", "Adrian", "Eugen")
				.map(Reservation::new)
				.forEach(repo::save);
	}

	@GetMapping("/reservations")
	public List<Reservation> getAll() {
		log.info("Get");
		return repo.findAll();
	}
}

@MessageEndpoint
class ReservationCreator {
	private final static Logger log = LoggerFactory.getLogger(ReservationCreator.class);
	@Autowired
	private ReservationRepo repo;
	@Bean
	Consumer<String> createReservation() {
		return rn-> {
			System.out.println("Primit rezervare de creat: " + rn);
			log.debug("execut");
			repo.save(new Reservation(rn));
		};
	}
}


interface ReservationRepo extends JpaRepository<Reservation, Long> {
}


@Data
@Entity
class Reservation {
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

