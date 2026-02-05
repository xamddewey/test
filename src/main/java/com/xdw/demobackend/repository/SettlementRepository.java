package com.xdw.demobackend.repository;

import com.xdw.demobackend.entity.Settlement;
import org.babyfish.jimmer.spring.repository.JRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementRepository extends JRepository<Settlement, Long> {
    
    Optional<Settlement> findByIdAndIsDeletedFalse(Long id);
    
    List<Settlement> findByLedgerIdAndIsDeletedFalse(Long ledgerId);
    
    List<Settlement> findByLedgerIdAndStatusAndIsDeletedFalse(Long ledgerId, Settlement.SettlementStatus status);
    
    List<Settlement> findByPayerIdAndIsDeletedFalse(Long payerId);
    
    List<Settlement> findByReceiverIdAndIsDeletedFalse(Long receiverId);
    
    List<Settlement> findByPayerIdAndStatusAndIsDeletedFalse(Long payerId, Settlement.SettlementStatus status);
    
    List<Settlement> findByReceiverIdAndStatusAndIsDeletedFalse(Long receiverId, Settlement.SettlementStatus status);
    
    boolean existsByIdAndIsDeletedFalse(Long id);
    
    long countByLedgerIdAndStatusAndIsDeletedFalse(Long ledgerId, Settlement.SettlementStatus status);
}
