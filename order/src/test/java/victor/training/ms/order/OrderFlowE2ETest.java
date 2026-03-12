package victor.training.ms.order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Black-box E2E test for the full order flow, running against the deployed stack.
 * Targets the API Gateway at http://localhost (Docker Desktop K8s default).
 * Override with -Dbase.url=http://... to point to another environment.
 *
 * Flow:
 *   POST /order  →  PUT /payment/{id}/status  →  PUT /shipping/{id}/status
 */
class OrderFlowE2ETest {

    private static final String BASE_URL = System.getProperty("base.url", "http://localhost");
    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(20);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(500);

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();

    @Test
    void fullOrderFlow() throws Exception {
        // ── 1. Place order ────────────────────────────────────────────────────────
        String placeOrderBody = """
                {"customerId":"margareta","items":[{"productId":1,"count":1}],"shippingAddress":"Str. Florilor 1"}
                """;
        String paymentUrl = post("/order", placeOrderBody);
        System.out.println("Payment URL: " + paymentUrl);

        long orderId = extractOrderId(paymentUrl);
        System.out.println("Order ID: " + orderId);

        assertOrderStatus(orderId, "AWAITING_PAYMENT");

        // ── 2. Simulate payment gateway callback ──────────────────────────────────
        // Note: PaymentResultEventListener processes payment AND requests shipping atomically,
        // so the status jumps directly from AWAITING_PAYMENT to SHIPPING_IN_PROGRESS.
        String paymentAck = put("/payment/" + orderId + "/status", "true");
        System.out.println("Payment ack: " + paymentAck);

        awaitOrderStatus(orderId, "SHIPPING_IN_PROGRESS");

        // ── 3. Simulate shipping provider callback ────────────────────────────────
        String shippingAck = put("/shipping/" + orderId + "/status", "true");
        System.out.println("Shipping ack: " + shippingAck);

        awaitOrderStatus(orderId, "SHIPPING_COMPLETED");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────

    private long extractOrderId(String paymentUrl) {
        int idx = paymentUrl.lastIndexOf("orderId=");
        assertThat(idx).as("Response must contain 'orderId=': " + paymentUrl).isGreaterThanOrEqualTo(0);
        String tail = paymentUrl.substring(idx + "orderId=".length()).replaceAll("[^0-9].*", "");
        return Long.parseLong(tail);
    }

    private void assertOrderStatus(long orderId, String expectedStatus) throws Exception {
        String status = fetchOrderStatus(orderId);
        assertThat(status).isEqualTo(expectedStatus);
    }

    private void awaitOrderStatus(long orderId, String expectedStatus) throws Exception {
        Instant deadline = Instant.now().plus(POLL_TIMEOUT);
        while (true) {
            String status = fetchOrderStatus(orderId);
            if (expectedStatus.equals(status)) {
                System.out.println("Order " + orderId + " reached status: " + status);
                return;
            }
            if (Instant.now().isAfter(deadline)) {
                fail("Timed out waiting for order %d to reach status %s. Last status: %s"
                        .formatted(orderId, expectedStatus, status));
            }
            Thread.sleep(POLL_INTERVAL.toMillis());
        }
    }

    private String fetchOrderStatus(long orderId) throws Exception {
        String body = get("/order/" + orderId);
        JsonNode node = json.readTree(body);
        return node.get("status").asText();
    }

    private String get(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .GET()
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        assertThat(res.statusCode()).as("GET %s returned %d: %s", path, res.statusCode(), res.body())
                .isBetween(200, 299);
        return res.body();
    }

    private String post(String path, String jsonBody) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        assertThat(res.statusCode()).as("POST %s returned %d: %s", path, res.statusCode(), res.body())
                .isBetween(200, 299);
        return res.body();
    }

    private String put(String path, String jsonBody) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        assertThat(res.statusCode()).as("PUT %s returned %d: %s", path, res.statusCode(), res.body())
                .isBetween(200, 299);
        return res.body();
    }
}
