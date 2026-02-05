package com.xdw.demobackend.dto.ledger;

import com.xdw.demobackend.entity.AccountLedger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
                .description(ledger.description())
                .creatorId(ledger.creatorId())
                .creatorNickname(ledger.creatorNickname())
                .memberCount(ledger.memberCount())
                .invitedCount(ledger.invitedCount())
                .totalExpenses(ledger.totalExpenses())
                .recordCount(ledger.recordCount())
                .lastExpenseDate(ledger.lastExpenseDate())
                .lastActivityAt(ledger.lastActivityAt())
                .createdAt(ledger.createdAt())
                .updatedAt(ledger.updatedAt())
                .build();
    }
}
