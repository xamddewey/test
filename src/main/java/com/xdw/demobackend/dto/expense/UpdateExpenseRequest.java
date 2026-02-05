package com.xdw.demobackend.dto.expense;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
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
 * 更新记账条目请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExpenseRequest {
    
    private Long categoryId;
    
    private Long payerId;
    
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;
    
    @Size(max = 500, message = "描述不能超过500字符")
    private String description;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expenseDate;
    
    @Valid
    private List<ParticipantRequest> participants;
}
