package com.example.reservationservice;

import java.util.stream.Stream;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esotericsoftware.minlog.Log;

@SpringBootApplication
@EnableDiscoveryClient
@EnableBinding(Sink.class)
@IntegrationComponentScan
public class ReservationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}
}

@MessageEndpoint
class ReservationCreator {
	private final static Logger log = LoggerFactory.getLogger(ReservationCreator.class);
	@Autowired
	private ReservationRestRepository repo;
	
	@ServiceActivator(inputChannel = Sink.INPUT)
	public void createReservation(String rn) {
		System.out.println("Primit rezervare de creat: " + rn);
		log.debug("execut");
		repo.save(new Reservation(rn));
	}
}

@Component
class DummyCLR implements CommandLineRunner {
	@Autowired
	private ReservationRestRepository repo;

	public void run(String... args) throws Exception {
		Stream.of("Bianca", "Marian", "Victor", "Adrian", "Eugen")
			.map(Reservation::new)
			.forEach(repo::save);
	}
	
}

@RestController
class MessageController {
	@Autowired
	private MessageProvider provider;
	
	public MessageController() {
		System.out.println("new instance");
	}
	@GetMapping("message")
	public String getMessage() {
		return provider.getMessage();
	}
}

@Component
@RefreshScope
class MessageProvider {
	
	@Value("${message:nuefrate}")
	private String message;
	
	public String getMessage() {
		return message;
	}
}


@RepositoryRestResource(path= "reservations")
interface ReservationRestRepository extends JpaRepository<Reservation, Long> {
	
}


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



	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getReservationName() {
		return reservationName;
	}

	public void setReservationName(String reservationName) {
		this.reservationName = reservationName;
	}
	
	
}

