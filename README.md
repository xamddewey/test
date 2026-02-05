# Shared Ledger Backend (共享账本后端)

Spring Boot 3.4.5 backend application for shared expense management with JWT authentication, PostgreSQL, and RabbitMQ.

## Technology Stack

- **Framework**: Spring Boot 3.4.5
- **Language**: Java 21
- **ORM**: Jimmer 0.9.120
- **Database**: PostgreSQL 16
- **Message Queue**: RabbitMQ 3.13
- **Security**: Spring Security + JWT
- **Container**: Docker Compose

## Prerequisites

- **Java 21** or higher
- **Docker** and **Docker Compose**
- **Maven 3.9+** (or use included `./mvnw`)

## Quick Start

### 1. Start Infrastructure

Start both PostgreSQL and RabbitMQ services:

```bash
docker compose up -d
```

This starts:
- **PostgreSQL 16** on port `15432` (mapped from container port 5432)
- **RabbitMQ 3.13** on port `5672` (AMQP) and `15672` (Management UI)

Verify services are running:

```bash
docker compose ps
```

Both services should show status as "running" (healthy).

### 2. Initialize Database

Apply schema migrations (if not auto-applied):

```bash
# Schema file is in: src/main/resources/sql_script/myapp_schema_init.sql
# Jimmer validates schema on startup via database-validation-mode=ERROR
```

### 3. Run Application

Compile and run the application:

```bash
./mvnw spring-boot:run
```

Application starts on `http://localhost:8080`

### 4. Access Services

- **Swagger UI**: `http://localhost:8080/swagger-ui.html` (add springdoc-openapi dependency to enable)
- **RabbitMQ Management**: `http://localhost:15672` (guest/guest)
- **API Base URL**: `http://localhost:8080/api`

## API Endpoints

The application provides REST APIs across **7 controllers** with approximately **30 endpoints**:

### 1. AuthController (`/api/auth`)

Authentication and user registration.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/register` | Register new user |
| POST | `/login` | Login and get JWT token |

### 2. LedgerController (`/api/ledgers`)

Ledger CRUD and member management.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create shared ledger |
| GET | `/{ledgerId}` | Get ledger by ID |
| GET | `/` | Get current user's joined ledgers |
| PUT | `/{ledgerId}` | Update ledger (creator only) |
| DELETE | `/{ledgerId}` | Delete ledger (creator only) |
| GET | `/{ledgerId}/members` | Get ledger members |

### 3. InvitationController (`/api/invitations`)

Invitation flow for joining ledgers.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Send invitation to user |
| POST | `/{invitationId}/accept` | Accept invitation |
| POST | `/{invitationId}/reject` | Reject invitation |
| GET | `/my` | Get my pending invitations |
| GET | `/ledger/{ledgerId}` | Get ledger's invitations |

### 4. ExpenseController (`/api/expenses`)

Expense record CRUD operations.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create expense record |
| GET | `/{expenseId}` | Get expense by ID |
| GET | `/ledger/{ledgerId}` | Get ledger expenses (paginated) |
| PUT | `/{expenseId}` | Update expense |
| DELETE | `/{expenseId}` | Delete expense |

### 5. CategoryController (`/api/categories`)

Expense category management.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create custom category |
| GET | `/system` | Get system-default categories |
| GET | `/ledger/{ledgerId}` | Get ledger's active categories |
| POST | `/ledger/{ledgerId}/add?categoryId={id}` | Add category to ledger |
| DELETE | `/ledger/{ledgerId}/category/{categoryId}` | Remove category from ledger |

### 6. SettlementController (`/api/settlements`)

Settlement calculation and execution.

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/calculate/{ledgerId}` | Calculate minimum transfers (read-only) |
| POST | `/` | Create settlement record |
| POST | `/{settlementId}/complete` | Mark settlement completed |
| GET | `/ledger/{ledgerId}` | Get ledger settlements (optional status filter) |
| GET | `/my` | Get current user's settlements |

### 7. StatisticsController (`/api/statistics`)

