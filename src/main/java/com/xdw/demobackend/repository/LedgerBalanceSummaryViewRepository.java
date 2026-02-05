package com.xdw.demobackend.repository;

import com.xdw.demobackend.entity.LedgerBalanceSummaryView;
import org.babyfish.jimmer.spring.repository.JRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LedgerBalanceSummaryViewRepository extends JRepository<LedgerBalanceSummaryView, Long> {
    
    Optional<LedgerBalanceSummaryView> findById(Long id);
    
    Optional<LedgerBalanceSummaryView> findByLedgerIdAndUserId(Long ledgerId, Long userId);
    
    List<LedgerBalanceSummaryView> findByLedgerId(Long ledgerId);
    
    List<LedgerBalanceSummaryView> findByLedgerIdOrderByBalanceDesc(Long ledgerId);
    
    List<LedgerBalanceSummaryView> findByUserId(Long userId);
    
    long countByLedgerId(Long ledgerId);
}
