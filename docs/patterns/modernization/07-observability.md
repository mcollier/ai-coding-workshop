# Pattern: Adding OpenTelemetry Observability

**Maturity:** 🟢 Production-Ready  
**Category:** Observability  
**Complexity:** Medium

---

## 1. Overview

Implement distributed tracing, metrics, and structured logging using OpenTelemetry, Micrometer, and SLF4J. This provides visibility into microservices behavior and performance.

**Key Capabilities:**
- **Distributed Tracing:** Track requests across services
- **Metrics Collection:** Monitor application health and performance
- **Log Correlation:** Link logs to traces
- **Dashboard Integration:** Visualize with Grafana, Jaeger, Prometheus

---

## 2. Context

Use this pattern when:
- ✅ You have multiple microservices that need correlation
- ✅ You need to diagnose performance issues
- ✅ You want centralized logging and metrics
- ✅ You need production-ready observability

---

## 3. Before: Limited Observability

Mule ESB has built-in monitoring but limited distributed tracing:
- Flow logs scattered across instances
- No automatic trace context propagation
- Metrics require external APM tools
- Difficult to correlate requests across flows

---

## 4. After: OpenTelemetry Implementation

### Dependencies

**File:** `pom.xml`

```xml
<dependencies>
    <!-- OpenTelemetry -->
    <dependency>
        <groupId>io.opentelemetry.instrumentation</groupId>
        <artifactId>opentelemetry-spring-boot-starter</artifactId>
        <version>2.0.0</version>
    </dependency>
    
    <!-- Micrometer for Metrics -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>
    
    <!-- SLF4J + Logback -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-logging</artifactId>
    </dependency>
    
    <!-- Distributed Tracing -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-tracing-bridge-otel</artifactId>
    </dependency>
</dependencies>
```

### Application Configuration

**File:** `application.yml`

```yaml
spring:
  application:
    name: taskmanager-api

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
  tracing:
    sampling:
      probability: 1.0  # Sample 100% in dev (reduce in prod)

# OpenTelemetry Configuration
otel:
  traces:
    exporter: otlp
  exporter:
    otlp:
      endpoint: http://localhost:4317
  service:
    name: ${spring.application.name}
  resource:
    attributes:
      environment: ${SPRING_PROFILES_ACTIVE:dev}

logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n [traceId=%X{traceId}, spanId=%X{spanId}]"
  level:
    com.centric.taskmanager: INFO
```

### Structured Logging

```java
package com.centric.taskmanager.application.services;

import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final Tracer tracer;
    
    public OrderService(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public Order createOrder(CreateOrderRequest request) {
        // Trace context automatically added to MDC
        logger.info("Creating order for customer: {}", request.customerId());
        
        try {
            Order order = processOrder(request);
            logger.info("Order created successfully: orderId={}", order.getId());
            return order;
        } catch (Exception e) {
            logger.error("Order creation failed: {}", e.getMessage(), e);
            throw e;
        }
    }
}
```

### Custom Span

```java
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

@Service
public class PaymentService {
    private final Tracer tracer;
    
    public PaymentService(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public PaymentResult processPayment(PaymentRequest request) {
        Span span = tracer.nextSpan().name("processPayment").start();
        
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            span.tag("customerId", String.valueOf(request.customerId()));
            span.tag("amount", request.amount().toString());
            
            logger.info("Processing payment for order: {}", request.orderId());
            
            // Business logic
            PaymentResult result = callPaymentGateway(request);
            
            span.tag("paymentStatus", result.status());
            span.event("payment.processed");
            
            return result;
        } catch (Exception e) {
            span.error(e);
            throw e;
        } finally {
            span.end();
        }
    }
}
```

### Custom Metrics

```java
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class OrderMetricsService {
    private final Counter orderCreatedCounter;
    private final Counter orderFailedCounter;
    private final Timer orderProcessingTimer;
    
    public OrderMetricsService(MeterRegistry meterRegistry) {
        this.orderCreatedCounter = meterRegistry.counter("orders.created", "status", "success");
        this.orderFailedCounter = meterRegistry.counter("orders.created", "status", "failed");
        this.orderProcessingTimer = meterRegistry.timer("orders.processing.time");
    }
    
    public Order createOrder(CreateOrderRequest request) {
        return orderProcessingTimer.recordCallable(() -> {
            try {
                Order order = processOrder(request);
                orderCreatedCounter.increment();
                return order;
            } catch (Exception e) {
                orderFailedCounter.increment();
                throw e;
            }
        });
    }
}
```

### Health Indicators

```java
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    private final OrderRepository orderRepository;
    
    public DatabaseHealthIndicator(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    @Override
    public Health health() {
        try {
            long count = orderRepository.count();
            return Health.up()
                .withDetail("orders.count", count)
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

---

## 5. Migration Steps

1. **Add OpenTelemetry dependencies** to pom.xml
2. **Configure OTLP exporter** (Jaeger, Zipkin, or cloud provider)
3. **Add structured logging** with MDC trace context
4. **Create custom metrics** for business KPIs
5. **Add health indicators** for dependencies
6. **Set up Grafana dashboards** for visualization
7. **Configure log aggregation** (ELK stack or cloud logging)

---

## 6. Testing

### Trace Verification

```java
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTracingTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private Tracer tracer;
    
    @Test
    void shouldPropagateTraceContext() throws Exception {
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":1,\"items\":[]}"))
            .andExpect(status().isCreated())
            .andExpect(header().exists("X-B3-TraceId"));  // Trace ID in response
        
        // Verify span was created
        assertThat(tracer.currentSpan()).isNotNull();
    }
}
```

### Metrics Verification

```java
@SpringBootTest
class OrderMetricsTest {
    @Autowired
    private MeterRegistry meterRegistry;
    
    @Autowired
    private OrderService orderService;
    
    @Test
    void shouldIncrementOrderCreatedCounter() {
        double before = meterRegistry.counter("orders.created", "status", "success").count();
        
        orderService.createOrder(validRequest());
        
        double after = meterRegistry.counter("orders.created", "status", "success").count();
        assertThat(after).isEqualTo(before + 1);
    }
}
```

---

## 7. Gotchas

### ❌ Sampling Rate Too Low
In production, don't sample below 10% or you'll miss critical traces.

### ❌ Missing Context Propagation
Ensure trace context propagates across HTTP calls (use WebClient with tracing).

### ❌ Too Many Custom Spans
Only create spans for significant operations (>10ms).

### ❌ High Cardinality Metrics
Don't use customer IDs or order IDs as metric tags (use status, region instead).

---

## 8. Related Patterns

- **[Flow Transformation](./01-flow-transformation.md)** - Adding tracing to services
- **[Integration HTTP](./03-integration-http.md)** - Trace propagation across services
- **[Error Handling](./05-error-handling.md)** - Error tracking in traces

---

**Observability Stack:**
- **Tracing:** OpenTelemetry → Jaeger/Tempo
- **Metrics:** Micrometer → Prometheus → Grafana
- **Logging:** SLF4J/Logback → ELK Stack or Loki
- **APM:** Datadog, New Relic, or Dynatrace

---

**Version:** 1.0  
**Last Updated:** April 2, 2026
