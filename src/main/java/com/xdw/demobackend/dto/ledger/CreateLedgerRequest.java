package com.xdw.demobackend.dto.ledger;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建账本请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLedgerRequest {
    
    @NotBlank(message = "账本名称不能为空")
    @Size(max = 100, message = "账本名称不能超过100字符")
    private String ledgerName;
    
    @Size(max = 500, message = "账本描述不能超过500字符")
    private String description;
}
