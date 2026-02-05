package com.xdw.demobackend.dto.settlement;

import java.math.BigDecimal;
import java.util.List;

/**
 * 结算计算响应DTO
 */
public record SettlementCalculationResponse(
        Long ledgerId,
        String ledgerName,
        List<TransferDTO> transfers,
        Integer transferCount,
        BigDecimal totalAmount
) {
}
