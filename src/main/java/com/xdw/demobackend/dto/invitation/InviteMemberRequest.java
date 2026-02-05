package com.xdw.demobackend.dto.invitation;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邀请成员请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteMemberRequest {
    
    @NotNull(message = "账本ID不能为空")
    private Long ledgerId;
    
    @NotNull(message = "被邀请用户ID不能为空")
    private Long recipientId;
}
