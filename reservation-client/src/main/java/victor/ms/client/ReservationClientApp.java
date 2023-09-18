package victor.ms.client;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.time.Duration.ofSeconds;
import static java.util.Arrays.asList;
import static java.util.Arrays.compare;
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
				// aici : iti faci un RestTemplate dedicat cu connect/read timeoout
				.setConnectTimeout(ofSeconds(4))
				.setReadTimeout(ofSeconds(4))
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

	private static final AtomicInteger fragileAtomic = new AtomicInteger();
	@GetMapping("fragile")
	@Bulkhead(name = "fragile")
	public String fragile() {
		int id = fragileAtomic.incrementAndGet();
		log.info("Sending " + id);
		String forObject = rest.getForObject("http://reservation-service/fragile", String.class);
		log.info("Receive " + id);
		return forObject;
	}


	@GetMapping("rate-limited")
	@RateLimiter(name = "rate-limited", fallbackMethod = "rateLimitReached")
	public String rateLimited() {
		log.info("Sending");
    return rest.getForObject("http://reservation-service/rate-limited", String.class);
	}
	public String rateLimitReached(Exception e) {
		return "Rate Limit Reached: " + e;
	}




	private static final AtomicInteger retryAtomic = new AtomicInteger();
	@GetMapping("flaky")
	@Retry(name = "flaky")
	public String flaky() {
		log.info("Request #" + retryAtomic.incrementAndGet());
		try {
			return rest.getForObject("http://reservation-service/flaky", String.class);
		} catch (RestClientException e) {
			log.error("Thrown: " + e);
			throw e;
		}
	}





	@GetMapping("flaky-slow")
	@Retry(name = "flaky-slow")
//	@Transactional // depinde cum pica:
//	a) n-ai bani < NU RETRY
//	b) OptimisticLockingException < DA RETRY
//	b) OptimisticLockingException < DA RETRY
	public String flakySlow() {
		log.info("Request #" + retryAtomic.incrementAndGet());
		try {
			return rest.getForObject("http://reservation-service/flaky-slow", String.class);
		} catch (RestClientException e) {
			log.error("Thrown: " + e);
			throw e;
		}
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



