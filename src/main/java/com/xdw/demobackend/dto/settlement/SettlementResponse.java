package com.xdw.demobackend.dto.settlement;

import com.xdw.demobackend.entity.Settlement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 结算响应DTO
 */
public record SettlementResponse(
        Long id,
        Long ledgerId,
        String ledgerName,
        Long payerId,
        String payerNickname,
        Long receiverId,
        String receiverNickname,
        BigDecimal amount,
        String status,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static SettlementResponse fromEntity(Settlement settlement) {
        return new SettlementResponse(
                settlement.id(),
                settlement.ledgerId(),
                settlement.ledgerName(),
                settlement.payerId(),
                settlement.payerNickname(),
                settlement.receiverId(),
                settlement.receiverNickname(),
                settlement.amount(),
                settlement.status().name(),
                settlement.description(),
                settlement.createdAt(),
                settlement.updatedAt()
        );
    }
}
