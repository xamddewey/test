# MyBatis-Flex to Jimmer ORM Migration Plan

**Project**: demo-backend (Spring Boot 3.4.5, Java 21, PostgreSQL)  
**Current State**: Using MyBatis-Flex 1.11.1  
**Target**: Migrate to Jimmer ORM 0.9.120  
**Date**: 2026-02-02

---

## Migration Overview

This plan outlines the complete migration from MyBatis-Flex to Jimmer ORM, a modern type-safe ORM with GraphQL-style object fetching capabilities.

### Key Changes
- **Entity Definition**: Class → Interface (immutable, dynamically generated)
- **Query Layer**: QueryWrapper → Type-safe DSL with compile-time checking
- **Repository**: BaseMapper → JRepository with Spring Data style
- **Associations**: Explicit SQL → Declarative `@ManyToOne`, `@OneToMany`, `@ManyToMany`

---

## Current Architecture (MyBatis-Flex)

### Entities (3)
- `User.java` - @Table("users"), 10 fields, timestamps
- `Role.java` - @Table("roles"), 6 fields, timestamps
- `UserRole.java` - Join entity (user_roles), missing @Table annotation

### Mappers (3)
- `UserMapper.java` - BaseMapper<User> + 5 custom @Select methods
- `RoleMapper.java` - BaseMapper<Role> + 2 custom @Select + 1 QueryWrapper method
- `UserRoleMapper.java` - BaseMapper<UserRole> + helper insert method

### Services Using Mappers
- `AuthServiceImpl.java` - Uses all 3 mappers (registration, login, token refresh)
- `UserDetailsServiceImpl.java` - Uses UserMapper + RoleMapper (Spring Security)

### Configuration
- `MyBatisFlexConfig.java` - Customizer with audit enabled
- `application.properties` - MyBatis-Flex settings, PostgreSQL connection

### Database
- PostgreSQL 15432/myapp_db
- Schema: `src/main/resources/sql_script/myapp_schema_init.sql`
- No Flyway/Liquibase (manual schema management)

---

## Migration Tasks

### Phase 1: Project Setup & Dependencies

- [ ] **Task 1.1**: Update pom.xml - Add Jimmer dependencies and remove MyBatis-Flex
  - Parallelizable: No (foundation for all other tasks)
  - Add: jimmer-spring-boot-starter (0.9.120)
  - Add: jimmer-apt annotation processor to compiler plugin
  - Remove: mybatis-flex-spring-boot-starter, mybatis-flex-processor, mybatis-flex-codegen
  - Remove: pagehelper-spring-boot-starter (Jimmer has built-in pagination)
  - Keep: PostgreSQL driver, HikariCP, Lombok, Spring Boot starters

- [ ] **Task 1.2**: Update application.properties - Replace MyBatis-Flex config with Jimmer
  - Parallelizable: No (depends on 1.1)
  - Remove: mybatis-flex.* properties
  - Add: jimmer.dialect=org.babyfish.jimmer.sql.dialect.PostgresDialect
  - Add: jimmer.show-sql=true, jimmer.pretty-sql=true
  - Add: jimmer.database-validation-mode=ERROR
  - Keep: spring.datasource.* (unchanged)

- [ ] **Task 1.3**: Delete MyBatisFlexConfig.java and Codegen.java
  - Parallelizable: No (depends on 1.2)
  - Delete: src/main/java/com/xdw/demobackend/config/MyBatisFlexConfig.java
  - Delete: src/main/java/com/xdw/demobackend/codegen/Codegen.java
  - Delete: src/main/resources/mapper/ directory (if empty)

### Phase 2: Entity Conversion (Interface-based, Immutable)

