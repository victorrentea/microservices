package victor.training.ms.order;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class InventoryHealthMetric implements HealthIndicator {
  @Value("http://localhost:8083")
  private String urlBase;

  @Autowired
  private RestTemplate restTemplate;

  @Override
  public Health health() {
    try {
        String url = urlBase + "/actuator/health";
        Map<String, Object> responseMap = new RestTemplate().getForObject(url, Map.class);
      //  {"status": "UP"}
      if (responseMap.get("status").equals("UP")) {
        return Health.up().build();
      }
    } catch (RestClientException e) {
      log.error("Health check failed to " + urlBase + " :  " + e);
    }

    return Health.down().build();
//        return Health.up().status("SOMETHING REALLY BAD HAPPENED").build();
  }
}
