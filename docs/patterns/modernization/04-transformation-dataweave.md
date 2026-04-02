# Pattern: DataWeave → Java Business Logic

**Maturity:** 🟢 Production-Ready  
**Category:** Transformation  
**Complexity:** Medium

---

## 1. Overview

Convert DataWeave transformations into type-safe Java code using mapping frameworks like MapStruct. This moves transformation logic from declarative scripts to maintainable, testable Java.

**Key Transformation:**
- **From:** DataWeave 2.0 transformation scripts
- **To:** MapStruct mappers with custom Java methods

---

## 2. Context

Use this pattern when:
- ✅ DataWeave performs object mapping/transformation
- ✅ You need type safety and compile-time validation
- ✅ Transformations contain business rules
- ✅ You want IDE support and refactoring capabilities

---

## 3. Before: DataWeave Transformation

```xml
<ee:transform>
    <ee:message>
        <ee:set-payload><![CDATA[%dw 2.0
        output application/json
        ---
        {
            orderId: payload.id,
            customer: {
                fullName: payload.firstName ++ " " ++ payload.lastName,
                email: lower(payload.email)
            },
            items: payload.lineItems map {
                productId: $.product.id,
                productName: $.product.name,
                quantity: $.qty,
                unitPrice: $.price,
                subtotal: $.qty * $.price
            },
            totals: {
                itemCount: sizeOf(payload.lineItems),
                subtotal: sum(payload.lineItems.*amount),
                tax: sum(payload.lineItems.*amount) * 0.08,
                grandTotal: sum(payload.lineItems.*amount) * 1.08
            },
            createdAt: now() as String {format: "yyyy-MM-dd'T'HH:mm:ss"}
        }]]></ee:set-payload>
    </ee:message>
</ee:transform>
```

---

## 4. After: MapStruct Mapper

### Mapper Interface

```java
@Mapper(componentModel = "spring", imports = {Instant.class, BigDecimal.class})
public interface OrderMapper {
    
    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "customer", source = ".", qualifiedByName = "mapCustomer")
    @Mapping(target = "items", source = "lineItems")
    @Mapping(target = "totals", source = "lineItems", qualifiedByName = "calculateTotals")
    @Mapping(target = "createdAt", expression = "java(Instant.now())")
    OrderResponse toOrderResponse(OrderEntity entity);
    
    @Named("mapCustomer")
    default CustomerDTO mapCustomer(OrderEntity entity) {
        return new CustomerDTO(
            entity.getFirstName() + " " + entity.getLastName(),
            entity.getEmail().toLowerCase()
        );
    }
    
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "quantity", source = "qty")
    @Mapping(target = "unitPrice", source = "price")
    @Mapping(target = "subtotal", expression = "java(calculateSubtotal(item))")
    OrderItemDTO toOrderItemDTO(LineItemEntity item);
    
    default BigDecimal calculateSubtotal(LineItemEntity item) {
        return item.getPrice().multiply(BigDecimal.valueOf(item.getQty()));
    }
    
    @Named("calculateTotals")
    default TotalsDTO calculateTotals(List<LineItemEntity> items) {
        int itemCount = items.size();
        BigDecimal subtotal = items.stream()
            .map(item -> item.getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.08));
        BigDecimal grandTotal = subtotal.multiply(BigDecimal.valueOf(1.08));
        
        return new TotalsDTO(itemCount, subtotal, tax, grandTotal);
    }
}
```

### Usage in Service

```java
@Service
public class OrderService {
    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    
    public OrderService(OrderMapper orderMapper, OrderRepository orderRepository) {
        this.orderMapper = orderMapper;
        this.orderRepository = orderRepository;
    }
    
    public OrderResponse getOrder(Long id) {
        OrderEntity entity = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));
        
        return orderMapper.toOrderResponse(entity);
    }
}
```

---

## 5. Migration Steps

1. **Identify source and target structures** in DataWeave
2. **Create DTOs** for target structure
3. **Generate MapStruct mapper** interface
4. **Add custom methods** for complex transformations
5. **Add `mapstruct` dependency** to pom.xml:

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.5.Final</version>
    <scope>provided</scope>
</dependency>
```

---

## 6. Testing

```java
@SpringBootTest
class OrderMapperTest {
    @Autowired
    private OrderMapper orderMapper;
    
    @Test
    void shouldMapOrderEntityToResponse() {
        // Given
        OrderEntity entity = createOrderEntity();
        
        // When
        OrderResponse response = orderMapper.toOrderResponse(entity);
        
        // Then
        assertThat(response.orderId()).isEqualTo(entity.getId());
        assertThat(response.customer().fullName()).isEqualTo("John Doe");
        assertThat(response.totals().grandTotal())
            .isEqualByComparingTo(BigDecimal.valueOf(108.00));
    }
    
    @ParameterizedTest
    @CsvSource({"10,2,20.00", "5.50,3,16.50"})
    void shouldCalculateSubtotalCorrectly(BigDecimal price, int qty, BigDecimal expected) {
        LineItemEntity item = new LineItemEntity();
        item.setPrice(price);
        item.setQty(qty);
        
        BigDecimal subtotal = orderMapper.calculateSubtotal(item);
        
        assertThat(subtotal).isEqualByComparingTo(expected);
    }
}
```

---

## 7. Gotchas

### ❌ Forgetting Null Checks
DataWeave handles nulls gracefully; Java requires explicit null handling.

**Solution:** Use `@Mapping(defaultValue = "0")` or custom methods with null checks.

### ❌ Complex Nested Mappings
Deeply nested structures can become verbose.

**Solution:** Break into multiple mapper methods or create intermediate DTOs.

---

## 8. Related Patterns

- **[Flow Transformation](./01-flow-transformation.md)** - Service layer integration
- **[Data Access](./02-data-access-stored-procedures.md)** - Entity to DTO mapping

---

**Version:** 1.0  
**Last Updated:** April 2, 2026
