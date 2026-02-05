# Shared Ledger Backend — Work Plan

## TL;DR

> **Quick Summary**: Build a Spring Boot–based shared ledger backend (monolith first) with JWT auth, shared account books, equal-split expenses, optimal settlements, in-app notifications, stats, and audit logs, while laying a learning path for MQ (RabbitMQ), Docker/CI/CD now and microservices later.
>
> **Deliverables**:
> - REST API (Swagger/OpenAPI) for auth, ledgers, members, entries, categories, invitations, settlements, notifications, stats, audit logs
> - Jimmer ORM entities & repositories aligned with `myapp_schema_init.sql` (multi-currency fields reserved)
> - RabbitMQ-based async flows for notifications, settlements, stats, audit logs (MVP)
> - Dockerized infra (Postgres, Redis if used later, RabbitMQ) + local dev runbook
> - Minimal but working test suite + CI skeleton
>
> **Estimated Effort**: Large
> **Parallel Execution**: YES — 3 waves
> **Critical Path**: Data model → Core APIs → Settlement logic → MQ async flows → Stats/Notifications → CI/Docker

---

## Context

### Original Request
用户要做“共享记账 App”后端（前后端分离，先做后端）。
需求包括：用户注册/登录、账本创建与共享、成员邀请、记账条目管理、条目类型、结算（最少转账次数）、站内通知、统计报表、审计日志；管理员/账本创建者权限；MVP 单体，后期微服务；RabbitMQ；多币种预留。

### Interview Summary
**Key Decisions**:
- 技术栈：Spring Boot + Jimmer ORM + JWT；REST + OpenAPI/Swagger，无 API 版本号
- 架构：MVP 单体，末期拆微服务；基础设施独立 Docker 容器
- 结算算法：最少转账次数；均摊；多 1 分由付款人承担
- 权限：账本创建者可改/删账本；成员可编辑条目；审计日志可被管理员与账本创建者查看
- 通知：站内通知（已读/未读），保留半年，用户可删除；类型（邀请/系统/结算）
- MQ：RabbitMQ（通知/结算/统计/审计异步）
- 统计：按类型/成员/时间区间（周/月/自定义）
- 账本创建者删除账号：转移给成员
- MVP 规模上限：成员数/条目数/类型数/邀请有效期

**Research Findings**:
- 现有项目已经实现 JWT 鉴权与 Jimmer ORM；有基础测试（JUnit5 + Spring Boot Test）
- 数据库草案 `myapp_schema_init.sql` 完整，但缺少 notifications 表
- Jimmer 严格校验 nullability；使用 Fetcher 做 eager loading

### Metis Review
**Identified Gaps (addressed)**:
- Settlement 最优标准、舍入规则、创建者删除策略、MQ 选择、审计范围、统计时间维度已明确

---

## Work Objectives

### Core Objective
交付可用于简历展示的共享记账后端 MVP，具备完整协作记账、结算、通知、统计与审计能力，并内建 MQ 与 Docker/CI 学习路径。

### Concrete Deliverables
- 完整 REST API + Swagger
- Jimmer 实体 & Repository + Service 层实现
- 结算算法（最少转账次数）
- RabbitMQ 异步处理（通知/结算/统计/审计）
- Docker Compose（Postgres + RabbitMQ）
- 测试与 CI 基础

### Definition of Done
- [ ] 本地 `./mvnw test` 通过
- [ ] 启动后通过 Swagger 可调用所有 MVP 端点
- [ ] 账本协作、邀请、记账、结算、统计、通知功能可完整跑通

### Must Have
- 账本多成员共享 + 邀请 + 不可退出
- 所有成员可改条目，创建者可改账本
- 结算最少转账次数
- 通知已读/未读 + 保留半年 + 可删除
- 统计维度完整

### Must NOT Have (Guardrails)
- 不实现前端/客户端
- 不实现 AI/LLM 接入
- 不实现分布式事务
- 不在 MVP 中启用真正多币种逻辑（仅字段预留）
- 不做微服务拆分（仅结构预留）
- 不做发票/附件上传
- Git 规范：遵循《Shared Ledger 项目 Git 使用规范（最终版）》

---

## Git 使用规范（项目级）

### 1. 分支策略
- 主开发分支：`dev`
- 功能分支：`feat-YYMMDD-<feature-name>`
  - 示例：`feat-260205-ledger-invite`
- 热修分支：`hotfix-YYMMDD-<fix-name>`
  - 示例：`hotfix-260210-login-bug`

### 2. 提交规范（Conventional + 中文）
- 使用类型：`feat | fix | docs | refactor | test | chore | perf`
- 提交标题中文；技术词汇保留英文原文

