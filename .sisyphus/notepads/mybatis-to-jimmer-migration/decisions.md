# Architectural Decisions - MyBatis-Flex to Jimmer Migration

## Task 2.4: Entity Associations (UserRole ↔ User, UserRole ↔ Role)

### Decision: Keep Explicit UserRole Entity (Not @ManyToMany)

**Status**: ✅ Implemented

**Context**:
- Traditional ORM approach would use `@ManyToMany` with auto-generated join table
- Our schema has an explicit `user_roles` table with audit timestamps (createdAt, updatedAt)
- These timestamps are business-critical for tracking when role assignments were made/modified

**Decision**: Use explicit UserRole entity with `@ManyToOne` associations instead of `@ManyToMany`

**Rationale**:
1. **Preserves Audit Timestamps**: UserRole entity has createdAt/updatedAt fields that track assignment lifecycle
2. **Jimmer-Native Pattern**: Aligns with Jimmer's philosophy of explicit domain modeling
3. **Database Schema Fidelity**: Matches the actual database schema without abstraction
4. **Future Extensibility**: Can easily add more fields to UserRole (permissions, expiry dates, etc.)

### Bidirectional Association Pattern

**Implementation**:

```
UserRole (Owning Side - Has Foreign Keys)
├── @ManyToOne User user()          // FK: user_id → users.id
└── @ManyToOne Role role()          // FK: role_id → roles.id

User (Inverse Side - No FK)
└── @OneToMany(mappedBy = "user") List<UserRole> userRoles()

Role (Inverse Side - No FK)
└── @OneToMany(mappedBy = "role") List<UserRole> userRoles()
```

**Key Points**:
- **UserRole is the "owner"**: Contains @ManyToOne annotations (holds foreign keys)
- **User and Role are "inverse"**: Contain @OneToMany with mappedBy parameter
- **mappedBy**: Tells Jimmer to look at the "user" property on UserRole (and "role" for Role)
- **No @JoinColumn needed**: Jimmer infers column names from property names using naming convention:
  - `User user()` → foreign key column: `user_id`
  - `Role role()` → foreign key column: `role_id`

### Import Requirements

Added to User.java and Role.java:
- `import java.util.List;` - For List<UserRole> return type
- Jimmer annotations already present:
  - `@OneToMany` from `org.babyfish.jimmer.sql.*`
  - `@ManyToOne` from `org.babyfish.jimmer.sql.*` (already in UserRole)

### Files Modified

1. **UserRole.java**:
   - Replaced: `long userId()` and `long roleId()`
   - Added: `@ManyToOne User user()` and `@ManyToOne Role role()`
   - Kept: id, createdAt, updatedAt (audit fields)

2. **User.java**:
   - Added: `@OneToMany(mappedBy = "user") List<UserRole> userRoles()`
   - Kept: All 8 existing properties unchanged

3. **Role.java**:
   - Added: `@OneToMany(mappedBy = "role") List<UserRole> userRoles()`
   - Kept: All 6 existing properties unchanged

### Verification

✅ All three entities compile successfully with Jimmer APT processor:
- `[INFO] Entity: com.xdw.demobackend.entity.User`
- `[INFO] Entity: com.xdw.demobackend.entity.UserRole`
- `[INFO] Entity: com.xdw.demobackend.entity.Role`

✅ No compilation errors in entity files

✅ Bidirectional associations properly defined:
- userRoles() method references are correct on both sides
- mappedBy values correctly reference the owning side properties

### Related Tasks

- **Depends On**: Tasks 2.1, 2.2, 2.3 (all entities converted to interfaces)
- **Enables**: Task 3 (Repository implementations will leverage these associations)
- **Future**: Phase 3 will add timestamp interceptors for audit fields

### Naming Conventions Applied

Following Jimmer's naming inference:
- **User entity** property `user()` → foreign key column `user_id`
- **Role entity** property `role()` → foreign key column `role_id`
- **No underscore transformation** needed (already follows snake_case convention in DB)

### Code Quality Notes

- JavaDoc comments added to association properties for IDE/developer understanding
- Comments explain which side owns the relationship
- Comments document the mappedBy direction for maintainability
- Follows established Jimmer patterns from learnings.md

## Task 7.3: Configure Jimmer Caching (Optional, Future Enhancement) - COMPLETED

### Decision: Safe Default Caching Configuration

**Status**: ✅ Completed

**Context**:
- Jimmer supports multi-level caching (object cache, association cache, calculated cache)
- Caching provides significant performance improvements (30%+ in some scenarios)
- Redis infrastructure NOT currently available in this project
- Need to provide foundation for future caching implementation without breaking current functionality

**Decision**: Add comprehensive caching configuration section to application.properties with safe defaults