Aggregated expense statistics (NEW in Task 7).

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/ledger/{ledgerId}/category` | Category expense breakdown |
| GET | `/ledger/{ledgerId}/timeline` | Timeline expense trends |
| GET | `/ledger/{ledgerId}/member` | Member spending analysis |
| GET | `/ledger/{ledgerId}/summary` | Overall ledger summary |

**Total:** ~30 endpoints across 7 controllers

## Project Structure

```
src/main/java/com/xdw/demobackend/
├── config/                 # Configuration classes
│   ├── RabbitMQConfig.java # RabbitMQ exchange/queue setup
│   ├── SecurityConfig.java # JWT + Spring Security
│   └── WebConfig.java      # CORS configuration
├── controller/             # 7 REST controllers
│   ├── AuthController.java
│   ├── LedgerController.java
│   ├── InvitationController.java
│   ├── ExpenseController.java
│   ├── CategoryController.java
│   ├── SettlementController.java
│   └── StatisticsController.java
├── dto/                    # Request/Response DTOs
│   ├── auth/               # Login/register DTOs
│   ├── common/             # Pagination, sorting, ApiResult
│   ├── ledger/             # Ledger CRUD DTOs
│   ├── invitation/         # Invitation flow DTOs
│   ├── expense/            # Expense CRUD DTOs
│   ├── settlement/         # Settlement DTOs
│   ├── statistics/         # Statistics response DTOs (NEW)
│   └── message/            # RabbitMQ message DTOs (NEW)
├── entity/                 # 15 Jimmer ORM entities
│   ├── User.java
│   ├── Role.java
│   ├── UserRole.java
│   ├── AccountLedger.java
│   ├── LedgerMember.java
│   ├── ExpenseRecord.java
│   ├── ExpenseParticipant.java
│   ├── ExpenseCategory.java
│   ├── LedgerCategory.java
│   ├── Invitation.java
│   ├── Settlement.java
│   ├── Notification.java
│   ├── AuditLog.java
│   ├── ExpenseSummaryView.java
│   └── LedgerBalanceSummaryView.java
├── repository/             # 15 JRepository interfaces
├── service/                # Business logic services
│   ├── auth/               # Authentication services
│   ├── user/               # User management
│   ├── ledger/             # Ledger operations
│   ├── invitation/         # Invitation flow
│   ├── expense/            # Expense management
│   ├── settlement/         # Settlement calculation
│   └── statistics/         # Statistics aggregation (NEW)
├── mq/                     # RabbitMQ infrastructure (NEW)
│   ├── producer/           # Message publishers
│   │   ├── NotificationProducer.java
│   │   ├── AuditProducer.java
│   │   ├── SettlementProducer.java
│   │   └── StatisticsProducer.java
│   └── consumer/           # Message consumers
│       ├── NotificationConsumer.java
│       ├── AuditConsumer.java
│       ├── SettlementConsumer.java
│       └── StatisticsConsumer.java
├── security/               # JWT authentication
│   ├── JwtAuthenticationFilter.java
│   ├── AuthEntryPointJwt.java
│   └── UserPrincipal.java
├── util/                   # Utility classes
│   ├── JwtUtils.java
│   └── SettlementAlgorithm.java
└── exception/              # Exception handling
    └── GlobalExceptionHandler.java
```

## Configuration

### Database Configuration

PostgreSQL connection settings in `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:15432/myapp_db
spring.datasource.username=myapp_user
spring.datasource.password=myapp_password
```

### RabbitMQ Configuration

RabbitMQ connection settings:

```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

**RabbitMQ Architecture:**
- **4 Topic Exchanges**: `ledger.notifications`, `ledger.audit`, `ledger.settlement`, `ledger.statistics`
- **4 Durable Queues**: `notification-queue`, `audit-queue`, `settlement-queue`, `statistics-queue`
- **Routing Keys**: Dynamic routing based on message type (e.g., `notification.INVITATION`, `audit.LEDGER.CREATE`)

### Jimmer ORM Configuration

```properties
jimmer.dialect=org.babyfish.jimmer.sql.dialect.PostgresDialect
jimmer.show-sql=true
jimmer.pretty-sql=true
jimmer.database-validation-mode=ERROR
```

- **Immutable entities**: All entities are interfaces (not classes)
- **Draft API**: Use `EntityDraft.$.produce()` for entity creation/updates
- **Fetcher API**: Eager-load associations to prevent N+1 queries
- **Nullability validation**: Strict matching with database schema

## Testing

### Run All Tests

```bash
./mvnw test
```

### Run Tests with Coverage

```bash
./mvnw test jacoco:report
```

