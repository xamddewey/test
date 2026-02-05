package com.xdw.demobackend.dto.ledger;

import com.xdw.demobackend.entity.LedgerMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账本成员响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    
    private Long id;
    private Long ledgerId;
    private String ledgerName;
    private Long userId;
    private String userNickname;
    private String joinStatus;
    private BigDecimal totalPaid;
    private BigDecimal totalShared;
    private BigDecimal balance;
    private Integer recordCount;
    private LocalDateTime joinedAt;
    private LocalDateTime lastActivityAt;
    
    /**
     * 从LedgerMember实体转换为DTO
     */
    public static MemberResponse fromEntity(LedgerMember member) {
        return MemberResponse.builder()
                .id(member.id())
                .ledgerId(member.ledgerId())
                .ledgerName(member.ledgerName())
                .userId(member.userId())
                .userNickname(member.userNickname())
                .joinStatus(member.joinStatus().name())
                .totalPaid(member.totalPaid())
                .totalShared(member.totalShared())
                .balance(member.balance())
                .recordCount(member.recordCount())
                .joinedAt(member.joinedAt())
                .lastActivityAt(member.lastActivityAt())
                .build();
    }
}
