package com.xdw.demobackend.dto.settlement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 结算转账DTO
 */
public record TransferDTO(
        Long payerId,
        String payerNickname,
        Long receiverId,
        String receiverNickname,
        BigDecimal amount
) {
}
