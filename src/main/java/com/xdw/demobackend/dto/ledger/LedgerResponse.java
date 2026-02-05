package com.xdw.demobackend.dto.ledger;

import com.xdw.demobackend.entity.AccountLedger;
import com.xdw.demobackend.entity.AccountLedgerProps;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.babyfish.jimmer.ImmutableObjects;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 账本响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerResponse {
    
    private Long id;
    private String ledgerName;
    private String description;
    private Long creatorId;
    private String creatorNickname;
    private Integer memberCount;
    private Integer invitedCount;
    private BigDecimal totalExpenses;
    private Integer recordCount;
    private LocalDate lastExpenseDate;
    private LocalDateTime lastActivityAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 从AccountLedger实体转换为DTO
     */
    public static LedgerResponse fromEntity(AccountLedger ledger) {
        return LedgerResponse.builder()
                .id(ledger.id())
                .ledgerName(ledger.ledgerName())
                .description(
                    ImmutableObjects.isLoaded(ledger, AccountLedgerProps.DESCRIPTION)
                        ? ledger.description()
                        : null
                )
                .creatorId(ledger.creatorId())
                .creatorNickname(
                    ImmutableObjects.isLoaded(ledger, AccountLedgerProps.CREATOR_NICKNAME)
                        ? ledger.creatorNickname()
                        : null
                )
                .memberCount(
                    ImmutableObjects.isLoaded(ledger, AccountLedgerProps.MEMBER_COUNT)
                        ? ledger.memberCount()
                        : null
                )
                .invitedCount(
                    ImmutableObjects.isLoaded(ledger, AccountLedgerProps.INVITED_COUNT)
                        ? ledger.invitedCount()
                        : null
                )
                .totalExpenses(
                    ImmutableObjects.isLoaded(ledger, AccountLedgerProps.TOTAL_EXPENSES)
                        ? ledger.totalExpenses()
                        : null
                )
                .recordCount(
                    ImmutableObjects.isLoaded(ledger, AccountLedgerProps.RECORD_COUNT)
                        ? ledger.recordCount()
                        : null
                )
                .lastExpenseDate(
                    ImmutableObjects.isLoaded(ledger, AccountLedgerProps.LAST_EXPENSE_DATE)
                        ? ledger.lastExpenseDate()
                        : null
                )
                .lastActivityAt(
                    ImmutableObjects.isLoaded(ledger, AccountLedgerProps.LAST_ACTIVITY_AT)
                        ? ledger.lastActivityAt()
                        : null
                )
                .createdAt(
                    ImmutableObjects.isLoaded(ledger, AccountLedgerProps.CREATED_AT)
                        ? ledger.createdAt()
                        : null
                )
                .updatedAt(
                    ImmutableObjects.isLoaded(ledger, AccountLedgerProps.UPDATED_AT)
                        ? ledger.updatedAt()
                        : null
                )
                .build();
    }
}