- [ ] **Task 2.1**: Convert User entity from class to interface
  - Parallelizable: Yes (independent of other entity conversions)
  - Change: Class → Interface
  - Change: @Table("users") → @Entity + @Table(name = "users") if needed
  - Change: All fields → getter methods (no setters)
  - Change: @Id(keyType = KeyType.Auto) → @Id @GeneratedValue(strategy = IDENTITY)
  - Remove: @Builder, @AllArgsConstructor (Jimmer generates Draft API)
  - Keep: @Data, @NoArgsConstructor (for Jimmer compatibility)
  - Add: @Nullable for optional fields (avatar, bio, phone)
  - Update timestamps: @Column(onInsertValue) → Jimmer's @OnDissociate pattern or database defaults
  - Dependencies: None

- [ ] **Task 2.2**: Convert Role entity from class to interface
  - Parallelizable: Yes (independent of other entity conversions)
  - Same transformations as Task 2.1
  - Fields: id, roleName, description, roleType, createdAt, updatedAt
  - Add: @Nullable for description if optional
  - Dependencies: None

- [ ] **Task 2.3**: Convert UserRole entity from class to interface
  - Parallelizable: Yes (independent of other entity conversions)
  - Add missing: @Entity @Table(name = "user_roles")
  - Change: Class → Interface
  - Add associations: @ManyToOne references to User and Role
  - Fields: id, userId/user, roleId/role, createdAt, updatedAt
  - Decision: Keep as explicit entity or use @ManyToMany on User/Role? (Document in decisions.md)
  - Dependencies: None

- [ ] **Task 2.4**: Define entity associations (User ↔ Role relationship)
  - Parallelizable: No (depends on 2.1, 2.2, 2.3)
  - Option A: Keep UserRole explicit entity + @ManyToOne from UserRole to User/Role
  - Option B: Add @ManyToMany on User/Role interfaces, use @JoinTable
  - Decision: Use Option A (preserve explicit join entity for audit timestamps)
  - Add: User.java → `@OneToMany(mappedBy = "user") List<UserRole> userRoles()`
  - Add: Role.java → `@OneToMany(mappedBy = "role") List<UserRole> userRoles()`
  - Dependencies: Tasks 2.1, 2.2, 2.3

### Phase 3: Repository Layer (Replace Mappers)

- [ ] **Task 3.1**: Create UserRepository extending JRepository<User, Long>
  - Parallelizable: Yes (independent of other repositories)
  - Create: src/main/java/com/xdw/demobackend/repository/UserRepository.java
  - Extend: JRepository<User, Long>
  - Migrate methods from UserMapper:
    - `Optional<User> findByUsername(String username)` → Spring Data method name convention
    - `Optional<User> findByEmail(String email)` → Spring Data method name convention
    - `List<Role> findRolesByUserId(Long id)` → Remove (use association fetching instead)
    - `boolean existsByUsername(String username)` → Spring Data method name convention
    - `boolean existsByEmail(String email)` → Spring Data method name convention
  - Dependencies: Task 2.1 (User entity)

- [ ] **Task 3.2**: Create RoleRepository extending JRepository<Role, Long>
  - Parallelizable: Yes (independent of other repositories)
  - Create: src/main/java/com/xdw/demobackend/repository/RoleRepository.java
  - Extend: JRepository<Role, Long>
  - Migrate methods from RoleMapper:
    - `Optional<Role> findByRoleName(String roleName)` → Spring Data method or default method with DSL
    - `List<Role> findByUserId(Long userId)` → Remove (use association fetching from User)
    - `Role findByNameWithFlex(String name)` → Replace QueryWrapper with Jimmer DSL
  - Dependencies: Task 2.2 (Role entity)

- [ ] **Task 3.3**: Create UserRoleRepository extending JRepository<UserRole, Long>
  - Parallelizable: Yes (independent of other repositories)
  - Create: src/main/java/com/xdw/demobackend/repository/UserRoleRepository.java
  - Extend: JRepository<UserRole, Long>
  - Migrate methods from UserRoleMapper:
    - `addUserRole(Long userId, Long roleId)` → Use Jimmer's save() with Draft API
  - Or: Remove this repository if using @ManyToMany (decision in 2.4)
  - Dependencies: Task 2.3 (UserRole entity)

