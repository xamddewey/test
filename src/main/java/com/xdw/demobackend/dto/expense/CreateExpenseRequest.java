package com.xdw.demobackend.dto.expense;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 创建记账条目请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExpenseRequest {
    
    @NotNull(message = "账本ID不能为空")
    private Long ledgerId;
    
    @NotNull(message = "类别ID不能为空")
    private Long categoryId;
    
    @NotNull(message = "付款人ID不能为空")
    private Long payerId;
    
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;
    
    @Size(max = 500, message = "描述不能超过500字符")
    private String description;
    
    @NotNull(message = "支出日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expenseDate;
    
    @NotEmpty(message = "参与者列表不能为空")
    @Valid
    private List<ParticipantRequest> participants;
}