**示例：**
```
feat(ledger): 添加账本邀请接口
fix(auth): 修复 JWT token 刷新逻辑
docs(api): 更新 Swagger 文档示例
```

### 3. Commit Body（必须）
每次提交必须写 commit body，说明：
- 为什么改（原因/动机）
- 影响范围（模块/接口/逻辑）
- 是否有特殊注意事项（如兼容性、迁移）

### 4. 合并策略
- 功能完成后：使用 `git merge --squash` 合并 feature 分支到 `dev`
- 不允许 `rebase` / `amend`

### 5. 远程策略
- feature/hotfix 分支：**不 push 远程**
- dev 分支：允许 push 远程

### 6. Tag / Release 规范
- 每个里程碑版本打 tag：`vX.Y.Z`
  - 例：`v0.1.0`（MVP 阶段完成）
  - 例：`v0.2.0`（结算+通知完成）
- Tag 必须基于 dev 分支最新 squash 提交

---

## Global Execution Rules（执行强制规则）

1) **所有 Git 操作必须使用 `git-master` skill**
2) **所有 Git 操作必须遵循本计划中的 Git 使用规范**
3) 任何违反 Git 规范的操作都视为阻塞

### Guardrails: MVP 规模上限（需确认数值）
- 账本成员上限: 50 人/账本
- 账本条目上限: 10000 条/账本
- 自定义类型上限: 100 个/账本
- 邀请有效期: 72 小时

---

## Verification Strategy (MANDATORY)

### Test Decision
- **Infrastructure exists**: YES (JUnit5 + Spring Boot Test)
- **User wants tests**: Tests-after (已有测试基础，新增关键场景测试)
- **Framework**: Spring Boot Test (JUnit5)

### Automated Verification (Agent-executable)

**API/Backend** (curl):
```bash
./mvnw test

# Auth
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"u1","password":"p1"}'

curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"u1","password":"p1"}' | jq '.token'
```

**Evidence**:
- Swagger UI 可浏览并调用
- curl 响应包含成功字段

---

## Execution Strategy

### Phased Learning Roadmap (5 months, ≤1h/day)

> 目标：每天最多 1 小时，允许断续；每阶段以“可演示的小闭环”结束。

**Phase 1 (Weeks 1–4): Core Domain MVP (单体核心功能)**
- 重点：数据模型 + 账本/成员/邀请/记账条目基础流程
- 产出：
  - Jimmer 实体 & Repository（基于 myapp_schema_init.sql）
  - 账本 CRUD + 成员邀请/接受/拒绝（不可退出）
  - 记账条目 CRUD + 类型管理（系统预置 + 自定义）
  - 基础 Swagger 文档可用

**Phase 1 拆解（每周≤7小时）**

**Week 1：数据模型与基础仓库层**
- 完成核心实体与 Repository（User/Role/UserRole 复用 + Ledger/Member/Entry/Category/Invitation/Settlement/Notification/Audit）
- 建立最小 DTO 与枚举（状态/类型）
- 目标：`./mvnw test` 能编译通过，Swagger 启动无错误

**Week 2：账本与成员邀请闭环**
- 账本 CRUD（创建/改名/删除仅创建者）
- 成员邀请/接受/拒绝 + 不可退出规则
- 审计日志记录基础事件（账本/成员）
- 目标：账本创建者可邀请，受邀者可加入

**Week 3：记账条目与类型管理**
- 记账条目 CRUD（成员可编辑）
- 条目类型（系统预置 + 自定义）
- 审计日志覆盖条目操作
- 目标：共享账本内可完整记账

**Week 4：MVP 验收与文档补齐**
- Swagger 文档完善（示例请求/响应）
- 关键路径测试补齐（邀请/记账/权限）
- README 运行说明完善
- 目标：核心流程可演示并记录

**Phase 2 (Weeks 5–8): Settlement + Audit + Notifications (同步版)**
- 重点：结算算法（最少转账次数）+ 审计日志 + 通知表
- 产出：
  - 结算端点与算法
  - 审计日志记录与查询（管理员/创建者）
  - 通知中心（已读/未读、分类、删除、半年保留）

**Phase 3 (Weeks 9–12): RabbitMQ 异步化**
- 重点：消息队列落地（RabbitMQ）
- 产出：
  - 通知异步发送
  - 结算/统计/审计事件异步化
  - MQ 监控与基本可观测日志

**Phase 4 (Weeks 13–16): 统计报表增强 + 质量保障**
- 重点：统计报表多维度 + 测试与稳定性
- 产出：
  - 按类型/成员/时间区间（周/月/自定义）的统计报表
  - 测试覆盖关键路径（结算、邀请、权限）
  - 文档完善（运行/部署/接口说明）

