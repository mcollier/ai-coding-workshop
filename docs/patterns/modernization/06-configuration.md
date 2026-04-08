# Pattern: Mule Properties → Spring Configuration

**Maturity:** 🟢 Production-Ready  
**Category:** Configuration  
**Complexity:** Low

---

## 1. Overview

Migrate Mule configuration files to Spring Boot's externalized configuration with YAML/properties files and Spring profiles.

**Key Transformation:**
- **From:** Mule `config-{env}.yaml` with property placeholders
- **To:** Spring `application-{profile}.yml` with @Value and @ConfigurationProperties

---

## 2. Context

Use this pattern when:
- ✅ You need environment-specific configuration
- ✅ You want type-safe configuration classes
- ✅ You need to externalize database, HTTP, and messaging config
- ✅ You want configuration validation at startup

---

## 3. Before: Mule Configuration

### Mule Global Configuration

**File:** `config-dev.yaml`

```yaml
# Mule configuration
http:
  listener:
    host: 0.0.0.0
   port: 8081

database:
  host: localhost
  port: 5432
  database: orders_db
  username: dbuser
  password: ${DB_PASSWORD}
  
payment:
  service:
    baseUrl: http://payment-service:8080
    timeout: 5000
    retries: 3

app:
  config:
    maxOrderItems: 50
    taxRate: 0.08
```

**File:** `mule-config.xml`

```xml
<db:config name="Database_Config">
    <db:data-source-connection
        host="${database.host}"
        port="${database.port}"
        database="${database.database}"
        user="${database.username}"
        password="${database.password}"/>
</db:config>

<http:request-config name="Payment_Service_Config">
    <http:request-connection
        host="${payment.service.baseUrl}"
        connectionTimeout="${payment.service.timeout}"/>
</http:request-config>
```

---

## 4. After: Spring Boot Configuration

### Application Configuration

**File:** `src/taskmanager-api/src/main/resources/application.yml`

```yaml
# Common configuration (all profiles)
spring:
  application:
    name: taskmanager-api
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

**File:** `application-dev.yml`

```yaml
# Development profile
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/orders_db
    username: dbuser
    password: ${DB_PASSWORD:devpassword}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
  jpa:
    show-sql: true

services:
  payment:
    base-url: http://localhost:8080
    timeout: 5s
    retries: 3

app:
  config:
    max-order-items: 50
    tax-rate: 0.08

logging:
  level:
    com.centric.taskmanager: DEBUG
```

**File:** `application-prod.yml`

```yaml
# Production profile
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
      
services:
  payment:
    base-url: ${PAYMENT_SERVICE_URL}
    timeout: 3s
    retries: 5

app:
  config:
    max-order-items: 100
    tax-rate: 0.08

logging:
  level:
    com.centric.taskmanager: INFO
```

### Type-Safe Configuration

```java
package com.centric.taskmanager.api.config;

import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "app.config")
@Validated
public record AppConfigProperties(
    @Min(1) @Max(1000) Integer maxOrderItems,
    @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal taxRate
) {}
```

### Service Configuration

```java
package com.centric.taskmanager.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties(prefix = "services.payment")
@Validated
public record PaymentServiceProperties(
    String baseUrl,
    Duration timeout,
    Integer retries
) {}
```

### Enable Configuration Properties

```java
@SpringBootApplication
@EnableConfigurationProperties({AppConfigProperties.class, PaymentServiceProperties.class})
public class TaskManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskManagerApplication.class, args);
    }
}
```

### Usage in Components

```java
@Service
public class OrderService {
    private final AppConfigProperties appConfig;
    
    public OrderService(AppConfigProperties appConfig) {
        this.appConfig = appConfig;
    }
    
    public void validateOrder(CreateOrderRequest request) {
        if (request.items().size() > appConfig.maxOrderItems()) {
            throw new OrderValidationException(
                "Order cannot contain more than " + appConfig.maxOrderItems() + " items"
            );
        }
    }
    
    public BigDecimal calculateTax(BigDecimal subtotal) {
        return subtotal.multiply(appConfig.taxRate());
    }
}
```

---

## 5. Migration Steps

1. **Extract all Mule properties** from config files
2. **Create `application.yml`** with common settings
3. **Create profile-specific files** (`application-dev.yml`, `application-prod.yml`)
4. **Create @ConfigurationProperties classes** for type safety
5. **Add validation constraints** (@Min, @Max, @NotBlank)
6. **Replace @Value with constructor injection** of config classes
7. **Set active profile** via `-Dspring.profiles.active=dev` or environment variable

---

## 6. Testing

### Test Configuration

**File:** `src/test/resources/application-test.yml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: create-drop

app:
  config:
    max-order-items: 10
    tax-rate: 0.05
```

### Configuration Tests

```java
@SpringBootTest
@ActiveProfiles("test")
class AppConfigPropertiesTest {
    @Autowired
    private AppConfigProperties appConfig;
    
    @Test
    void shouldLoadTestConfiguration() {
        assertThat(appConfig.maxOrderItems()).isEqualTo(10);
        assertThat(appConfig.taxRate()).isEqualByComparingTo(BigDecimal.valueOf(0.05));
    }
}

@SpringBootTest
class ConfigurationValidationTest {
    @Test
    void shouldFailWhenMaxOrderItemsExceedsLimit() {
        assertThatThrownBy(() -> {
            new AppConfigProperties(1001, BigDecimal.valueOf(0.08));
        }).isInstanceOf(ConstraintViolationException.class);
    }
}
```

### Using @TestPropertySource

```java
@SpringBootTest
@TestPropertySource(properties = {
    "app.config.max-order-items=5",
    "app.config.tax-rate=0.10"
})
class OrderServiceTest {
    @Autowired
    private OrderService orderService;
    
    @Test
    void shouldUseTestConfiguration() {
        // Test uses overridden properties
    }
}
```

---

## 7. Gotchas

### ❌ Hardcoded Values
Don't hardcode configuration in Java code; externalize everything.

### ❌ Missing Default Values
Always provide sensible defaults: `${DB_PASSWORD:defaultPassword}`

### ❌ Sensitive Data in Git
Never commit passwords or secrets. Use environment variables or secret management.

### ❌ Profile Not Activated
Set `SPRING_PROFILES_ACTIVE=dev` environment variable or use `-Dspring.profiles.active=dev`.

---

## 8. Related Patterns

- **[Flow Transformation](./01-flow-transformation.md)** - Service configuration
- **[Data Access](./02-data-access-stored-procedures.md)** - Database configuration
- **[Integration HTTP](./03-integration-http.md)** - Service endpoint configuration

---

**Version:** 1.0  
**Last Updated:** April 2, 2026
