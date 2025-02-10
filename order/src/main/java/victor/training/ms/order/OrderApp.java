package victor.training.ms.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestTemplate;
import victor.training.ms.order.entity.Order;
import victor.training.ms.order.repo.OrderRepo;
import victor.training.ms.shared.OrderStatusChangedEvent;

import java.util.Map;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient // needed if using RestTemplate
@RequiredArgsConstructor
public class OrderApp {
  private final OrderRepo orderRepo;

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  public static void main(String[] args) {
    SpringApplication.run(OrderApp.class, args);
  }

  @EventListener(ApplicationStartedEvent.class)
  @Transactional
  public void initialData() {
    Order order = orderRepo.save(new Order()
        .customerId("margareta")
        .shippingAddress("shipping address")
        .total(100d)
        .items(Map.of(1L, 2)));
    order.paid(true);
    order.scheduleForShipping("tracking#");
    order.shipped(true);
  }

  private final StreamBridge streamBridge;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onOrderStatusChanged(OrderStatusChangedEvent event) {
    // this method runs after the successful COMMIT of the @Transactional
    //   from within which the OrderStatusChangedEvent was published

    // #1 Notification
    streamBridge.send("OrderStatusChangedEvent-out", event);
//    streamBridge.send("OrderStatusChang edEvent-out", new OrderStatusChanged2Event(order.id));

    // #2 Event-Carried State Transfer [@mfowler] => burn the REST!!!
//    streamBridge.send("OrderStatusChangedEvent-out", new OrderStatusChanged2EventV1(order));
    // + nu mai facem fetch inapoi la sursa dupa date
    // - datele ACUM in sistemul sursa pot fi deja altele ± => si ce daca? chiar daca sun 'in urma cu procesarea',
    //    probabil voi primi mesaje ulterioare care reflecta schimbarea din sistemul sursa,
    //    pe care le voi procesa cu latenta de rigoare. => Eventually consistent la un moment dat
    // - size of the event is bigger;
    // - eventul incepa sa aiba nevoie de schema, exact ca un DTO json response. = JSON Schema sau AVRO
  }
  // !! Atentie: TOATE microserviciile din jurul event streamului importau un events-v43.jar ce continea toate
  // definitiile tuturor evenimentelor de pe Kafka ca clase Java
  // + nu generezi cod, ci doar importi un jar
  // - upgrade constant

  // alternativa:
  // a) JSON Schema publicate undeva din care fiecare app sa-si genereze clasele asoc eventurilor
  // b) AVRO Schema (pentru peformanta mare) -> generezi clase Java din schema
  // c) cate-un jar pentru fiecare event

  // Versionarea eventurilor e mult mai grea decat versionarea DTO-urilor dintr-un REST
  // pt ca eventurile pot ramane mult timp pe topice, in istorie, DLQ/T
  // daca vrei sa replay history (resetezi consumer offsets=0 si revizitezi toate eventurile)

  // de aceea procesatoarele de eventuri trebuie sa fie si
  // > backwards-compatible: sa poata procesa un event v-1 -> if (!=null)...
  // > forwards-compatible: sa poata procesa un event v+1 (campuri noi)

  @RestControllerAdvice
  @Slf4j
  public static class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e) {
      log.error("Error", e);
      return "Error: " + e;
    }
  }
}
