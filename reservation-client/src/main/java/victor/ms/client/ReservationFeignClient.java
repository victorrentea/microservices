package victor.ms.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "reservation-service")
public interface ReservationFeignClient {
  @GetMapping("reservations")
  List<ReservationDto> getAllReservations();

  @GetMapping("flaky")
  String flaky();

  @GetMapping("flaky-slow")
  String flakySlow();
}