**Rationale**:
1. **Safe by Default**: Caching is disabled by default (jimmer.cache.enabled=false)
2. **Clear Path Forward**: Developers can easily enable caching when infrastructure is available
3. **Well Documented**: Extensive comments explain caching options and setup requirements
4. **No Breaking Changes**: Existing application.properties functionality unchanged
5. **Future-Proof**: Foundation for when caching infrastructure becomes available

### Configuration Added to application.properties

**Caching Types Documented**:

1. **Disabled (Default)**
   - Comment: `jimmer.cache.enabled=false`
   - Status: Safe, recommended for current deployment
   - Reason: Simplicity, no external dependencies

2. **In-Memory Caching (Development/Testing)**
   - Cache Type: Caffeine
   - Configuration shown:
     ```properties
     jimmer.cache.enabled=true
     jimmer.cache.type=caffeine
     jimmer.cache.caffeine.maximum-size=1000
     ```
   - Use Case: Single-instance applications, development/testing
   - Advantage: No external dependencies

3. **Redis Caching (Production/Distributed)**
   - Cache Type: Redis
   - Configuration shown:
     ```properties
     jimmer.cache.enabled=true
     jimmer.cache.type=redis
     jimmer.cache.redis.host=localhost
     jimmer.cache.redis.port=6379
     jimmer.cache.redis.database=0
     jimmer.cache.redis.password=  # Optional
     ```
   - Use Case: Multi-instance, production deployments
   - Advantages: Distributed cache, automatic invalidation across instances
   - Prerequisites clearly documented:
     - Redis server must be running
     - spring-boot-starter-data-redis dependency required
     - Connection parameters must match environment

### Documentation Provided

**Setup Instructions**:
- Link to Jimmer caching documentation: https://babyfish-ct.github.io/jimmer-doc/docs/cache/
- Step-by-step Redis setup procedure:
  1. Add spring-boot-starter-data-redis dependency
  2. Uncomment and configure Redis connection details
  3. Restart application

**Important Warnings**:
- Cache adds complexity (mentioned in comments)
- Testing of cache invalidation scenarios required
- Stale data risk: only for predictable change patterns
- Start with small cache sizes, increase based on metrics
- Consider disabling for frequently-modified entities

### Cache Duration Configuration

- Default documented: 300000 ms (5 minutes)
- Can be customized: `jimmer.cache.duration=<milliseconds>`
- Example provided but commented out

### Files Modified

✅ `/Users/dewey/SharedLedger/demo-backend/src/main/resources/application.properties`

**Section Added**: "Jimmer Caching Configuration (Optional)"
- 50+ lines of documentation and examples
- All configuration options commented out (safe)
- Clear separation from existing configuration
- Organized with headers for readability

### Verification

✅ Application compiles successfully after changes
- Build time: 1.937 seconds
- No errors or warnings related to configuration
- Jimmer APT processor runs successfully
- All entities recognized correctly

### Why Safe Configuration Matters

1. **No Infrastructure Dependencies**: Doesn't require Redis setup
2. **Doesn't Break Existing Behavior**: All caching disabled by default
3. **Self-Documenting**: Future developers can see options without external docs
4. **Easy Enablement**: Single line change to enable when ready
5. **Learning Resource**: Comments explain caching concepts and trade-offs

### Architecture Decision

Instead of complex caching infrastructure upfront, we:
1. ✅ Document all available caching options
2. ✅ Show exact configuration needed for each option
3. ✅ Keep defaults safe (disabled)
4. ✅ Provide clear path to enable when needed
5. ✅ Link to official documentation for details

This approach balances:
- **Immediate Need**: Application works as-is
- **Future Flexibility**: Easy to enable when infrastructure available
- **Knowledge Transfer**: Configuration self-documenting for team members
- **Best Practices**: Safe defaults, clear progression path

### Related Tasks

- Depends On: All previous tasks (1-21) completed successfully
- This is the FINAL task in the migration plan (Task 7.3)
- Enables: Future performance optimization when caching needed

### Migration Status: COMPLETE ✅

All 22 tasks from the MyBatis-Flex to Jimmer migration plan are now complete:
- ✅ Phase 1: Project Setup (pom.xml, application.properties, config cleanup)
- ✅ Phase 2: Entity Conversion (User, Role, UserRole to Jimmer interfaces)
- ✅ Phase 3: Repository Migration (JRepository implementations)
- ✅ Phase 4: Service Layer Updates (Draft API, Fetcher API)
- ✅ Phase 5: Cleanup (removed obsolete mappers and configuration)
- ✅ Phase 6: Testing & Verification (all tests passing)
- ✅ Phase 7: Optimization & Future Enhancement
  - ✅ 7.1: Eager Loading with Fetchers (Completed)
  - ✅ 7.2: Performance Optimization (Completed)
  - ✅ 7.3: Caching Configuration (Completed - This Task)

**Migration Complete**: Application fully migrated from MyBatis-Flex to Jimmer ORM 0.9.120
