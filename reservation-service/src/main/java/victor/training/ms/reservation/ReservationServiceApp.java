package victor.training.ms.reservation;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.IntegrationComponentScan;
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

	@GetMapping("/fragile")
	public String fragile() throws InterruptedException {
		Thread.sleep(3000);
		return "response from a fragile slow system";
	}

	@GetMapping("/rate-limited")
	public String rateLimited() {
		return "fast free response, but expensive if more than 10 / minute";
	}

	@GetMapping("flaky")
	public String flaky() {
		if (Math.random() < .7) {
			throw new IllegalArgumentException("FLAKY ERROR");
		}
		return "Normal Result";
	}

	@GetMapping("slow")
	public String slow() throws InterruptedException {
		Thread.sleep(300*1000); // 100 sec
		return "SLOW";
	}
	@GetMapping("flaky-slow")
	public ResponseEntity<String> flakySlow() throws InterruptedException {
		double r = Math.random();
		if (r < .3) {
			// 30% chance FAST KO 400
			return ResponseEntity.badRequest().body("Fast ERROR not to be retried eg invalid request");
		} else if (r < .6) {
			// 30% chance SLOW OK ?
			Thread.sleep(100*1000); // 100s
			return ResponseEntity.ok("Late but OK result");
		} else {
			// 40 % chance FAST OK
			return ResponseEntity.ok("FAST OK");
		}
	}

	@Bean
	public Consumer<String> createReservationMessageListener() {
		return rn-> {
			log.info("Creating reservation via MQ: " + rn);
			repo.save(new Reservation(rn));
		};
	}

}


