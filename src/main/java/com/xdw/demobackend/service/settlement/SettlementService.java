package com.xdw.demobackend.service.settlement;

import com.xdw.demobackend.dto.settlement.CreateSettlementRequest;
import com.xdw.demobackend.dto.settlement.SettlementCalculationResponse;
import com.xdw.demobackend.dto.settlement.SettlementResponse;

import java.util.List;

public interface SettlementService {

    SettlementCalculationResponse calculateSettlement(Long ledgerId, Long userId);

    SettlementResponse createSettlement(CreateSettlementRequest request, Long userId);

    SettlementResponse completeSettlement(Long settlementId, Long userId);

    List<SettlementResponse> getLedgerSettlements(Long ledgerId, String status, Long userId);

    List<SettlementResponse> getMySettlements(Long userId, String status);
}
