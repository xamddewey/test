## DatabaseValidationException on Timestamp Fields (RESOLVED)

### Issue
Jimmer's strict database validation threw `DatabaseValidationException` errors because `createdAt` and `updatedAt` fields were declared as non-null in the entity interfaces, but were nullable in the actual database schema.

**Error Messages:**
```
Failed to validate database: 
- com.xdw.demobackend.entity.Role.createdAt: The property is nonnull, but the database column "CREATED_AT" in table "myapp_db.public.roles" is nullable
- com.xdw.demobackend.entity.Role.updatedAt: The property is nonnull, but the database column "UPDATED_AT" in table "myapp_db.public.roles" is nullable
- com.xdw.demobackend.entity.User.createdAt: The property is nonnull, but the database column "CREATED_AT" in table "myapp_db.public.users" is nullable
- com.xdw.demobackend.entity.User.updatedAt: The property is nonnull, but the database column "UPDATED_AT" in table "myapp_db.public.users" is nullable
- com.xdw.demobackend.entity.UserRole.createdAt: The property is nonnull, but the database column "CREATED_AT" in table "myapp_db.public.user_roles" is nullable
- com.xdw.demobackend.entity.UserRole.updatedAt: The property is nonnull, but the database column "UPDATED_AT" in table "myapp_db.public.user_roles" is nullable
```

### Root Cause
- Original MyBatis-Flex schema used `@Column(onInsertValue = "CURRENT_TIMESTAMP")` to auto-populate audit timestamps
- These fields were NOT enforced as NOT NULL constraints in the actual database DDL
- Jimmer's `database-validation-mode=ERROR` strictly validates that entity field nullability matches the actual database schema
- Mismatch = **startup failure** when in ERROR mode

### Solution
Added `@Nullable` annotation to `createdAt()` and `updatedAt()` methods in all three entity interfaces:
- User.java
- Role.java
- UserRole.java

**Pattern Applied:**
```java
// Before (causing DatabaseValidationException)
LocalDateTime createdAt();
LocalDateTime updatedAt();

// After (matches database schema nullability)
@Nullable
LocalDateTime createdAt();

@Nullable
LocalDateTime updatedAt();
```

### Files Modified
1. ✅ `src/main/java/com/xdw/demobackend/entity/User.java` - Added @Nullable to both methods
2. ✅ `src/main/java/com/xdw/demobackend/entity/Role.java` - Added @Nullable to both methods
3. ✅ `src/main/java/com/xdw/demobackend/entity/UserRole.java` - Added @Nullable to both methods + added import

### Verification Results
✅ **Application starts successfully**
```
Started DemoBackendApplication in 1.983 seconds
```
✅ **No DatabaseValidationException errors**
✅ **Database validation passed**
✅ **Compilation successful with no errors**

### Key Lesson for MyBatis-Flex to Jimmer Migration

When migrating from MyBatis-Flex to Jimmer with `database-validation-mode=ERROR`:

1. **Entity fields must match database schema exactly** - This includes nullability constraints
2. **Audit fields are often nullable in legacy systems** - Even though they're auto-populated by DEFAULT CURRENT_TIMESTAMP, they're still nullable if no NOT NULL constraint exists
3. **Use @Nullable annotation** - Mark fields as nullable if the database column allows NULL values, regardless of application-layer expectations
4. **Default values != NOT NULL constraint** - A column with DEFAULT CURRENT_TIMESTAMP but no NOT NULL constraint is still nullable

### Configuration Context
```properties
# application.properties
jimmer.database-validation-mode=ERROR
```

This setting ensures strict schema validation, which is excellent for catching schema/code mismatches early but requires careful attention to database DDL details.

## Jimmer save() vs insert() Confusion (RESOLVED)

### Issue
User registration failed with "Cannot save illegal entity object whose type is User, entity with neither id nor key cannot be accepted" error.

### Root Cause
1. Jimmer's `save()` method is for upsert operations (requires ID or key) - not for creating new entities
2. For INSERT operations on new entities without IDs, must use `insert()` method
3. After entity migration, UserRole uses `@ManyToOne` associations, not foreign key fields
4. The User entity requires a `nickname` field which was missing from the registration logic
5. When setting associations with full objects, Jimmer tries to cascade insert related entities, causing duplicate key conflicts

### Solution Applied
1. Changed `userRepository.save()` → `userRepository.insert()` for new user creation
2. Changed `userRoleRepository.save()` → `userRoleRepository.insert()` for new user role creation
3. Added `draft.setNickname(registerRequest.getUsername())` to satisfy NOT NULL constraint
4. Changed association setting pattern:
   - Before: `draft.setUser(saved)` and `draft.setRole(role)` - triggers cascading inserts
   - After: Create ID-only references using nested produces to prevent cascading:
     ```java
     draft.setUser(UserDraft.$.produce(u -> u.setId(saved.id())))
     draft.setRole(RoleDraft.$.produce(r -> r.setId(role.id())))
     ```

### Key Learnings
1. **Jimmer Operations**: 
   - `insert()` = for new entities (no ID required, auto-generated)
   - `save()` = for upsert/merge operations (requires ID or key properties)
   - `update()` = for updating existing entities (requires ID)

2. **Association Handling**:
   - Setting full entity objects in drafts can trigger cascading operations
   - Create ID-only references when you only want to link existing entities
   - Use nested `produce()` to create minimal reference objects

3. **Entity Schema**:
   - All non-@Nullable fields are required when creating new entities
   - Review entity interfaces for @Nullable annotations when building drafts

### Verification
- User registration endpoint: ✅ HTTP 200 success
- Database insert: ✅ User created with ID 3, username=testuser005
- Role assignment: ✅ UserRole created linking user to LEDGER_PARTICIPANT role
- No duplicate key errors: ✅ ID-only references prevent cascading inserts
