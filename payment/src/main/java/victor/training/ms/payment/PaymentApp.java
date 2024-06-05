package victor.training.ms.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.bind.annotation.ExceptionHandler;
import victor.training.ms.shared.OrderStatusChangedEvent;

import java.util.Map;

@SpringBootApplication
@EnableFeignClients
@RequiredArgsConstructor
public class PaymentApp {
  public static void main(String[] args) {
    SpringApplication.run(PaymentApp.class, args);
  }
}