### Phase 4: Service Layer Updates

- [ ] **Task 4.1**: Update AuthServiceImpl to use repositories instead of mappers
  - Parallelizable: No (depends on 3.1, 3.2, 3.3)
  - Replace: UserMapper → UserRepository injection
  - Replace: RoleMapper → RoleRepository injection
  - Replace: UserRoleMapper → UserRoleRepository injection (or remove if @ManyToMany)
  - Update: register() method
    - userMapper.existsByUsername() → userRepository.existsByUsername()
    - userMapper.insert(user) → userRepository.save(UserDraft.$.produce(draft -> {...}))
    - roleMapper.findByRoleName() → roleRepository.findByRoleName()
    - userRoleMapper.addUserRole() → userRoleRepository.save() or association management
  - Update: login() method (minimal changes, uses AuthenticationManager)
  - Update: refreshToken() method
    - userMapper.findByUsername() → userRepository.findByUsername()
    - roleMapper.findByUserId() → Use association fetching instead
  - Dependencies: Tasks 3.1, 3.2, 3.3

- [ ] **Task 4.2**: Update UserDetailsServiceImpl to use repositories
  - Parallelizable: No (depends on 3.1, 3.2)
  - Replace: UserMapper → UserRepository injection
  - Replace: RoleMapper → RoleRepository injection (or remove, use association)
  - Update: loadUserByUsername() method
    - userMapper.findByUsername() → userRepository.findByUsername()
    - roleMapper.findByUserId() → Fetch user with roles association in one query
    - Use Jimmer Fetcher to load user + roles in single query
  - Dependencies: Tasks 3.1, 3.2

- [ ] **Task 4.3**: Implement UserService (currently only interface exists)
  - Parallelizable: No (depends on 3.1)
  - Create: src/main/java/com/xdw/demobackend/service/user/impl/UserServiceImpl.java
  - Implement all methods from UserService interface using UserRepository
  - Use Jimmer's save() and Draft API for updates
  - Use Jimmer Fetcher for complex queries
  - Dependencies: Task 3.1

### Phase 5: Delete Old Mapper Files

- [ ] **Task 5.1**: Delete all mapper interfaces and generated files
  - Parallelizable: No (depends on 4.1, 4.2, 4.3 - ensure no references remain)
  - Delete: src/main/java/com/xdw/demobackend/mapper/UserMapper.java
  - Delete: src/main/java/com/xdw/demobackend/mapper/RoleMapper.java
  - Delete: src/main/java/com/xdw/demobackend/mapper/UserRoleMapper.java
  - Delete: target/generated-sources/annotations/com/xdw/demobackend/entity/table/ (if exists)
  - Verify: No references to mapper classes remain in codebase
  - Dependencies: Tasks 4.1, 4.2, 4.3

### Phase 6: Testing & Verification

- [ ] **Task 6.1**: Compile project and verify Jimmer APT code generation
  - Parallelizable: No (depends on all entity conversions)
  - Run: mvn clean compile
  - Verify: target/generated-sources/annotations/ contains Jimmer-generated classes:
    - UserDraft.java, UserTable.java, UserFetcher.java
    - RoleDraft.java, RoleTable.java, RoleFetcher.java
    - UserRoleDraft.java, UserRoleTable.java, UserRoleFetcher.java
  - Check: No compilation errors
  - Dependencies: Tasks 2.1, 2.2, 2.3, 2.4

- [ ] **Task 6.2**: Run application and verify database connectivity
  - Parallelizable: No (depends on 6.1)
  - Run: mvn spring-boot:run
  - Verify: Application starts without errors
  - Verify: Jimmer SQL logs show correct PostgreSQL dialect
  - Verify: Database connection pool initializes (HikariCP)
  - Check: No entity mapping errors in logs
  - Dependencies: Task 6.1

