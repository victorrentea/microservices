package victor.training.ms.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "reservation-service")
public interface ReservationFeignClient { // similar to Spring Data
  @GetMapping("reservations")
  List<ReservationDto> getAllReservations();

  @GetMapping("flaky")
  String flaky();

  @GetMapping("slow")
  String slow();

  @GetMapping("flaky-slow")
  String flakySlow();
}
