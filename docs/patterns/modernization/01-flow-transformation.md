# Pattern: Mule Flow → Spring Boot Service

**Maturity:** 🟢 Production-Ready  
**Category:** Flow Transformation  
**Complexity:** Medium

---

## 1. Overview

Transform Mule ESB XML flows into Spring Boot service classes following Clean Architecture principles. This pattern maps Mule's flow-based orchestration to Spring's dependency injection and service layer patterns.

**Key Transformation:**
- **From:** Declarative XML flow with connectors and transformations
- **To:** Java service class with injected dependencies and explicit business logic

---

## 2. Context

Use this pattern when:
- ✅ You have a Mule flow that orchestrates business logic
- ✅ The flow contains multiple steps (database access, transformations, external calls)
- ✅ You want to maintain testability and separation of concerns
- ✅ The flow's logic belongs in the Application layer (use cases/services)

Do **not** use this pattern when:
- ❌ The flow is purely a simple HTTP proxy (use Spring Cloud Gateway instead)
- ❌ The flow logic belongs in the Domain layer (use domain services/entities)
- ❌ The flow is only configuration (use Spring configuration classes)

---

## 3. Before: Mule ESB Flow

**File:** `order-processing-flow.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<mule xmlns="http://www.mulesoft.org/schema/mule/core"
      xmlns:http="http://www.mulesoft.org/schema/mule/http"
      xmlns:db="http://www.mulesoft.org/schema/mule/db"
      xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core">

    <flow name="processOrderFlow">
        <!-- HTTP listener receives request -->
        <http:listener config-ref="HTTP_Listener_config" 
                       path="/orders" 
                       allowedMethods="POST"/>
        
        <!-- Validate input -->
        <validation:validate-size value="#[payload.items]" 
                                  min="1" 
                                  message="Order must contain at least one item"/>
        
        <!-- Transform to internal format -->
        <ee:transform doc:name="Map to Order Request">
            <ee:message>
                <ee:set-payload><![CDATA[%dw 2.0
                output application/java
                ---
                {
                    customerId: payload.customer_id,
                    items: payload.items map {
                        productId: $.product_id,
                        quantity: $.qty
                    },
                    totalAmount: sum(payload.items.*amount)
                }]]></ee:set-payload>
            </ee:message>
        </ee:transform>
        
        <!-- Check inventory -->
        <db:select config-ref="Database_Config">
            <db:sql>
                SELECT product_id, available_quantity 
                FROM inventory 
                WHERE product_id IN (:productIds)
            </db:sql>
            <db:input-parameters>
                #[{'productIds': payload.items.*productId}]
            </db:input-parameters>
        </db:select>
        
        <!-- Store payload for later -->
        <set-variable variableName="orderRequest" value="#[payload]"/>
        
        <!-- Create order -->
        <db:insert config-ref="Database_Config">
            <db:sql>
                INSERT INTO orders (customer_id, total_amount, status, created_at)
                VALUES (:customerId, :totalAmount, 'PENDING', NOW())
            </db:sql>
            <db:input-parameters>
                #[[
                    'customerId': vars.orderRequest.customerId,
                    'totalAmount': vars.orderRequest.totalAmount
                ]]
            </db:input-parameters>
        </db:insert>
        
        <!-- Get generated order ID -->
        <set-variable variableName="orderId" value="#[payload.generatedKeys.id]"/>
        
        <!-- Call payment service -->
        <http:request config-ref="Payment_Service_Config" 
                      path="/payments" 
                      method="POST">
            <http:body><![CDATA[#[{
                orderId: vars.orderId,
                customerId: vars.orderRequest.customerId,
                amount: vars.orderRequest.totalAmount
            }]]]></http:body>
        </http:request>
        
        <!-- Return response -->
        <ee:transform doc:name="Build Response">
            <ee:message>
                <ee:set-payload><![CDATA[%dw 2.0
                output application/json
                ---
                {
                    orderId: vars.orderId,
                    status: "PENDING",
                    paymentStatus: payload.status
                }]]></ee:set-payload>
            </ee:message>
        </ee:transform>
        
        <!-- Error handling -->
        <error-handler>
            <on-error-propagate type="DB:CONNECTIVITY">
                <set-payload value="#[{error: 'Database unavailable'}]"/>
                <set-variable variableName="httpStatus" value="503"/>
            </on-error-propagate>
            <on-error-propagate type="HTTP:TIMEOUT">
                <set-payload value="#[{error: 'Payment service timeout'}]"/>
                <set-variable variableName="httpStatus" value="504"/>
            </on-error-propagate>
        </error-handler>
    </flow>
</mule>
```

**Characteristics:**
- Declarative XML configuration
- Embedded DataWeave transformations
- Database access mixed with business logic
- HTTP calls inline in flow
- Flow variables for state management

---

## 4. After: Spring Boot Service

### Application Layer (Use Case)

