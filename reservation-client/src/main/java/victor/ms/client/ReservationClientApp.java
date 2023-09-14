package victor.ms.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
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
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofSeconds;
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
		return builder
				.setConnectTimeout(ofSeconds(2))
				.setReadTimeout(ofSeconds(2))
				.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(ReservationClientApp.class, args);
	}

}

@Slf4j
@RequiredArgsConstructor
@RestController
class MyController {
	private final RestTemplate rest;


	public List<String> fallbackResponse(Exception e)
	{
		log.error("Recovered from ", e);
		return asList("Basescu");
	}

	@GetMapping("reservations")
	@CircuitBreaker(name = "get-reservations",fallbackMethod = "fallbackResponse")
	public List<String> getReservationNames() {
		log.info("Get");
		ResponseEntity<List<Reservation>> entity = rest.exchange("http://reservation-service/reservations",
						HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
		
		return entity.getBody().stream()
			.map(Reservation::getReservationName)
			.collect(toList());
	}

	private final StreamBridge streamBridge;

	@PostMapping("reservation")
	public void createReservation(@RequestBody Reservation reservation) {
		streamBridge.send("createReservation", reservation.getReservationName());
		log.info("Message Sent");
	}
}

@Data
class Reservation {
	private String reservationName;
}



