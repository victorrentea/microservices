package victor.training.ms.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryHealthMetric implements HealthIndicator {
  private final RestTemplate restTemplate;

  @Override
  public Health health() {
    try {
      // see its output at: http://localhost:8088/actuator/health
      String url = "http://localhost:8083/actuator/health";
      Map<String, Object> responseMap = restTemplate.getForObject(url, Map.class);

      if (responseMap.get("status").equals("UP")) {//  {"status": "UP"}
        return Health.up().build();
      }
    } catch (Exception e) {
      log.error("Health check failed: " + e);
    }

    return Health.down().build();
//        return Health.up().status("SOMETHING REALLY BAD HAPPENED").build();
  }
}
