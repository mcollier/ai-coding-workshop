# Pattern: Stored Procedures → Repository Pattern

**Maturity:** 🟢 Production-Ready  
**Category:** Data Access  
**Complexity:** High

---

## 1. Overview

Eliminate stored procedures in favor of Spring Data JPA repositories with domain-driven design. This pattern moves business logic from the database layer into the application and domain layers, improving testability, portability, and maintainability.

**Key Transformation:**
- **From:** Mule DB connector calling stored procedures with complex SQL logic
- **To:** Spring Data JPA repositories with query methods and domain entities

---

## 2. Context

Use this pattern when:
- ✅ Stored procedures contain business logic that belongs in the application
- ✅ You want to make business logic testable without database dependencies
- ✅ You need database portability (stored procs are vendor-specific)
- ✅ The stored procedure performs CRUD operations with simple transformations

Do **not** use this pattern when:
- ❌ Stored procedure performs complex analytics (use native queries or reporting tools)
- ❌ Procedure has deep dependencies on database-specific features
- ❌ High-performance batch processing requires SQL optimization (consider hybrid approach)

---

## 3. Before: Mule ESB with Stored Procedure

### Mule Flow

**File:** `get-customer-orders-flow.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<mule xmlns:db="http://www.mulesoft.org/schema/mule/db">
    
    <flow name="getCustomerOrdersFlow">
        <http:listener path="/customers/{customerId}/orders" method="GET"/>
        
        <!-- Call stored procedure -->
        <db:stored-procedure config-ref="Database_Config">
            <db:sql>CALL sp_GetCustomerOrders(:customerId, :startDate, :endDate, :status)</db:sql>
            <db:input-parameters>
                #[{
                    'customerId': attributes.uriParams.customerId,
                    'startDate': attributes.queryParams.startDate,
                    'endDate': attributes.queryParams.endDate,
                    'status': attributes.queryParams.status default 'ALL'
                }]
            </db:input-parameters>
            <db:output-parameters>
                <db:output-parameter key="result" type="CURSOR"/>
                <db:output-parameter key="totalCount" type="INTEGER"/>
            </db:output-parameters>
        </db:stored-procedure>
        
        <!-- Transform result -->
        <ee:transform>
            <ee:message>
                <ee:set-payload><![CDATA[%dw 2.0
                output application/json
                ---
                {
                    orders: payload.result map {
                        orderId: $.ORDER_ID,
                        orderDate: $.ORDER_DATE,
                        status: $.STATUS,
                        totalAmount: $.TOTAL_AMOUNT,
                        items: $.ITEMS splitBy "," map {
                            productName: $,
                            quantity: $.QUANTITY
                        }
                    },
                    totalCount: payload.totalCount
                }]]></ee:set-payload>
            </ee:message>
        </ee:transform>
    </flow>
</mule>
```

### Stored Procedure (PostgreSQL)

**File:** `sp_GetCustomerOrders.sql`

```sql
CREATE OR REPLACE FUNCTION sp_GetCustomerOrders(
    p_customer_id BIGINT,
    p_start_date DATE,
    p_end_date DATE,
    p_status VARCHAR
)
RETURNS TABLE (
    ORDER_ID BIGINT,
    ORDER_DATE TIMESTAMP,
    STATUS VARCHAR,
    TOTAL_AMOUNT DECIMAL(10,2),
    ITEMS TEXT,
    QUANTITY INTEGER
) AS $$
BEGIN
    -- Business logic in database
    IF p_status = 'ALL' THEN
        RETURN QUERY
        SELECT 
            o.id AS ORDER_ID,
            o.created_at AS ORDER_DATE,
            o.status AS STATUS,
            o.total_amount AS TOTAL_AMOUNT,
            STRING_AGG(oi.product_name, ', ') AS ITEMS,
            SUM(oi.quantity) AS QUANTITY
        FROM orders o
        INNER JOIN order_items oi ON o.id = oi.order_id
        WHERE o.customer_id = p_customer_id
          AND o.created_at BETWEEN p_start_date AND p_end_date
        GROUP BY o.id, o.created_at, o.status, o.total_amount
        ORDER BY o.created_at DESC;
    ELSE
        RETURN QUERY
        SELECT 
            o.id AS ORDER_ID,
            o.created_at AS ORDER_DATE,
            o.status AS STATUS,
            o.total_amount AS TOTAL_AMOUNT,
            STRING_AGG(oi.product_name, ', ') AS ITEMS,
            SUM(oi.quantity) AS QUANTITY
        FROM orders o
        INNER JOIN order_items oi ON o.id = oi.order_id
        WHERE o.customer_id = p_customer_id
          AND o.created_at BETWEEN p_start_date AND p_end_date
          AND o.status = p_status
        GROUP BY o.id, o.created_at, o.status, o.total_amount
        ORDER BY o.created_at DESC;
    END IF;
END;
$$ LANGUAGE plpgsql;
```

