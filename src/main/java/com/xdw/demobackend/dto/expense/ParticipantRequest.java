package com.xdw.demobackend.dto.expense;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 参与者请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantRequest {
    
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotNull(message = "分摊金额不能为空")
    @DecimalMin(value = "0.01", message = "分摊金额必须大于0")
    private BigDecimal amount;
}