**Phase 5 (Weeks 17–20): DevOps/容器化/CI (为微服务做准备)**
- 重点：Docker + CI/CD 基础
- 产出：
  - Docker Compose（Postgres + RabbitMQ）
  - CI 运行测试（GitHub Actions/类似）
  - 基础发布流程说明

**Phase 6 (Weeks 21–24): 微服务拆分（首批服务边界）**
- 重点：单体到服务拆分（用户/账本/记账/通知/结算）
- 产出：
  - 服务边界与 API 契约
  - 基础服务间通信（同步 REST 或消息）

**Phase 7 (Weeks 25–28): 分布式事务**
- 重点：跨服务一致性（如邀请→加入→通知）
- 产出：
  - 分布式事务方案验证（如 Saga/补偿）

**Phase 8 (Weeks 29–32): 多币种真实逻辑**
- 重点：多币种结算与换算策略
- 产出：
  - 货币字段启用 + 汇率策略 + 结算调整

**Phase 9 (Weeks 33–36): AI/LLM 接入（加分项）**
- 重点：AI 辅助分析或记账助手

**Phase 10 (Weeks 37–40): 前端/客户端轻量展示**
- 重点：简易 UI 展示（可仅做管理面板或移动端原型）

### Parallel Execution Waves

```
Wave 1:
├── Task 1: 数据模型与实体（Jimmer + DB）
├── Task 2: 通知表与审计表设计

Wave 2:
├── Task 3: 账本/成员/邀请核心 API
├── Task 4: 记账条目与类型管理 API
├── Task 5: 结算算法与端点

Wave 3:
├── Task 6: MQ 异步处理 (RabbitMQ)
├── Task 7: 统计报表端点
├── Task 8: Docker + CI + Docs
```

---

## TODOs

### 1. 设计并实现核心数据模型（Jimmer Entities + Repos）
**What to do**:
- 以 `myapp_schema_init.sql` 为权威，完成 Jimmer 实体与 Repository
- 确保 nullability 与 DB 严格匹配
- 预留 currency 字段但 MVP 仅允许 RMB

**Recommended Agent Profile**:
- Category: `unspecified-high`
- Skills: []

**References**:
- `src/main/resources/sql_script/myapp_schema_init.sql` — 作为实体字段与约束来源
- `src/main/java/com/xdw/demobackend/entity/*` — 现有 Jimmer 实体模式

**Acceptance Criteria**:
- [x] 实体/Repository 完整覆盖账本、成员、条目、邀请、结算
- [x] Jimmer 编译无 nullability 错误

---

### 2. 新增通知表与审计日志表
**What to do**:
- 补齐 notifications 表：类型、已读/未读、半年保留、可删除
- 审计日志表记录账本/成员/条目/结算操作

**Acceptance Criteria**:
- [x] Schema 更新与实体同步
- [x] 通知支持 read/unread & delete

---

### 3. 账本/成员/邀请 API
**What to do**:
- 账本 CRUD + 创建者权限控制
- 邀请/接受/拒绝流程
- 加入后不可退出

**Acceptance Criteria**:
- [ ] 创建/邀请/加入流程可跑通

---

### 4. 记账条目与类型管理 API
**What to do**:
- 条目 CRUD
- 类型管理（系统默认 + 自定义）

**Acceptance Criteria**:
- [ ] 条目可由任意成员编辑

---

### 5. 结算算法与端点
**What to do**:
- 按最少转账次数计算
- 均摊 + 多 1 分由付款人承担

**Acceptance Criteria**:
- [ ] 结算返回正确转账列表

---

### 6. RabbitMQ 异步处理
**What to do**:
- 通知、结算、统计、审计事件异步化

**Acceptance Criteria**:
- [ ] 消息入队后有可追踪消费日志

---

### 7. 统计报表端点
**What to do**:
- 按类型/成员/时间区间统计

**Acceptance Criteria**:
- [ ] 周/月/自定义区间统计可查询

---

### 8. Docker + CI + 文档
**What to do**:
- Docker Compose for Postgres + RabbitMQ
- CI 运行测试
- README 补充运行方式

**Acceptance Criteria**:
- [ ] `docker compose up` 可启动依赖
- [ ] CI 能运行 ./mvnw test

**Recommended Agent Profile**:
- Category: `unspecified-high`
- Skills: ["git-master"]

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 1 | feat(model): add ledger domain entities | entity/repo | ./mvnw test |

---

## Success Criteria

- [ ] 所有 MVP 功能端点可通过 Swagger 调用
- [ ] 结算逻辑正确（最少转账次数）
- [ ] RabbitMQ 异步通知可追踪
- [ ] Docker Compose 与 CI 可运行
