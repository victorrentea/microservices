package victor.training.ms.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HelloRest {
  private final CustomerClient customerClient;
  private final RestTemplate restTemplate;

  @GetMapping("notification/hello") // http://localhost/notification/hello
  public String hello() { //  TODO keep
    log.info("Requesting customer");
    // IP:PORT ACCESS DIRECT - nefezabil in realitate
//    var dto = new RestTemplate().getForObject("http://localhost:8082/customer/margareta", CustomerDto.class);

    // PRIN API_GATEWAY: waste de resurse si timp
    // am facut new RestTemplate  -> TraceId nu se propaga intre sisteme::  NU MERGE TODO
//    var dto = new RestTemplate().getForObject("http://localhost/customer/margareta", CustomerDto.class);

//    var dto = restTemplate.getForObject("http://localhost/customer/margareta", CustomerDto.class);

    // am mers direct la micro celalalt fara api gateway
    // merge si traceId propagation
//    var dto = restTemplate.getForObject("http://customer/customer/margareta", CustomerDto.class);

    log.info("Cu traceid");
    CompletableFuture.runAsync(() -> {
      log.info("Traceid pierdut");
    });// ATENTIE CAND SCHIMBI THREADUL ca poti pierde TraceID !

    var dto = customerClient.getCustomer("margareta");
    return "Hello from Notification: " + dto;
  }
  private final ThreadPoolTaskExecutor e;

  // asta-i pemntru Gicu din spate
//  @GetMapping("product/{id}")
//  public CompletableFuture<String> getProduct(long id) {
//    CompletableFuture<String> cf = new CompletableFuture<>();
//    ongoingRequests.put(id, cf);
//    return cf; // ii spune lui SPring sa lase in asteptare req HTTP dar fara sa blocheze threadul curent
//  }
//  Map<Long, CompletableFuture<String>> ongoingRequests = synchronizedMap(new HashMap<>());
//  @Scheduled(fixedDelay = 1000)
//  public void method() {
//    Map<Long, CompletableFuture<String>> clone;
//    synchronized (ongoingRequests) {
//      clone = new HashMap<>(ongoingRequests);
//      ongoingRequests.clear();
//    }
//    Set<Long> productIds = clone.keySet();
//    Map<Long, String> results = restApi.getAll(productIds);
//    results.forEach((productId, productStr) -> clone.get(productId)
//        .complete(results.get(productId))); // aici un request HTTP primeste raspunsul asteptat,
//    // apoi spring inchide conex http cu lientu ramasa pana acum deschisa
//  }
}
