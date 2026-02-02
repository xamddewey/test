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
