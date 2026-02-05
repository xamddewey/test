package com.xdw.demobackend.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Overall statistics response
 * Combines all statistics dimensions for dashboard view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverallStatisticsResponse {
    
    /**
     * Total expenses in the date range
     */
    private BigDecimal totalExpenses;
    
    /**
     * Total number of expense records
     */
    private Long totalExpenseCount;
    
    /**
     * Statistics by category
     */
    private List<CategoryStatisticsResponse> categoryStatistics;
    
    /**
     * Statistics by member
     */
    private List<MemberStatisticsResponse> memberStatistics;
    
    /**
     * Statistics by time range
     */
    private List<TimeRangeStatisticsResponse> timeRangeStatistics;
}
