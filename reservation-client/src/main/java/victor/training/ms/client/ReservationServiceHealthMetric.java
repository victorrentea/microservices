package victor.training.ms.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class ReservationServiceHealthMetric implements HealthIndicator {
  @Autowired
  private RestTemplate rest;

  @Override
  public Health health() {
    try {
      String url = "http://reservation-service/actuator/health";
      Map<String, Object> responseMap = rest.getForObject(url, Map.class);
      if (responseMap.get("status").equals("UP")) {
        return Health.up().build();
      }
      return Health.down().build();
    } catch (RestClientException e) {
      log.error("Health check failed :  " + e);
      return Health.down(e).build();
    }
  }
}


// Feign reservation-service