**File:** `src/taskmanager-application/src/main/java/com/centric/taskmanager/application/services/OrderService.java`

```java
package com.centric.taskmanager.application.services;

import com.centric.taskmanager.application.dto.CreateOrderRequest;
import com.centric.taskmanager.application.dto.OrderResponse;
import com.centric.taskmanager.application.exceptions.InsufficientInventoryException;
import com.centric.taskmanager.application.ports.out.OrderRepository;
import com.centric.taskmanager.application.ports.out.InventoryRepository;
import com.centric.taskmanager.application.ports.out.PaymentServiceClient;
import com.centric.taskmanager.domain.Order;
import com.centric.taskmanager.domain.OrderItem;
import com.centric.taskmanager.domain.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final PaymentServiceClient paymentServiceClient;
    
    public OrderService(OrderRepository orderRepository,
                        InventoryRepository inventoryRepository,
                        PaymentServiceClient paymentServiceClient) {
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
        this.paymentServiceClient = paymentServiceClient;
    }
    
    @Transactional
    public OrderResponse processOrder(CreateOrderRequest request) {
        logger.info("Processing order for customer: {}", request.customerId());
        
        // 1. Validate input
        validateOrderRequest(request);
        
        // 2. Check inventory availability
        List<Long> productIds = extractProductIds(request);
        Map<Long, Integer> availability = inventoryRepository.getAvailability(productIds);
        ensureSufficientInventory(request.items(), availability);
        
        // 3. Create order entity
        Order order = createOrderFromRequest(request);
        Order savedOrder = orderRepository.save(order);
        
        // 4. Process payment
        PaymentResponse payment = paymentServiceClient.processPayment(
            savedOrder.getId(),
            request.customerId(),
            order.getTotalAmount()
        );
        
        // 5. Build response
        return new OrderResponse(
            savedOrder.getId(),
            OrderStatus.PENDING,
            payment.status()
        );
    }
    
    private void validateOrderRequest(CreateOrderRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
    }
    
    private List<Long> extractProductIds(CreateOrderRequest request) {
        return request.items().stream()
            .map(CreateOrderRequest.OrderItemRequest::productId)
            .collect(Collectors.toList());
    }
    
    private void ensureSufficientInventory(List<CreateOrderRequest.OrderItemRequest> items,
                                           Map<Long, Integer> availability) {
        for (var item : items) {
            Integer available = availability.get(item.productId());
            if (available == null || available < item.quantity()) {
                throw new InsufficientInventoryException(
                    String.format("Insufficient inventory for product %d", item.productId())
                );
            }
        }
    }
    
    private Order createOrderFromRequest(CreateOrderRequest request) {
        List<OrderItem> items = request.items().stream()
            .map(item -> new OrderItem(item.productId(), item.quantity(), item.price()))
            .collect(Collectors.toList());
        
        BigDecimal totalAmount = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new Order(request.customerId(), items, totalAmount);
    }
}
```

### API Layer (Controller)

**File:** `src/taskmanager-api/src/main/java/com/centric/taskmanager/api/controllers/OrderController.java`

```java
package com.centric.taskmanager.api.controllers;

import com.centric.taskmanager.application.dto.CreateOrderRequest;
import com.centric.taskmanager.application.dto.OrderResponse;
import com.centric.taskmanager.application.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.processOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

**Characteristics:**
- Explicit Java code (type-safe, IDE-supported)
- Clear separation: Controller → Service → Repository
- Constructor-based dependency injection
- Declarative transactions with @Transactional
- Business logic in readable methods

---

## 5. Migration Steps

### Step 1: Identify Flow Boundaries

1. **Analyze the Mule flow** to identify:
   - Entry point (HTTP listener, message queue, scheduler)
   - External dependencies (databases, HTTP services)
   - Business logic steps
   - Error handling requirements

2. **Map to Clean Architecture layers:**
   - HTTP listener → API layer (Controller)
   - Business orchestration → Application layer (Service)
   - Entity logic → Domain layer
   - External calls → Infrastructure layer (adapters)

### Step 2: Create Service Interface

```java
public interface OrderService {
    OrderResponse processOrder(CreateOrderRequest request);
}
```

### Step 3: Define DTOs

Extract request/response structures from DataWeave transformations:

```java
public record CreateOrderRequest(
    @NotNull Long customerId,
    @NotEmpty List<OrderItemRequest> items
) {
    public record OrderItemRequest(
        @NotNull Long productId,
        @Positive Integer quantity,
        @NotNull BigDecimal price
    ) {}
}
```

### Step 4: Create Repository Interfaces

```java
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);
}
```

### Step 5: Implement Service

- Extract each Mule step into a method
- Replace DataWeave with Java transformation logic
- Use repositories instead of inline DB queries
- Inject HTTP clients instead of inline requests

### Step 6: Add Error Handling

Create custom exceptions for business errors:

```java
public class InsufficientInventoryException extends RuntimeException {
    public InsufficientInventoryException(String message) {
        super(message);
    }
}
```

Map to HTTP status codes in @ControllerAdvice (see Pattern #5).

### Step 7: Add Tests

Write service tests before implementation (TDD):

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock private OrderRepository orderRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private PaymentServiceClient paymentServiceClient;
    
    @InjectMocks
    private OrderService orderService;
    
    @Test
    void shouldProcessValidOrder() {
        // Given
        var request = new CreateOrderRequest(/* ... */);
        when(inventoryRepository.getAvailability(anyList()))
            .thenReturn(Map.of(1L, 100));
        when(orderRepository.save(any())).thenReturn(savedOrder);
        when(paymentServiceClient.processPayment(any(), any(), any()))
            .thenReturn(new PaymentResponse("AUTHORIZED"));
        
        // When
        OrderResponse response = orderService.processOrder(request);
        
        // Then
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository).save(any(Order.class));
    }
}
```

