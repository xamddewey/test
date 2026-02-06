# Shared Ledger Backend MVP 开发学习总结文档

## 目录
1. [项目概述](#1-项目概述)
2. [技术栈选型与环境要求](#2-技术栈选型与环境要求)
3. [系统架构与模块设计](#3-系统架构与模块设计)
4. [核心数据模型 (Entities)](#4-核心数据模型-entities)
5. [核心业务流程实现](#5-核心业务流程实现)
6. [Jimmer ORM 深度实践](#6-jimmer-orm-深度实践)
7. [RabbitMQ 异步架构设计](#7-rabbitmq-异步架构设计)
8. [核心算法：最少转账次数结算](#8-核心算法最少转账次数结算)
9. [API 设计规范与端点概览](#9-api-设计规范与端点概览)
10. [关键技术决策与最佳实践](#10-关键技术决策与最佳实践)
11. [典型问题与解决方案 (Troubleshooting)](#11-典型问题与解决方案-troubleshooting)
12. [测试与验证](#12-测试与验证)
13. [DevOps 与 CI/CD 实践](#13-devops-与-cicd-实践)
14. [Git 使用规范](#14-git-使用规范)
15. [未来改进方向](#15-未来改进方向)

---

## 1. 项目概述

本项目是一个基于 Spring Boot 的共享账本后端系统（MVP 阶段）。它旨在解决多人协作记账、账目平摊及债务结算的痛点。

### 1.1 MVP 核心目标
- 实现完整的用户认证与授权（JWT）。
- 支持多用户共享账本的创建、邀请与成员管理。
- 提供高效的记账条目管理与灵活的费用分类。
- 实现债务优化的自动结算算法。
- 构建基于消息队列（RabbitMQ）的异步处理架构，支持通知、审计与统计。

### 1.2 核心功能列表
- **认证授权**：注册、登录、JWT 校验、权限控制。
- **账本管理**：创建、更新、软删除、成员权限验证。
- **邀请系统**：发送邀请、接受/拒绝邀请、加入状态追踪。
- **记账管理**：支出记录（Expense）的增删改查、多成员分摊逻辑。
- **分类管理**：系统预置分类与账本自定义分类。
- **结算系统**：最少转账次数算法计算、结算记录生成与完成确认。
- **异步处理**：站内通知推送、审计日志记录、数据统计聚合。

---

## 2. 技术栈选型与环境要求

### 2.1 技术栈概览
| 组件 | 选型 | 说明 |
| :--- | :--- | :--- |
| 核心框架 | Spring Boot 3.4.5 | 最新稳定版本，支持 Java 21 虚拟线程等特性。 |
| 编程语言 | Java 21 (LTS) | 利用现代 Java 语法和性能优化。 |
| ORM 框架 | Jimmer 0.9.120 | 革命性的不可变对象 ORM，提供极强的类型安全性。 |
| 数据库 | PostgreSQL 16 | 强大的开源关系型数据库，支持复杂的索引与约束。 |
| 消息中间件 | RabbitMQ 3.13 | 稳定的 Topic Exchange 模型，实现异步解耦。 |
| 安全框架 | Spring Security + JWT | 经典的无状态认证方案。 |
| 构建工具 | Maven 3.9+ | 依赖管理与项目构建。 |
| 基础设施 | Docker Compose | 一键启动开发环境（DB, MQ）。 |

### 2.2 环境要求
- **JDK**: 21+
- **Docker**: 20.10+
- **Docker Compose**: V2+
- **Maven**: 3.9+

---

## 3. 系统架构与模块设计

### 3.1 总体架构图 (文字版)
```text
[ 用户端 (REST API) ]
       |
       v
[ Spring Security / JWT Filter ]
       |
       v
[ Controller 层 (7个控制器) ] <------> [ DTO 层 (请求/响应/消息) ]
       |
       v
[ Service 业务逻辑层 ] <--------------> [ MQ Producer ] ----> [ RabbitMQ ]
       |                                                    |
       v                                                    v
[ Repository 仓库层 ] <------------------------------ [ MQ Consumer ]
       |
       v
[ PostgreSQL 数据库 ]
```

### 3.2 项目结构树
```text
src/main/java/com/xdw/demobackend/
├── config/                 # 配置类 (RabbitMQ, Security, Web)
├── controller/             # REST 控制器 (7个)
│   ├── AuthController.java
│   ├── LedgerController.java
│   ├── InvitationController.java
│   ├── ExpenseController.java
│   ├── CategoryController.java
│   ├── SettlementController.java
│   └── StatisticsController.java
├── dto/                    # 数据传输对象 (分包管理)
│   ├── auth/               # 认证相关 DTO
│   ├── ledger/             # 账本相关 DTO
│   ├── invitation/         # 邀请相关 DTO
│   ├── expense/            # 记账相关 DTO
│   ├── settlement/         # 结算相关 DTO
│   ├── statistics/         # 统计响应 DTO
│   └── message/            # MQ 消息对象
├── entity/                 # Jimmer 实体定义 (15个)
├── repository/             # JRepository 接口 (15个)
├── service/                # 业务逻辑接口及其实现
├── mq/                     # 消息队列组件
│   ├── producer/           # 4个生产者
│   └── consumer/           # 4个消费者
├── security/               # 安全核心组件 (JWT Filter, UserPrincipal)
├── util/                   # 工具类 (JwtUtils, 结算算法)
└── exception/              # 全局异常处理
```

---

## 4. 核心数据模型 (Entities)

项目共定义了 15 个 Jimmer 实体，分为核心业务、系统支撑和汇总视图三类。

### 4.1 核心业务实体
- **User**: 用户信息，包含 username, password, nickname, email 等。
- **AccountLedger**: 共享账本，包含统计冗余字段（member_count, total_expenses）。
- **LedgerMember**: 账本成员映射，包含成员余额（balance, total_paid, total_shared）。
- **ExpenseRecord**: 支出记录，存储分摊总额、付款人、分类等。
- **ExpenseParticipant**: 支出参与者，记录每个成员在该条目中的分摊金额。
- **ExpenseCategory**: 费用分类，区分系统预置和用户自定义。
- **LedgerCategory**: 账本与分类的关联表。
- **Invitation**: 成员邀请记录，追踪 PENDING/ACCEPTED/REJECTED 状态。
- **Settlement**: 结算记录，追踪转账状态。

### 4.2 系统支撑实体
- **Role / UserRole**: 权限控制基础模型。
- **Notification**: 站内通知，支持已读/未读状态。
- **AuditLog**: 审计日志，记录所有 CRUD 操作（不可变设计）。

### 4.3 汇总视图实体 (Read-only)
- **ExpenseSummaryView**: 账本支出汇总视图。
- **LedgerBalanceSummaryView**: 成员余额汇总视图。

---

## 5. 核心业务流程实现

### 5.1 账本创建与邀请流程
1. **创建账本**：
   - 调用 `LedgerService.createLedger`。
   - 在 `account_ledgers` 插入记录。
   - 自动在 `ledger_members` 为创建者创建 `JOINED` 记录。
   - 初始化冗余字段（memberCount=1, totalExpenses=0）。
2. **成员邀请**：
   - 检查邀请人权限及受邀人是否已存在。
   - 创建 `Invitation` 记录。
   - 发送 `NotificationMessage` 到 RabbitMQ。
3. **接受邀请**：
   - 验证当前用户 ID 是否匹配。
   - 更新 `Invitation` 状态为 `ACCEPTED`。
   - 在 `ledger_members` 插入新记录，并更新账本的 `memberCount`。

### 5.2 记账与余额同步流程
1. **录入支出**：
   - 提交 `CreateExpenseRequest`，包含参与者列表及其金额。
   - 开启事务。
   - 插入 `ExpenseRecord` 和 N 个 `ExpenseParticipant`。
   - **更新冗余字段**：更新账本总额、付款人已付总额、参与者分摊总额。
   - **余额重新计算**：`balance = totalPaid - totalShared`。
   - 异步触发审计日志和统计更新消息。

---

## 6. Jimmer ORM 深度实践

### 6.1 实体设计规范
Jimmer 使用不可变接口定义实体，极大地提高了代码的健壮性。
```java
@Entity
@Table(name = "account_ledgers")
public interface AccountLedger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();

    @Nullable
    String ledgerName();

    @ManyToOne
    User creator();

    @IdView("creator")
    long creatorId();

    @Nullable
    String creatorNickname();

    @Nullable
    Integer memberCount();
    
    // ... 其他冗余统计字段
}
```

### 6.2 Draft API 使用示例
更新实体时，Jimmer 提供了流畅的 Draft API：
```java
AccountLedger updated = AccountLedgerDraft.$.produce(ledger, draft -> {
    if (request.getLedgerName() != null) {
        draft.setLedgerName(request.getLedgerName());
    }
    draft.setUpdatedAt(LocalDateTime.now());
});
ledgerRepository.save(updated);
```

### 6.3 Fetcher API 与 N+1 问题解决
Fetcher 允许开发者精确控制需要加载的字段和关联，避免了传统 ORM 的“抓取策略”困境。
```java
return ledgerRepository.findById(id, AccountLedgerFetcher.$
    .allScalarFields()
    .creator(UserFetcher.$.username().nickname())
).map(LedgerResponse::fromEntity);
```

### 6.4 冗余字段（Denormalization）策略
为了减少 JOIN 操作，我们在实体中广泛使用了冗余字段。
- **自动同步**：在 Service 层业务动作触发时，手动维护这些字段的一致性。
- **Nullable 约定**：所有业务冗余字段在 Jimmer 实体中必须标记为 `@Nullable`，因为它们在数据库中可能由于历史原因或初始化时为 null。

---

## 7. RabbitMQ 异步架构设计

### 7.1 交换机与路由配置
项目使用 `TopicExchange` 实现高度灵活的消息路由。

| 交换机名称 | 对应功能 | 路由键模式示例 |
| :--- | :--- | :--- |
| `ledger.notifications` | 站内通知 | `notification.INVITATION`, `notification.SETTLEMENT` |
| `ledger.audit` | 操作审计 | `audit.LEDGER.CREATE`, `audit.EXPENSE.DELETE` |
| `ledger.settlement` | 结算处理 | `settlement.trigger` |
| `ledger.statistics` | 数据统计 | `statistics.EXPENSE_CREATED` |

### 7.2 消息对象 (DTO) 设计
消息对象必须是可序列化的 POJO，使用 Lombok 简化开发。
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    private String type;
    private Long userId;
    private String title;
    private String content;
    private Long relatedEntityId;
}
```

### 7.3 消费者幂等性与异常处理
- **事务性**：消费者方法标注 `@Transactional`，确保 DB 操作原子性。
- **错误捕获**：使用 try-catch 包裹业务逻辑，防止单条坏消息导致整个消费者线程崩溃。
- **日志记录**：消费失败时记录 Error 级别日志，便于后续人工介入。

---

## 8. 核心算法：最少转账次数结算

### 8.1 算法背景
在一个共享账本中，经过多次消费后，每个成员都有一个“应付”或“应收”的差额。结算的目标是用最少的转账操作清空所有差额。

### 8.2 贪心算法实现细节
```java
public static List<TransferDTO> calculate(Map<Long, BigDecimal> balances) {
    // 1. 分离债权人和债务人
    PriorityQueue<MemberBalance> creditors = new PriorityQueue<>((a, b) -> b.amount.compareTo(a.amount));
    PriorityQueue<MemberBalance> debtors = new PriorityQueue<>((a, b) -> a.amount.compareTo(b.amount));
    
    balances.forEach((userId, amount) -> {
        if (amount.compareTo(BigDecimal.ZERO) > 0) creditors.add(new MemberBalance(userId, amount));
        else if (amount.compareTo(BigDecimal.ZERO) < 0) debtors.add(new MemberBalance(userId, amount));
    });

    List<TransferDTO> transfers = new ArrayList<>();
    // 2. 循环对冲
    while (!creditors.isEmpty() && !debtors.isEmpty()) {
        MemberBalance creditor = creditors.poll();
        MemberBalance debtor = debtors.poll();
        
        BigDecimal amount = creditor.amount.min(debtor.amount.abs());
        transfers.add(new TransferDTO(debtor.userId, creditor.userId, amount));
        
        creditor.amount = creditor.amount.subtract(amount);
        debtor.amount = debtor.amount.add(amount);

        if (creditor.amount.compareTo(BigDecimal.ZERO) > 0) creditors.add(creditor);
        if (debtor.amount.compareTo(BigDecimal.ZERO) < 0) debtors.add(debtor);
    }
    return transfers;
}
```

### 8.3 复杂度与性能
- 该算法在成员数量 N 较小时表现极其优异。
- 对于 50 人上限的账本，计算耗时在毫秒级。

---

## 9. API 设计规范与端点概览

### 9.1 通用响应格式
所有接口均返回 `ApiResult<T>` 统一包装格式：
```json
{
  "success": true,
  "message": "操作成功",
  "data": { ... }
}
```

### 9.2 主要端点详细列表

#### AuthController (`/api/auth`)
| 方法 | 端点 | 功能说明 |
| :--- | :--- | :--- |
| POST | `/register` | 用户注册 |
| POST | `/login` | 用户登录并获取 JWT Token |

#### LedgerController (`/api/ledgers`)
| 方法 | 端点 | 功能说明 |
| :--- | :--- | :--- |
| POST | `/` | 创建共享账本 |
| GET | `/{id}` | 获取账本详情 (需要成员权限) |
| GET | `/` | 获取当前用户加入的所有账本 |
| PUT | `/{id}` | 更新账本基本信息 (仅创建者) |
| DELETE | `/{id}` | 软删除账本 (仅创建者) |
| GET | `/{id}/members` | 获取账本已加入成员列表 |

#### InvitationController (`/api/invitations`)
| 方法 | 端点 | 功能说明 |
| :--- | :--- | :--- |
| POST | `/` | 发送邀请给其他用户 |
| POST | `/{id}/accept` | 接受邀请并正式加入账本 |
| POST | `/{id}/reject` | 拒绝邀请 |
| GET | `/my` | 获取发送给我的待处理邀请 |
| GET | `/ledger/{ledgerId}` | 获取该账本的所有邀请记录 |

#### ExpenseController (`/api/expenses`)
| 方法 | 端点 | 功能说明 |
| :--- | :--- | :--- |
| POST | `/` | 创建支出记录 (包含参与者列表) |
| GET | `/{id}` | 获取单条支出记录详情 |
| GET | `/ledger/{ledgerId}` | 分页获取账本内的所有支出 |
| PUT | `/{id}` | 更新支出记录 (任何成员均可) |
| DELETE | `/{id}` | 软删除支出记录 (撤销余额影响) |

#### CategoryController (`/api/categories`)
| 方法 | 端点 | 功能说明 |
| :--- | :--- | :--- |
| POST | `/` | 创建自定义费用分类 |
| GET | `/system` | 获取系统预置的公共分类 |
| GET | `/ledger/{ledgerId}` | 获取账本已启用的分类列表 |
| POST | `/ledger/{ledgerId}/add` | 将现有分类添加到特定账本 |
| DELETE | `/ledger/{ledgerId}/category/{id}` | 从账本中移除某分类 |

#### SettlementController (`/api/settlements`)
| 方法 | 端点 | 功能说明 |
| :--- | :--- | :--- |
| GET | `/calculate/{ledgerId}` | 根据当前余额试算最少转账方案 |
| POST | `/` | 创建正式的结算请求 |
| POST | `/{id}/complete` | 确认结算已完成，更新成员余额 |
| GET | `/ledger/{ledgerId}` | 获取账本的历史结算记录 |
| GET | `/my` | 获取与我相关的结算记录 |

#### StatisticsController (`/api/statistics`)
| 方法 | 端点 | 功能说明 |
| :--- | :--- | :--- |
| GET | `/ledger/{id}/category` | 按分类统计支出分布 |
| GET | `/ledger/{id}/member` | 按成员统计已付/分摊/余额 |
| GET | `/ledger/{id}/timeline` | 按时间轴统计支出趋势 |
| GET | `/ledger/{id}/summary` | 账本整体概览 (包含总额、记录数等) |

---

## 10. 关键技术决策与最佳实践

### 10.1 软删除 (Soft Delete) 机制
- **实现方案**：在所有核心业务表中增加 `is_deleted (boolean)` 和 `deleted_at (timestamp)`。
- **查询过滤**：所有 JRepository 查询通过方法名约定（如 `AndIsDeletedFalse`）自动过滤。
- **业务回滚**：在删除 `ExpenseRecord` 时，必须原子性地回滚对账本总额和成员余额的影响。

### 10.2 权限验证模型
- **垂直权限**：Spring Security 结合 JWT 拦截非法请求。
- **水平权限 (Data-level)**：在 Service 层执行。
  - 成员可见性：只有 `JOINED` 状态的成员能看到账本详情。
  - 操作权：只有 `creator` 能修改账本名称或解散账本。

### 10.3 数据一致性与 Redundant Fields
- 为了极致查询性能，我们采用了 **读写不平衡设计**。
- **写时多更**：一次记账动作会触发 5-8 个表的更新（冗余字段维护）。
- **读时单表**：展示账本列表时，无需 JOIN `ledger_members` 即可获得 `memberCount`。

---

## 11. 典型问题与解决方案 (Troubleshooting)

### 11.1 Jimmer `UnloadedException`
- **现象**：`The property "xxx" is unloaded`。
- **场景**：调用 `insert()` 后直接将返回实体转为 DTO。
- **对策**：使用 `ImmutableObjects.isLoaded(entity, Props.FIELD)` 守卫。

### 11.2 PostgreSQL 枚举解析警告
- **现象**：启动时日志显示 Jimmer 无法解析 DB 中的枚举约束。
- **对策**：将校验模式设为 `WARNING`，此问题源于 Jimmer 0.9.120 对 PG 特有语法的解析限制，不影响实际运行。

### 11.3 RabbitMQ 连接自动恢复
- **问题**：网络波动导致 MQ 断连。
- **解决**：Spring RabbitMQ 默认开启自动重连。通过 `healthCheck` 确保容器环境下的稳定性。

### 11.4 统计端点 500 错误
- **已知问题**：`GET /api/statistics/ledger/{id}/summary` 在特定数据下报错。
- **临时方案**：客户端暂缓调用此特定汇总端点，改用分类统计和成员统计组合展示。

---

## 12. 测试与验证

### 12.1 自动化单元测试
- **测试框架**: JUnit 5 + Mockito。
- **重点**: `SettlementAlgorithm` (算法正确性), `JwtUtils` (令牌生成与解析)。

### 12.2 集成测试 (Spring Boot Test)
- **环境**: 使用 Testcontainers 或独立的 CI 数据库。
- **路径**: `AuthController` -> `LedgerController` -> `InvitationController` -> `ExpenseController`。

---

## 13. DevOps 与 CI/CD 实践

### 13.1 Docker Compose 环境
```yaml
services:
  postgres:
    image: postgres:16
    ports: ["15432:5432"]
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U myapp_user -d myapp_db"]
  rabbitmq:
    image: rabbitmq:3.13-management
    ports: ["5672:5672", "15672:15672"]
```

### 13.2 GitHub Actions 工作流
- 流程：Checkout -> Java Setup -> Docker Compose Up -> ./mvnw test -> Artifact Upload。
- 优点：确保每一行进入 `dev` 分支的代码都是经过环境验证的。

---

## 14. Git 使用规范

严格的 Git 规范是大型项目成功的基石。

### 14.1 核心准则
1. **中文标题**：`feat(auth): 实现 JWT 刷新逻辑`。
2. **Commit Body**：必须包含修改原因和影响范围。
3. **Squash Merge**：合并 feature 分支时使用 squash，保持主线整洁。
4. **禁止强制推送**：保护 `dev` 分支历史。

### 14.2 示例
```text
feat(ledger): 添加账本邀请接口

- 实现发送邀请、接受/拒绝邀请逻辑
- 集成 RabbitMQ 异步发送通知
- 增加成员权限校验，防止重复邀请
```

---

## 15. 开发工作流与本地调试

为了确保团队协作的高效性，本项目定义了严格的本地开发流。

### 15.1 环境启动
1. **启动依赖容器**：
   ```bash
   docker compose up -d
   ```
2. **验证状态**：
   ```bash
   docker compose ps
   # 确保 shared-ledger-postgres 和 shared-ledger-rabbitmq 均为 Healthy
   ```

### 15.2 数据库初始化
- 脚本位置：`src/main/resources/sql_script/myapp_schema_init.sql`。
- Jimmer 会在应用启动时自动验证 Schema。如果需要重置，建议执行：
  ```bash
  docker compose down -v
  docker compose up -d
  ```

### 15.3 编译与运行
- **生成 Jimmer 辅助类**：
  ```bash
  ./mvnw clean compile
  ```
- **启动应用**：
  ```bash
  ./mvnw spring-boot:run
  ```
- **端口访问**：
  - 应用 API：`http://localhost:8080/api`
  - RabbitMQ 控制台：`http://localhost:15672` (guest/guest)

---

## 16. 详细数据库设计 (Database Schema)

### 16.1 核心表描述
- **users**: 存储用户基本信息及加密后的密码。
- **account_ledgers**: 记录账本元数据。
  - `total_expenses`: 缓存的总支出金额，避免全表 SUM。
  - `record_count`: 缓存的支出条目数，用于快速列表显示。
- **ledger_members**: 
  - `balance`: 当前成员的结余。计算公式：`已支付 - 应分摊`。
- **expense_records**:
  - `participants_info`: JSONB 格式存储参与者摘要，用于极速渲染列表，无需 JOIN `expense_participants`。

### 16.2 索引优化策略
- 为外键（`user_id`, `ledger_id`, `category_id`）建立 B-Tree 索引。
- 为 `is_deleted` 建立部分索引（Partial Index），优化活跃数据查询性能。
- 为 `expense_date` 建立索引，加速时间区间统计报表。

---

## 17. 未来改进方向

1. **分布式事务管理**：考虑引入 Seata 处理微服务化后的分布式事务。
2. **多币种实时汇率**：对接外部汇率 API，支持跨币种结算。
3. **性能压测**：模拟万级账本、百万级支出的性能表现。
4. **移动端适配**：开发配套的 React Native 或 Flutter 应用。

---

> **结语**：本项目作为共享账本系统的后端 MVP，不仅展示了 Spring Boot 与现代 ORM (Jimmer) 的完美结合，更通过异步消息架构和严谨的算法设计，为后续的微服务演进打下了坚实基础。

**文档版本**：v1.1 (MVP 深度总结)
**更新日期**：2026-02-06
**编写者**：Sisyphus-Junior
