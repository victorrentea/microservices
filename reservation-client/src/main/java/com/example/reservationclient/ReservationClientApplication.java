package com.example.reservationclient;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ReservationClientApplication {
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(ReservationClientApplication.class, args);
	}

}

@RestController
class MyController {
	@Value("${message:nuefrate}")
	private String message;
	
	@GetMapping("message")
	public String getMessage() {
		return message;
	}
	
	@Autowired
	private RestTemplate rest;
	
	@GetMapping("reservations")
	public List<String> getReservationNames() {
		List<String> lista14 = new ArrayList<String>(); // NU ai cum sa obtii <String> din lista14 datorita Type Erasure
		List<String> lista = new ArrayList<String>() { }; // MAMA!! se pastreaza <String> din cauza ca subclasam
		
		
		// DIN list pot obtine faptul ca el e "de String" ?
		
		ParameterizedTypeReference<Resources<Reservation>> type = 
				new ParameterizedTypeReference<Resources<Reservation>>() { };
		
		ResponseEntity<Resources<Reservation>> entity = rest.exchange("http://localhost:9080/reservations", HttpMethod.GET, null, type);
		
		return entity.getBody()
			.getContent()
			.stream()
			.map(Reservation::getReservationName)
			.collect(toList());
	}
}

class Reservation {
	private String reservationName;

	public Reservation(String reservationName) {
		this.reservationName = reservationName;
	}

	public String getReservationName() {
		return reservationName;
	}

	public void setReservationName(String reservationName) {
		this.reservationName = reservationName;
	}

	public Reservation() {
		super();
	}
	
	
}



