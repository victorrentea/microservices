package victor.training.ms.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import victor.training.ms.notification.CustomerClient.CustomerDto;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HelloRest {
  private final CustomerClient customerClient;
  private final RestTemplate restTemplate;

  @GetMapping("hello")
  public String hello() {
    log.info("Requesting customer");
    var dto = new RestTemplate().getForObject("http://localhost:8082/customer/margareta", CustomerDto.class);
//    var dto = new RestTemplate().getForObject("http://localhost/customer/margareta", CustomerDto.class);
//    var dto = restTemplate.getForObject("http://localhost/customer/margareta", CustomerDto.class);
//    var dto = customerClient.getCustomer("margareta");
    return "Hello from Notification: " + dto;
  }
}
