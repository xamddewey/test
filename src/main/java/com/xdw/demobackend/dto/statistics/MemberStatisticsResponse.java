package com.xdw.demobackend.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Member statistics response
 * Aggregates financial info by ledger member
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberStatisticsResponse {
    
    /**
     * Member user ID
     */
    private Long userId;
    
    /**
     * Member nickname
     */
    private String memberNickname;
    
    /**
     * Total amount paid by this member
     */
    private BigDecimal totalPaid;
    
    /**
     * Total amount shared by this member
     */
    private BigDecimal totalShared;
    
    /**
     * Current balance (totalPaid - totalShared)
     * Positive = others owe them
     * Negative = they owe others
     */
    private BigDecimal balance;
    
    /**
     * Number of expenses this member paid for
     */
    private Long expenseCount;
}