**Characteristics:**
- Business logic trapped in database
- Hard to test (requires database)
- Vendor-specific syntax (PostgreSQL)
- Difficult to version control effectively
- Performance tuning requires DBA expertise

---

## 4. After: Spring Boot with JPA Repository

### Domain Entity

**File:** `src/taskmanager-domain/src/main/java/com/centric/taskmanager/domain/Order.java`

```java
package com.centric.taskmanager.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    
    protected Order() {} // JPA requirement
    
    public Order(Long customerId, List<OrderItem> items, BigDecimal totalAmount) {
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
        this.totalAmount = totalAmount;
        this.status = OrderStatus.PENDING;
        this.createdAt = Instant.now();
        
        // Bi-directional relationship
        this.items.forEach(item -> item.setOrder(this));
    }
    
    // Business logic methods
    public boolean isCompletable() {
        return this.status == OrderStatus.PENDING || this.status == OrderStatus.CONFIRMED;
    }
    
    public void complete() {
        if (!isCompletable()) {
            throw new IllegalStateException("Order cannot be completed in status: " + status);
        }
        this.status = OrderStatus.COMPLETED;
    }
    
    // Getters
    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public Instant getCreatedAt() { return createdAt; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public List<OrderItem> getItems() { return List.copyOf(items); }
}

@Entity
@Table(name = "order_items")
class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(name = "product_name", nullable = false)
    private String productName;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    protected OrderItem() {} // JPA requirement
    
    public OrderItem(String productName, Integer quantity, BigDecimal price) {
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }
    
    void setOrder(Order order) {
        this.order = order;
    }
    
    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
    
    // Getters
    public String getProductName() { return productName; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
}

enum OrderStatus {
    PENDING, CONFIRMED, COMPLETED, CANCELLED
}
```

### Repository Interface

**File:** `src/taskmanager-application/src/main/java/com/centric/taskmanager/application/ports/out/OrderRepository.java`

```java
package com.centric.taskmanager.application.ports.out;

import com.centric.taskmanager.domain.Order;
import com.centric.taskmanager.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Query method - Spring Data derives query from method name
    List<Order> findByCustomerIdAndCreatedAtBetween(
        Long customerId, 
        Instant startDate, 
        Instant endDate
    );
    
    // Query method with status filter
    List<Order> findByCustomerIdAndCreatedAtBetweenAndStatus(
        Long customerId,
        Instant startDate,
        Instant endDate,
        OrderStatus status
    );
    
    // Custom JPQL query for complex scenarios
    @Query("""
        SELECT o FROM Order o
        LEFT JOIN FETCH o.items
        WHERE o.customerId = :customerId
          AND o.createdAt BETWEEN :startDate AND :endDate
          AND (:status IS NULL OR o.status = :status)
        ORDER BY o.createdAt DESC
        """)
    List<Order> findCustomerOrdersWithItems(
        @Param("customerId") Long customerId,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("status") OrderStatus status
    );
    
    // Count query
    @Query("""
        SELECT COUNT(o) FROM Order o
        WHERE o.customerId = :customerId
          AND o.createdAt BETWEEN :startDate AND :endDate
          AND (:status IS NULL OR o.status = :status)
        """)
    long countCustomerOrders(
        @Param("customerId") Long customerId,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("status") OrderStatus status
    );
}
```

### Service Layer

**File:** `src/taskmanager-application/src/main/java/com/centric/taskmanager/application/services/OrderQueryService.java`

