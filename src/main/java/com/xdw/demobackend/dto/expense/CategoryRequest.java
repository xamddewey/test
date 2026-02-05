package com.xdw.demobackend.dto.expense;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建类别请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    
    @NotBlank(message = "类别名称不能为空")
    @Size(max = 50, message = "类别名称不能超过50字符")
    private String categoryName;
    
    @Size(max = 200, message = "类别描述不能超过200字符")
    private String description;
}
