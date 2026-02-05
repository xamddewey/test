package com.xdw.demobackend.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Time range statistics response
 * Aggregates expenses by time period (week/month/custom)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeRangeStatisticsResponse {
    
    /**
     * Time period label
     * Format: "2026-W06" for week, "2026-02" for month, "Custom" for custom range
     */
    private String periodLabel;
    
    /**
     * Total expenses in this period
     */
    private BigDecimal totalExpenses;
    
    /**
     * Number of expense records in this period
     */
    private Long expenseCount;
}
