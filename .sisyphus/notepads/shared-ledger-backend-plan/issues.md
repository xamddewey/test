
## RESOLVED: UnloadedException when Creating Ledger

### Issue
When creating a new ledger, the application threw an UnloadedException:
```
创建账本失败: The property "com.xdw.demobackend.entity.AccountLedger.lastExpenseDate" is unloaded
```

### Root Cause
- `AccountLedger` entity has multiple `@Nullable` fields: `lastExpenseDate`, `description`, `creatorNickname`, `memberCount`, `invitedCount`, `totalExpenses`, `recordCount`, `lastActivityAt`, `createdAt`, `updatedAt`
- When a new ledger is created and inserted via `repository.insert()`, only explicitly set fields are loaded
- Unloaded properties throw UnloadedException when accessed
- `LedgerResponse.fromEntity()` was unconditionally accessing all these nullable fields

### Solution
Used Jimmer's `ImmutableObjects.isLoaded()` check to safely access unloaded nullable fields:

**Changed:** `src/main/java/com/xdw/demobackend/dto/ledger/LedgerResponse.java`
- Added import: `import org.babyfish.jimmer.ImmutableObjects`
- Added import: `import com.xdw.demobackend.entity.AccountLedgerProps` (Jimmer generated)
- Wrapped all nullable field accesses with `ImmutableObjects.isLoaded(ledger, AccountLedgerProps.FIELD_NAME) ? ledger.field() : null`

### Pattern Used
```java
ImmutableObjects.isLoaded(ledger, AccountLedgerProps.LAST_EXPENSE_DATE)
    ? ledger.lastExpenseDate()
    : null
```

### Verification
✅ Ledger creation now succeeds without UnloadedException
✅ All nullable fields return `null` when unloaded (expected behavior)
✅ Non-nullable fields and @IdView fields always accessible

### Jimmer Best Practice
When accessing entities returned from `insert()`, `update()`, or queries without Fetcher:
1. Check if property is loaded using `ImmutableObjects.isLoaded(entity, EntityProps.FIELD)`
2. Return `null` for unloaded nullable fields (never set default values)
3. Only use Fetcher for eager-loading if multiple reads are needed

