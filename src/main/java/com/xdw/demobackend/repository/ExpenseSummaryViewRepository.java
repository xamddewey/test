package com.xdw.demobackend.repository;

import com.xdw.demobackend.entity.ExpenseSummaryView;
import org.babyfish.jimmer.spring.repository.JRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseSummaryViewRepository extends JRepository<ExpenseSummaryView, Long> {
    
    Optional<ExpenseSummaryView> findById(Long id);
    
    Optional<ExpenseSummaryView> findByRecordId(Long recordId);
    
    List<ExpenseSummaryView> findByLedgerId(Long ledgerId);
    
    List<ExpenseSummaryView> findByLedgerIdOrderByExpenseDateDesc(Long ledgerId);
    
    List<ExpenseSummaryView> findByPayerId(Long payerId);
    
    List<ExpenseSummaryView> findByLedgerIdAndExpenseDateBetween(Long ledgerId, LocalDate startDate, LocalDate endDate);
    
    long countByLedgerId(Long ledgerId);
}
