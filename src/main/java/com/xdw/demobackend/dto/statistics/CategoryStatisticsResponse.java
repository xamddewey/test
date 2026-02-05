package com.xdw.demobackend.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Category statistics response
 * Aggregates expenses by category
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStatisticsResponse {
    
    /**
     * Category name
     */
    private String categoryName;
    
    /**
     * Total amount spent in this category
     */
    private BigDecimal totalAmount;
    
    /**
     * Number of expenses in this category
     */
    private Long expenseCount;
}