---

## 6. Testing

### Unit Tests (Service Layer)

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    // Test business logic in isolation with mocked dependencies
    
    @Test
    void shouldThrowExceptionWhenInventoryInsufficient() {
        // Given insufficient inventory
        when(inventoryRepository.getAvailability(anyList()))
            .thenReturn(Map.of(1L, 5));
        
        var request = new CreateOrderRequest(1L, List.of(
            new OrderItemRequest(1L, 10, BigDecimal.TEN)
        ));
        
        // When/Then
        assertThrows(InsufficientInventoryException.class, 
            () -> orderService.processOrder(request));
    }
}
```

### Integration Tests (API Layer)

```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class OrderControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    
    @Test
    void shouldCreateOrderSuccessfully() throws Exception {
        String requestJson = """
            {
                "customerId": 1,
                "items": [
                    {"productId": 1, "quantity": 2, "price": 29.99}
                ]
            }
            """;
        
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").exists())
            .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
```

---

## 7. Gotchas

### ❌ Pitfall 1: Anemic Domain Model

**Problem:** Service contains all business logic, domain entities are just data holders.

```java
// BAD: Anemic Order entity
public class Order {
    private Long id;
    private BigDecimal totalAmount;
    // Only getters/setters, no behavior
}

// Business logic in service
public class OrderService {
    public Order calculateTotal(Order order) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            total = total.add(item.getPrice().multiply(item.getQuantity()));
        }
        order.setTotalAmount(total);
        return order;
    }
}
```

**Solution:** Move business logic into domain entities.

```java
// GOOD: Rich domain model
public class Order {
    private final Long id;
    private final List<OrderItem> items;
    private BigDecimal totalAmount;
    
    public Order(Long customerId, List<OrderItem> items) {
        this.items = new ArrayList<>(items);
        this.totalAmount = calculateTotal(); // Business logic in domain
    }
    
    private BigDecimal calculateTotal() {
        return items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

### ❌ Pitfall 2: Mixing Concerns in Service

**Problem:** Service handles HTTP details, validation, persistence, and business logic.

**Solution:** Use layered responsibilities:
- Controller: HTTP concerns (status codes, headers)
- Service: Business orchestration
- Domain: Business rules
- Repository: Persistence

### ❌ Pitfall 3: Lost Transaction Boundaries

**Problem:** Mule flows have implicit transaction contexts; Spring requires explicit `@Transactional`.

**Solution:** Add `@Transactional` to service methods that modify data:

```java
@Transactional
public OrderResponse processOrder(CreateOrderRequest request) {
    // All repository operations in one transaction
}
```

### ❌ Pitfall 4: Direct Entity Exposure

**Problem:** Returning domain entities directly from controllers couples API to domain.

**Solution:** Always use DTOs:

```java
// BAD
@GetMapping("/{id}")
public Order getOrder(@PathVariable Long id) {
    return orderRepository.findById(id); // Exposes internal structure
}

// GOOD
@GetMapping("/{id}")
public OrderResponse getOrder(@PathVariable Long id) {
    Order order = orderRepository.findById(id);
    return OrderMapper.toResponse(order); // DTO mapping
}
```

---

## 8. Related Patterns

- **[Data Access: Stored Procedures → Repository](./02-data-access-stored-procedures.md)** - For database operations
- **[Integration: HTTP Request → WebClient](./03-integration-http.md)** - For external service calls
- **[Transformation: DataWeave → Java](./04-transformation-dataweave.md)** - For payload transformations
- **[Error Handling](./05-error-handling.md)** - For exception management
- **[Configuration](./06-configuration.md)** - For externalizing Mule properties

---

**References:**
- Clean Architecture by Robert C. Martin
- Domain-Driven Design by Eric Evans
- Spring Framework Documentation: [Core - IoC Container](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html)

**Version:** 1.0  
**Last Updated:** April 2, 2026
