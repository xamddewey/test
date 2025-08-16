package com.xdw.demobackend.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 排序信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SortInfo {
    
    /**
     * 排序字段列表
     */
    private List<SortField> fields;
    
    /**
     * 是否已排序
     */
    private Boolean sorted;
    
    /**
     * 是否为空
     */
    private Boolean empty;
    
    /**
     * 排序字段信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SortField {
        /**
         * 字段名
         */
        private String field;
        
        /**
         * 排序方向
         */
        private SortDirection direction;
        
        /**
         * 是否忽略大小写
         */
        private Boolean ignoreCase;
        
        /**
         * 排序优先级
         */
        private Integer priority;
    }
    
    /**
     * 排序方向枚举
     */
    public enum SortDirection {
        ASC, DESC
    }
    
    // ============ 静态工厂方法 ============
    
    /**
     * 创建排序信息
     */
    public static SortInfo of(List<SortField> fields) {
        return SortInfo.builder()
                .fields(fields)
                .sorted(fields != null && !fields.isEmpty())
                .empty(fields == null || fields.isEmpty())
                .build();
    }
    
    /**
     * 创建单字段排序
     */
    public static SortInfo of(String field, SortDirection direction) {
        SortField sortField = SortField.builder()
                .field(field)
                .direction(direction)
                .priority(1)
                .build();
        
        return of(List.of(sortField));
    }
    
    /**
     * 创建空排序
     */
    public static SortInfo empty() {
        return SortInfo.builder()
                .fields(List.of())
                .sorted(false)
                .empty(true)
                .build();
    }
}