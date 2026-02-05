package com.xdw.demobackend.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Statistics request DTO
 * Supports filtering by date range and time granularity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsRequest {
    
    /**
     * Ledger ID (required)
     */
    private Long ledgerId;
    
    /**
     * Start date (optional, null = from beginning)
     */
    private LocalDate startDate;
    
    /**
     * End date (optional, null = until now)
     */
    private LocalDate endDate;
    
    /**
     * Time granularity for time-range statistics
     */
    private TimeGranularity granularity;
    
    /**
     * Time granularity enum
     */
    public enum TimeGranularity {
        WEEK,   // Group by ISO week
        MONTH,  // Group by year-month
        CUSTOM  // Single bucket for entire date range
    }
}
