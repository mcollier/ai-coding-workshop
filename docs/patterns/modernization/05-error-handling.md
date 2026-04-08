# Pattern: Mule Error Handling → Spring Exception Handling

**Maturity:** 🟢 Production-Ready  
**Category:** Error Handling  
**Complexity:** Low

---

## 1. Overview

Standardize error responses using Spring's `@ControllerAdvice` and RFC 7807 Problem Details. This replaces Mule's error handlers with centralized exception handling.

**Key Transformation:**
- **From:** Mule error handlers in XML flows
- **To:** Spring @ControllerAdvice with custom exceptions

---

## 2. Context

Use this pattern when:
- ✅ You need consistent error response format
- ✅ You want centralized exception handling
- ✅ You need to map domain exceptions to HTTP status codes
- ✅ API clients expect standardized error responses (RFC 7807)

---

## 3. Before: Mule Error Handling

```xml
<flow name="orderProcessingFlow">
    <http:listener path="/orders" method="POST"/>
    
    <!-- Business logic -->
    <flow-ref name="validateOrderSubflow"/>
    <flow-ref name="createOrderSubflow"/>
    
    <error-handler>
        <on-error-propagate type="VALIDATION:INVALID">
            <set-payload value='#[{"error": "Invalid order data", "details": error.description}]'/>
            <set-variable variableName="httpStatus" value="400"/>
        </on-error-propagate>
        
        <on-error-propagate type="DB:CONNECTIVITY">
            <set-payload value='#[{"error": "Database unavailable"}]'/>
            <set-variable variableName="httpStatus" value="503"/>
            <logger level="ERROR" message="Database connection failed: #[error.description]"/>
        </on-error-propagate>
        
        <on-error-propagate type="HTTP:TIMEOUT">
            <set-payload value='#[{"error": "External service timeout"}]'/>
            <set-variable variableName="httpStatus" value="504"/>
        </on-error-propagate>
        
        <on-error-propagate type="ANY">
            <set-payload value='#[{"error": "Internal server error"}]'/>
            <set-variable variableName="httpStatus" value="500"/>
        </on-error-propagate>
    </error-handler>
</flow>
```

---

## 4. After: Spring Boot Exception Handling

### Custom Exceptions

```java
package com.centric.taskmanager.application.exceptions;

public class OrderValidationException extends RuntimeException {
    public OrderValidationException(String message) {
        super(message);
    }
}

public class ResourceNotFoundException extends RuntimeException {
    private final Long resourceId;
    
    public ResourceNotFoundException(String resourceType, Long resourceId) {
        super(String.format("%s with id %d not found", resourceType, resourceId));
        this.resourceId = resourceId;
    }
    
    public Long getResourceId() {
        return resourceId;
    }
}

public class ExternalServiceException extends RuntimeException {
    private final String serviceName;
    
    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
    }
    
    public String getServiceName() {
        return serviceName;
    }
}
```

### Global Exception Handler

```java
package com.centric.taskmanager.api.exceptions;

import com.centric.taskmanager.application.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(OrderValidationException.class)
    public ResponseEntity<ProblemDetail> handleOrderValidation(
            OrderValidationException ex, WebRequest request) {
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setType(URI.create("https://api.example.com/problems/validation-error"));
        problemDetail.setTitle("Order Validation Failed");
        problemDetail.setProperty("timestamp", Instant.now());
        
        logger.warn("Order validation failed: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(problemDetail);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
        );
        problemDetail.setType(URI.create("https://api.example.com/problems/not-found"));
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setProperty("resourceId", ex.getResourceId());
        
        logger.info("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }
    
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ProblemDetail> handleExternalService(
            ExternalServiceException ex, WebRequest request) {
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.SERVICE_UNAVAILABLE,
            "External service unavailable"
        );
        problemDetail.setType(URI.create("https://api.example.com/problems/service-unavailable"));
        problemDetail.setTitle("Service Unavailable");
        problemDetail.setProperty("serviceName", ex.getServiceName());
        
        logger.error("External service error ({}): {}", ex.getServiceName(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problemDetail);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(
            Exception ex, WebRequest request) {
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred"
        );
        problemDetail.setType(URI.create("https://api.example.com/problems/internal-error"));
        problemDetail.setTitle("Internal Server Error");
        
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }
}
```

### Usage in Service

```java
@Service
public class OrderService {
    
    public Order getOrder(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }
    
    public Order createOrder(CreateOrderRequest request) {
        if (request.items().isEmpty()) {
            throw new OrderValidationException("Order must contain at least one item");
        }
        // Business logic
    }
}
```

---

## 5. Migration Steps

1. **Identify error types** from Mule error handlers
2. **Create custom exception classes** for each business error
3. **Implement @ControllerAdvice** with @ExceptionHandler methods
4. **Map exceptions to HTTP status codes**
5. **Use RFC 7807 ProblemDetail** for consistent responses
6. **Add logging** at appropriate levels (WARN, ERROR)

---

## 6. Testing

```java
@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private OrderService orderService;
    
    @Test
    void shouldReturn404WhenOrderNotFound() throws Exception {
        when(orderService.getOrder(999L))
            .thenThrow(new ResourceNotFoundException("Order", 999L));
        
        mockMvc.perform(get("/orders/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Resource Not Found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("Order with id 999 not found"))
            .andExpect(jsonPath("$.resourceId").value(999));
    }
    
    @Test
    void shouldReturn400WhenOrderValidationFails() throws Exception {
        when(orderService.createOrder(any()))
            .thenThrow(new OrderValidationException("Order must contain at least one item"));
        
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":1,\"items\":[]}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Order Validation Failed"));
    }
}
```

---

## 7. Gotchas

### ❌ Exposing Sensitive Information
Don't leak stack traces or internal details to clients.

### ❌ Generic Error Messages
Provide actionable error messages, not "An error occurred."

### ❌ Logging Passwords/Tokens
Sanitize logs before recording exception details.

---

## 8. Related Patterns

- **[Flow Transformation](./01-flow-transformation.md)** - Service layer exceptions
- **[Integration HTTP](./03-integration-http.md)** - External service errors
- **[Observability](./07-observability.md)** - Error correlation and tracing

---

**Version:** 1.0  
**Last Updated:** April 2, 2026
