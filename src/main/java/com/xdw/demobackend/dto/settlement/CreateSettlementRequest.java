package com.xdw.demobackend.dto.settlement;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 创建结算请求DTO
 */
public record CreateSettlementRequest(
        @NotNull(message = "账本ID不能为空")
        Long ledgerId,

        @NotNull(message = "付款人ID不能为空")
        Long payerId,

        @NotNull(message = "收款人ID不能为空")
        Long receiverId,

        @NotNull(message = "金额不能为空")
        @DecimalMin(value = "0.01", message = "金额必须大于0")
        BigDecimal amount,

        @Size(max = 500, message = "描述不能超过500字符")
        String description
) {
}
