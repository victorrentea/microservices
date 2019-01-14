package com.example.reservationservice;

import java.util.stream.Stream;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class ReservationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
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

