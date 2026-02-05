package com.xdw.demobackend.service.ledger;

import com.xdw.demobackend.dto.ledger.CreateLedgerRequest;
import com.xdw.demobackend.dto.ledger.LedgerResponse;
import com.xdw.demobackend.dto.ledger.MemberResponse;
import com.xdw.demobackend.dto.ledger.UpdateLedgerRequest;

import java.util.List;

public interface LedgerService {
    
    LedgerResponse createLedger(CreateLedgerRequest request, Long userId);
    
    LedgerResponse getLedger(Long ledgerId, Long userId);
    
    List<LedgerResponse> getUserLedgers(Long userId);
    
    LedgerResponse updateLedger(Long ledgerId, UpdateLedgerRequest request, Long userId);
    
    void deleteLedger(Long ledgerId, Long userId);
    
    List<MemberResponse> getLedgerMembers(Long ledgerId, Long userId);
}