- [ ] **Task 6.3**: Test user registration flow
  - Parallelizable: No (depends on 6.2)
  - Test: POST /api/auth/register with new user
  - Verify: User created in database
  - Verify: UserRole association created
  - Verify: Jimmer SQL logs show INSERT statements
  - Check: No runtime errors
  - Dependencies: Task 6.2

- [ ] **Task 6.4**: Test user login and authentication flow
  - Parallelizable: No (depends on 6.3)
  - Test: POST /api/auth/login with existing user
  - Verify: JWT token returned
  - Verify: User roles loaded correctly (via association or query)
  - Verify: Jimmer SQL logs show efficient queries (no N+1)
  - Check: Spring Security integration works
  - Dependencies: Task 6.3

- [ ] **Task 6.5**: Run existing unit/integration tests
  - Parallelizable: No (depends on 6.2)
  - Run: mvn test
  - Update tests if needed to use repositories instead of mappers
  - Verify: All tests pass
  - Check: Test coverage maintained
  - Dependencies: Task 6.2

### Phase 7: Optimization & Documentation

- [ ] **Task 7.1**: Optimize queries with Jimmer Fetchers
  - Parallelizable: No (depends on 6.4)
  - Review: All service methods that load associations
  - Add: Fetcher specifications to load exactly what's needed
  - Example: Load user + roles in single query instead of separate queries
  - Verify: SQL logs show reduced query count (no N+1 problem)
  - Dependencies: Task 6.4

- [ ] **Task 7.2**: Update README with Jimmer migration notes
  - Parallelizable: Yes (documentation)
  - Add: Section explaining Jimmer entity interfaces
  - Add: Instructions for running annotation processor
  - Add: Notes about Draft API for entity creation/updates
  - Add: Link to Jimmer documentation
  - Dependencies: None (can be done anytime after completion)

- [ ] **Task 7.3**: Configure Jimmer caching (optional, future enhancement)
  - Parallelizable: Yes (optional optimization)
  - Research: Jimmer multi-level caching configuration
  - Add: Cache configuration if needed (Redis, Caffeine)
  - Skip if: Caching not required initially
  - Dependencies: None

---

## Migration Notes

### Critical Success Factors
1. **Annotation Processor**: jimmer-apt MUST be configured in maven-compiler-plugin
2. **Entity Interfaces**: All entities must be interfaces (not classes)
3. **Immutability**: Use Draft API for creating/updating entities (no setters)
4. **Association Fetching**: Use Fetchers to avoid N+1 queries

### Rollback Plan
- Keep MyBatis-Flex code in separate branch before deletion
- Tag commit before starting migration: `git tag pre-jimmer-migration`
- If critical issues: revert to MyBatis-Flex and reassess

### Risk Mitigation
- Test each phase incrementally (don't migrate everything at once)
- Keep database schema unchanged (Jimmer uses same tables)
- Verify SQL logs after each change (ensure queries are correct)
- Run tests frequently

---

## Parallelization Map

**Parallel Groups:**
- **Group 1 (Phase 2)**: Tasks 2.1, 2.2, 2.3 can run in parallel (entity conversions)
- **Group 2 (Phase 3)**: Tasks 3.1, 3.2, 3.3 can run in parallel (repository creation)
- **Sequential**: Tasks 1.x → 2.4 → 3.x → 4.x → 5.x → 6.x → 7.x

**Total Tasks**: 23 checkboxes

---

## Estimated Timeline
- Phase 1: 1 hour (setup)
- Phase 2: 2 hours (entity conversion + associations)
- Phase 3: 2 hours (repository layer)
- Phase 4: 3 hours (service layer updates)
- Phase 5: 30 minutes (cleanup)
- Phase 6: 2 hours (testing)
- Phase 7: 1 hour (optimization)

**Total**: ~11.5 hours of development work

---

**Plan Status**: Ready for execution  
**Next Step**: Execute Task 1.1 (Update pom.xml)
