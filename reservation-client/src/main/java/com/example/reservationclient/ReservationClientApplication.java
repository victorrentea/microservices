package com.example.reservationclient;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

// daca vrei vreodata mai multe cozi de iesire, le poti definit pe toate 
// intr-o interfgata pe care ti-o creezi tu "cu manuntele astea doua" - Ciorbea
//interface MyOutputQueues {
//	@Output("createCustomer")
//	MessageChannel createCustomer();
//	@Output("activateCustomer")
//	MessageChannel activateCustomer();
//}

@EnableHystrix
@EnableDiscoveryClient
@SpringBootApplication
@EnableBinding(Source.class)
public class ReservationClientApplication {
	
	@Bean
	@LoadBalanced
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
	
	public List<String> fallbackResponse() {
		return asList("Basescu");
	}
	
	@HystrixCommand(fallbackMethod = "fallbackResponse")
	@GetMapping("reservations")
	public List<String> getReservationNames() {
		List<String> lista14 = new ArrayList<String>(); // NU ai cum sa obtii <String> din lista14 datorita Type Erasure
		List<String> lista = new ArrayList<String>() { }; // MAMA!! se pastreaza <String> din cauza ca subclasam
		
		
		// DIN list pot obtine faptul ca el e "de String" ?
		
		ParameterizedTypeReference<Resources<Reservation>> type = 
				new ParameterizedTypeReference<Resources<Reservation>>() { };
		
		ResponseEntity<Resources<Reservation>> entity = 
				rest.exchange("http://reservation-service/reservations", 
						HttpMethod.GET, null, type);
		
		return entity.getBody()
			.getContent()
			.stream()
			.map(Reservation::getReservationName)
			.collect(toList());
	}
	
	@Autowired
	private Source source;
	private final static Logger log = LoggerFactory.getLogger(MyController.class);
	
	@PostMapping("reservation")
	public void createReservation(@RequestBody Reservation reservation) {
		source.output().send(MessageBuilder.withPayload(reservation.getReservationName()).build());
		log.debug("Message Sent");
		System.out.println("Message Sent");
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