Coverage report available at: `target/site/jacoco/index.html`

### Run Specific Test Class

```bash
./mvnw test -Dtest=YourTestClass
```

## Continuous Integration

CI workflow runs automatically on:
- Push to `dev` branch
- Pull requests targeting `dev` branch

**Workflow:**
1. Set up Java 21 with Maven cache
2. Start PostgreSQL and RabbitMQ service containers
3. Run `./mvnw clean test` with test environment variables
4. Upload test results as artifacts (available for 90 days)

## Development Workflow

### 1. Create Feature Branch

```bash
git checkout -b feature/your-feature-name
```

### 2. Make Changes

Edit code, add tests, verify compilation:

```bash
./mvnw clean compile
```

### 3. Run Tests Locally

```bash
./mvnw test
```

### 4. Commit Changes

Use conventional commit format (中文描述):

```bash
git commit -m "feat(controller): 添加统计API"
```

Commit types: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`

### 5. Push to Remote

```bash
git push origin feature/your-feature-name
```

CI workflow will run automatically.

## Troubleshooting

### RabbitMQ Connection Refused

**Symptoms:** Application fails to start with connection errors.

**Solutions:**
```bash
docker compose ps
docker compose logs rabbitmq

docker compose restart rabbitmq
```

Verify port 5672 is not in use by another process:
```bash
lsof -i :5672
```

### Database Migration Issues

**Symptoms:** Schema validation errors on startup.

**Solutions:**
```bash
docker compose logs postgres

docker compose down -v
docker compose up -d
```

Check if schema was applied:
```bash
docker compose exec postgresql psql -U myapp_user -d myapp_db -c "\dt"
```

### Build Failures

**Symptoms:** Maven compilation errors.

**Solutions:**
```bash
./mvnw clean

./mvnw compile

rm -rf target/
./mvnw clean compile
```

Check Java version:
```bash
java -version
```

### Port Conflicts

**Symptoms:** Services fail to start due to port already in use.

**Solutions:**

Check which process is using the port:
```bash
lsof -i :8080
lsof -i :15432
lsof -i :5672
```

Kill the process or change port in `application.properties` / `compose.yaml`.

### Lombok Compilation Issues

**Symptoms:** LSP shows errors for getters/setters, but Maven succeeds.

**Explanation:** Lombok generates code at compile time. LSP errors can be ignored if Maven compilation succeeds.

**Solution:** Trust Maven output over LSP diagnostics for Lombok-annotated classes.

## Key Features

### 1. JWT Authentication

Secure token-based authentication with refresh token support.

### 2. Soft Delete Pattern

All entities use `isDeleted` flag for logical deletion (no hard deletes).

### 3. Redundant Fields Strategy

Schema uses denormalized fields (nicknames, counts, amounts) to avoid JOINs in read queries.

### 4. Async Processing

RabbitMQ handles background tasks:
- Notification delivery
- Audit logging
- Settlement recalculation
- Statistics aggregation

### 5. Settlement Algorithm

Greedy minimum-transfer algorithm for debt optimization:
- **Input:** Member balances (who owes what)
- **Output:** Minimum number of transfers to settle all debts
- **Complexity:** O(N log N)

### 6. Statistics Engine

Real-time aggregation for expense analytics:
- Category breakdown
- Timeline trends
- Member spending analysis
- Ledger summary

## Database Schema

### Core Tables

- **users**: User accounts with roles
- **account_ledgers**: Shared expense ledgers
- **ledger_members**: User memberships in ledgers
- **expense_records**: Individual expense entries
- **expense_participants**: Who participated in each expense
- **expense_categories**: Category definitions
- **ledger_categories**: Ledger-specific category associations
- **invitations**: Ledger join invitations
- **settlements**: Payment settlement records
- **notifications**: User notifications
- **audit_logs**: System audit trail

### Summary Views

Pre-calculated aggregate tables for fast queries:

- **expense_summary_view**: Expense statistics per ledger
- **ledger_balance_summary_view**: Member balance summaries

## Resources

- **Jimmer Documentation**: https://babyfish-ct.github.io/jimmer-doc/
- **Spring Boot Reference**: https://docs.spring.io/spring-boot/docs/current/reference/html/
- **RabbitMQ Tutorials**: https://www.rabbitmq.com/getstarted.html

## License

[Specify your license here]
