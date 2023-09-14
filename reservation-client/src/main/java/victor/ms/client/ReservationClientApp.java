package victor.ms.client;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

// daca vrei vreodata mai multe cozi de iesire, le poti definit pe toate 
// intr-o interfgata pe care ti-o creezi tu "cu manuntele astea doua" - Ciorbea
//interface MyOutputQueues {
//	@Output("createCustomer")
//	MessageChannel createCustomer();
//	@Output("activateCustomer")
//	MessageChannel activateCustomer();
//}

@EnableDiscoveryClient
@SpringBootApplication
public class ReservationClientApp {

	@Bean
	@LoadBalanced
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(ReservationClientApp.class, args);
	}

}


@Slf4j
@RestController
class MyController {
	@Autowired
	private RestTemplate rest;
	public List<String> fallbackResponse() {
		return asList("Basescu");
	}

//	@CircuitBreaker
//	@HystrixCommand(fallbackMethod = "fallbackResponse")

	@GetMapping("reservations")
	public List<String> getReservationNames() {
		log.info("Get");
		ParameterizedTypeReference<List<Reservation>> type = new ParameterizedTypeReference<>() {};
		ResponseEntity<List<Reservation>> entity =
				rest.exchange("http://reservation-service/reservations", 
						HttpMethod.GET, null, type);
		
		return entity.getBody()
			.stream()
			.map(Reservation::getReservationName)
			.collect(toList());
	}
	
//	@Autowired
//	private Source source;
//	private final static Logger log = LoggerFactory.getLogger(MyController.class);
//
//	@PostMapping("reservation")
//	public void createReservation(@RequestBody Reservation reservation) {
//		source.output().send(MessageBuilder.withPayload(reservation.getReservationName()).build());
//		log.debug("Message Sent");
//		System.out.println("Message Sent");
//	}
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



