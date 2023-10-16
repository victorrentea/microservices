package victor.training.ms.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import static java.time.Duration.ofSeconds;

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class ReservationClientApp {

  public static void main(String[] args) {
    SpringApplication.run(ReservationClientApp.class, args);
  }


  @Bean
  @LoadBalanced
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
//  @Bean
//  @LoadBalanced
//  public RestTemplate restTemplate(RestTemplateBuilder builder) {
//    return builder
//        .setConnectTimeout(ofSeconds(4))
//        .setReadTimeout(ofSeconds(4))
//        .build();
//  }
}



