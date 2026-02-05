package com.xdw.demobackend.dto.invitation;

import com.xdw.demobackend.entity.Invitation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 邀请响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponse {
    
    private Long id;
    private Long ledgerId;
    private String ledgerName;
    private Long senderId;
    private String senderNickname;
    private Long recipientId;
    private String recipientNickname;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 从Invitation实体转换为DTO
     */
    public static InvitationResponse fromEntity(Invitation invitation) {
        return InvitationResponse.builder()
                .id(invitation.id())
                .ledgerId(invitation.ledgerId())
                .ledgerName(invitation.ledgerName())
                .senderId(invitation.senderId())
                .senderNickname(invitation.senderNickname())
                .recipientId(invitation.recipientId())
                .recipientNickname(invitation.recipientNickname())
                .status(invitation.status().name())
                .createdAt(invitation.createdAt())
                .updatedAt(invitation.updatedAt())
                .build();
    }
}