```java
package com.centric.taskmanager.application.services;

import com.centric.taskmanager.application.dto.OrderSummaryResponse;
import com.centric.taskmanager.application.ports.out.OrderRepository;
import com.centric.taskmanager.domain.Order;
import com.centric.taskmanager.domain.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderQueryService {
    private final OrderRepository orderRepository;
    
    public OrderQueryService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    public OrderSummaryResponse getCustomerOrders(
            Long customerId,
            Instant startDate,
            Instant endDate,
            String statusFilter) {
        
        // Parse status filter
        OrderStatus status = parseStatus(statusFilter);
        
        // Fetch orders with join fetch to avoid N+1 problem
        List<Order> orders = orderRepository.findCustomerOrdersWithItems(
            customerId, startDate, endDate, status
        );
        
        // Get count
        long totalCount = orderRepository.countCustomerOrders(
            customerId, startDate, endDate, status
        );
        
        // Transform to DTOs (business logic in application layer)
        List<OrderSummaryResponse.OrderDTO> orderDTOs = orders.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
        
        return new OrderSummaryResponse(orderDTOs, totalCount);
    }
    
    private OrderStatus parseStatus(String statusFilter) {
        if (statusFilter == null || "ALL".equalsIgnoreCase(statusFilter)) {
            return null; // No filter
        }
        return OrderStatus.valueOf(statusFilter.toUpperCase());
    }
    
    private OrderSummaryResponse.OrderDTO mapToDTO(Order order) {
        List<OrderSummaryResponse.ItemDTO> itemDTOs = order.getItems().stream()
            .map(item -> new OrderSummaryResponse.ItemDTO(
                item.getProductName(),
                item.getQuantity()
            ))
            .collect(Collectors.toList());
        
        return new OrderSummaryResponse.OrderDTO(
            order.getId(),
            order.getCreatedAt(),
            order.getStatus(),
            order.getTotalAmount(),
            itemDTOs
        );
    }
}
```

### Controller

**File:** `src/taskmanager-api/src/main/java/com/centric/taskmanager/api/controllers/OrderQueryController.java`

```java
package com.centric.taskmanager.api.controllers;

import com.centric.taskmanager.application.dto.OrderSummaryResponse;
import com.centric.taskmanager.application.services.OrderQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/customers/{customerId}/orders")
public class OrderQueryController {
    private final OrderQueryService orderQueryService;
    
    public OrderQueryController(OrderQueryService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }
    
    @GetMapping
    public ResponseEntity<OrderSummaryResponse> getCustomerOrders(
            @PathVariable Long customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(required = false, defaultValue = "ALL") String status) {
        
        OrderSummaryResponse response = orderQueryService.getCustomerOrders(
            customerId, startDate, endDate, status
        );
        
        return ResponseEntity.ok(response);
    }
}
```

**Characteristics:**
- Business logic in Java (testable without database)
- Type-safe queries with Spring Data
- Domain entities with behavior
- Automatic SQL generation by JPA
- Database-agnostic (portable to MySQL, PostgreSQL, H2)

---

## 5. Migration Steps

### Step 1: Analyze Stored Procedure

1. **Identify the business logic** (what transformations happen?)
2. **Extract query patterns** (JOIN structures, WHERE clauses)
3. **Document input/output** parameters
4. **Note performance characteristics** (indexes used, execution time)

### Step 2: Create Domain Entities

```java
@Entity
@Table(name = "orders")
public class Order {
    // Map database columns to entity fields
    // Add business logic methods
}
```

### Step 3: Create Repository Interface

Start simple with query methods:

```java
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
}
```

### Step 4: Test Repository

Write integration tests with Testcontainers:

```java
@DataJpaTest
@Testcontainers
class OrderRepositoryTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Test
    void shouldFindOrdersByCustomerId() {
        // Test query method behavior
    }
}
```

### Step 5: Add Complex Queries

For logic that doesn't fit query methods, use `@Query`:

```java
@Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.customerId = ?1")
List<Order> findByCustomerIdWithItems(Long customerId);
```

### Step 6: Move Business Logic to Service

Extract conditional logic from stored proc to service:

```java
public List<Order> getOrders(Long customerId, String statusFilter) {
    if ("ALL".equals(statusFilter)) {
        return orderRepository.findByCustomerId(customerId);
    } else {
        OrderStatus status = OrderStatus.valueOf(statusFilter);
        return orderRepository.findByCustomerIdAndStatus(customerId, status);
    }
}
```

