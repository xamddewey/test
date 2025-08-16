package com.xdw.demobackend.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 筛选信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterInfo {
    
    /**
     * 筛选条件
     */
    private Map<String, Object> filters;
    
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 是否有筛选条件
     */
    private Boolean hasFilters;
    
    /**
     * 筛选器数量
     */
    private Integer filterCount;
    
    // ============ 静态工厂方法 ============
    
    /**
     * 创建筛选信息
     */
    public static FilterInfo of(Map<String, Object> filters) {
        return FilterInfo.builder()
                .filters(filters)
                .hasFilters(filters != null && !filters.isEmpty())
                .filterCount(filters != null ? filters.size() : 0)
                .build();
    }
    
    /**
     * 创建带关键词的筛选信息
     */
    public static FilterInfo of(Map<String, Object> filters, String keyword) {
        FilterInfo filterInfo = of(filters);
        filterInfo.setKeyword(keyword);
        return filterInfo;
    }
    
    /**
     * 创建空筛选
     */
    public static FilterInfo empty() {
        return FilterInfo.builder()
                .filters(Map.of())
                .hasFilters(false)
                .filterCount(0)
                .build();
    }
}