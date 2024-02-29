package victor.training.ms.customer;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepo extends JpaRepository<Customer, String> {
//  @Bulkhead(name = "fat-query"
////      ,type = Bulkhead.Type.SEMAPHORE
//       // you let the execution in the same thread, block that
//
////      type = Bulkhead.Type.THREADPOOL
//      // create a dedicated pool with 2 threds and run
//      // your query async in one of those 2 threads
//
//
//      // if you want to limit the total concurrency across 2 instances of your app.
//      //a)  manually coordinate all clients via a DB/Redis persistent thing https://redis.com/ebook/part-2-core-concepts/chapter-6-application-components-in-redis/6-3-counting-semaphores/
//      //b) put a protective intermediary api gateway (1 instance) in front of the fragile system.
//
//  )
//  List<tot> exportuDe10GBDeFinalDeLuna();
}