### Step 7: Performance Optimization

- Add `@EntityGraph` to avoid N+1 problems
- Use projections for read-only queries
- Add database indexes for common query patterns

---

## 6. Testing

### Repository Integration Tests

```java
@DataJpaTest
@Testcontainers
class OrderRepositoryIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Test
    void shouldFindOrdersByCustomerIdAndDateRange() {
        // Given
        Order order1 = createOrder(1L, Instant.parse("2026-01-01T00:00:00Z"));
        Order order2 = createOrder(1L, Instant.parse("2026-02-01T00:00:00Z"));
        orderRepository.saveAll(List.of(order1, order2));
        
        // When
        List<Order> orders = orderRepository.findByCustomerIdAndCreatedAtBetween(
            1L,
            Instant.parse("2026-01-15T00:00:00Z"),
            Instant.parse("2026-02-15T00:00:00Z")
        );
        
        // Then
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getId()).isEqualTo(order2.getId());
    }
    
    @Test
    void shouldHandleEmptyResults() {
        List<Order> orders = orderRepository.findByCustomerId(999L);
        assertThat(orders).isEmpty();
    }
}
```

### Service Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class OrderQueryServiceTest {
    @Mock
    private OrderRepository orderRepository;
    
    @InjectMocks
    private OrderQueryService orderQueryService;
    
    @Test
    void shouldReturnAllOrdersWhenStatusFilterIsAll() {
        // Given
        when(orderRepository.findCustomerOrdersWithItems(any(), any(), any(), isNull()))
            .thenReturn(List.of(mockOrder()));
        
        // When
        OrderSummaryResponse response = orderQueryService.getCustomerOrders(
            1L, Instant.now(), Instant.now(), "ALL"
        );
        
        // Then
        verify(orderRepository).findCustomerOrdersWithItems(any(), any(), any(), isNull());
    }
}
```

---

## 7. Gotchas

### ❌ Pitfall 1: N+1 Query Problem

**Problem:** Loading orders triggers separate query for each order's items.

```java
// BAD: Lazy loading causes N+1
List<Order> orders = orderRepository.findByCustomerId(1L);
orders.forEach(order -> {
    order.getItems().size(); // Triggers separate SELECT for each order
});
```

**Solution:** Use JOIN FETCH or @EntityGraph.

```java
// GOOD: Single query with join
@Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.customerId = ?1")
List<Order> findByCustomerIdWithItems(Long customerId);
```

### ❌ Pitfall 2: Cartesian Product with Multiple Collections

**Problem:** JOIN FETCH on multiple collections causes duplicates.

**Solution:** Use projections or separate queries with batch fetching.

### ❌ Pitfall 3: Complex Business Logic in JPQL

**Problem:** Trying to replicate entire stored procedure logic in JPQL.

**Solution:** Use simple queries, move complex logic to Java service layer.

### ❌ Pitfall 4: Missing Indexes

**Problem:** Query methods work but perform poorly in production.

**Solution:** Add indexes using Flyway migrations:

```sql
CREATE INDEX idx_orders_customer_created ON orders(customer_id, created_at);
CREATE INDEX idx_orders_status ON orders(status);
```

### ❌ Pitfall 5: Not Using Read-Only Transactions

**Problem:** Read queries participate in write transactions.

**Solution:** Mark read-only queries:

```java
@Transactional(readOnly = true)
public List<Order> getOrders(Long customerId) {
    return orderRepository.findByCustomerId(customerId);
}
```

---

## 8. Related Patterns

- **[Flow Transformation](./01-flow-transformation.md)** - Service layer orchestration
- **[JDBC → Spring Data JPA](./02-data-access-jdbc.md)** - Alternative data access migration
- **[Configuration](./06-configuration.md)** - Database connection properties
- **[Observability](./07-observability.md)** - Query performance monitoring

---

**References:**
- Spring Data JPA Documentation: https://spring.io/projects/spring-data-jpa
- JPA Specification: https://jakarta.ee/specifications/persistence/
- Testcontainers: https://www.testcontainers.org/

**Version:** 1.0  
**Last Updated:** April 2, 2026
