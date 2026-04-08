# Pattern: HTTP Request → RestTemplate/WebClient

**Maturity:** 🟢 Production-Ready  
**Category:** Integration  
**Complexity:** Medium

---

## 1. Overview

Migrate Mule HTTP connectors to Spring's REST client stack (WebClient for reactive or RestTemplate for blocking). This pattern covers synchronous and asynchronous HTTP communication with circuit breakers and error handling.

**Key Transformation:**
- **From:** Mule HTTP request connector with XML configuration
- **To:** Spring WebClient/RestTemplate with @Configuration beans

---

## 2. Context

Use this pattern when:
- ✅ You need to call external REST APIs
- ✅ You want resilience patterns (circuit breakers, retries)
- ✅ Non-blocking I/O is beneficial (use WebClient)
- ✅ You need consistent error handling across services

Use **WebClient** for reactive/non-blocking. Use **RestTemplate** for simple blocking calls.

---

## 3. Before: Mule HTTP Connector

```xml
<http:request-config name="Payment_Service_Config">
    <http:request-connection host="payment-service.internal" port="8080"/>
</http:request-config>

<flow name="processPaymentFlow">
    <http:request config-ref="Payment_Service_Config" 
                  path="/payments" 
                  method="POST">
        <http:body><![CDATA[#[{
            orderId: vars.orderId,
            amount: payload.totalAmount
        }]]]></http:body>
        <http:headers>
            #[{'Content-Type': 'application/json', 'X-Trace-Id': correlationId}]
        </http:headers>
        <http:response-validator>
            <http:success-status-code-validator values="200,201"/>
        </http:response-validator>
    </http:request>
</flow>
```

---

## 4. After: Spring Boot WebClient

###Configuration

```java
@Configuration
public class WebClientConfig {
    
    @Bean
    public WebClient paymentServiceClient(
            @Value("${services.payment.base-url}") String baseUrl) {
        return WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}
```

### Client Interface

```java
@Component
public class PaymentServiceClient {
    private final WebClient webClient;
    
    public PaymentServiceClient(@Qualifier("paymentServiceClient") WebClient webClient) {
        this.webClient = webClient;
    }
    
    public Mono<PaymentResponse> processPayment(PaymentRequest request) {
        return webClient.post()
            .uri("/payments")
            .header("X-Trace-Id", MDC.get("traceId"))
            .bodyValue(request)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, response ->
                response.bodyToMono(ErrorResponse.class)
                    .flatMap(error -> Mono.error(new PaymentClientException(error))))
            .bodyToMono(PaymentResponse.class)
            .timeout(Duration.ofSeconds(5))
            .retryWhen(Retry.backoff(3, Duration.ofMillis(100)));
    }
}
```

### With Resilience4j Circuit Breaker

```java
@Component
public class PaymentServiceClient {
    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;
    
    public PaymentServiceClient(WebClient webClient, CircuitBreakerFactory circuitBreakerFactory) {
        this.webClient = webClient;
        this.circuitBreaker = circuitBreakerFactory.create("paymentService");
    }
    
    public PaymentResponse processPaymentBlocking(PaymentRequest request) {
        return circuitBreaker.run(
            () -> webClient.post()
                .uri("/payments")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                .block(),
            throwable -> fallbackPaymentResponse()
        );
    }
    
    private PaymentResponse fallbackPaymentResponse() {
        return new PaymentResponse("PENDING", "Service unavailable - queued for retry");
    }
}
```

---

## 5. Migration Steps

1. **Extract HTTP configuration** from Mule to application.yml
2. **Create WebClient bean** with base URL and defaults
3. **Implement client interface** with methods per endpoint
4. **Add error handling** with `onStatus()` and custom exceptions
5. **Add resilience** with circuit breakers and retries
6. **Test with WireMock** for HTTP mocking

---

## 6. Testing

```java
@SpringBootTest
class PaymentServiceClientIntegrationTest {
    @Autowired
    private PaymentServiceClient client;
    
    private WireMockServer wireMockServer;
    
    @BeforeEach
    void setup() {
        wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8080);
    }
    
    @Test
    void shouldProcessPaymentSuccessfully() {
        stubFor(post(urlEqualTo("/payments"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\":\"AUTHORIZED\"}")));
        
        PaymentResponse response = client.processPaymentBlocking(
            new PaymentRequest(1L, BigDecimal.valueOf(100))
        );
        
        assertThat(response.status()).isEqualTo("AUTHORIZED");
    }
    
    @AfterEach
    void teardown() {
        wireMockServer.stop();
    }
}
```

---

## 7. Gotchas

### ❌ Block on Non-Reactive Context
Using `.block()` in reactive flows defeats the purpose of WebClient.

### ❌ Missing Timeouts
Always set explicit timeouts; default is infinite.

### ❌ No Circuit Breaker
External service failures can cascade without circuit breakers.

---

## 8. Related Patterns

- **[Flow Transformation](./01-flow-transformation.md)** - Service integration
- **[Error Handling](./05-error-handling.md)** - HTTP error responses
- **[Observability](./07-observability.md)** - Distributed tracing

---

**Version:** 1.0  
**Last Updated:** April 2, 2026
