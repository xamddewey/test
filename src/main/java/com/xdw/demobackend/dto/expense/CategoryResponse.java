package com.xdw.demobackend.dto.expense;

import com.xdw.demobackend.entity.ExpenseCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 类别响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    
    private Long id;
    private String categoryName;
    private String description;
    private Boolean isDefault;
    private Boolean isSystem;
    private Long creatorId;
    private Integer displayOrder;
    private Integer usageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static CategoryResponse fromEntity(ExpenseCategory category) {
        return CategoryResponse.builder()
                .id(category.id())
                .categoryName(category.categoryName())
                .description(category.description())
                .isDefault(category.isDefault())
                .isSystem(category.isSystem())
                .creatorId(category.creatorId())
                .displayOrder(category.displayOrder())
                .usageCount(category.usageCount())
                .createdAt(category.createdAt())
                .updatedAt(category.updatedAt())
                .build();
    }
}
