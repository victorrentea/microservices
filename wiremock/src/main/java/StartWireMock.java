import com.github.tomakehurst.wiremock.standalone.WireMockServerRunner;


import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class StartWireMock {
  public static void main(String[] args) throws IOException {
    File rootFolder = new File(".", "wiremock");
    File mappingsFolder = new File(rootFolder, "mappings");
    System.out.println("*.json mappings stubs expected at " + mappingsFolder.getAbsolutePath());

    CompletableFuture.runAsync(() -> System.out.println(
        "You should see some response at http://localhost:9999/get-payment-link/123"),
        CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS));

    new WireMockServerRunner().run(
            "--port", "9999",
            "--root-dir", rootFolder.getAbsolutePath(),
            "--global-response-templating", // UUID
            "--async-response-enabled=true" // enable Wiremock to not bottleneck on heavy load
    );
  }
}
