package com.xdw.demobackend.repository;

import com.xdw.demobackend.entity.ExpenseRecord;
import org.babyfish.jimmer.spring.repository.JRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRecordRepository extends JRepository<ExpenseRecord, Long> {
    
    Optional<ExpenseRecord> findByIdAndIsDeletedFalse(Long id);
    
    List<ExpenseRecord> findByLedgerIdAndIsDeletedFalse(Long ledgerId);
    
    List<ExpenseRecord> findByLedgerIdAndIsDeletedFalseOrderByExpenseDateDesc(Long ledgerId);
    
    List<ExpenseRecord> findByLedgerIdAndCategoryIdAndIsDeletedFalse(Long ledgerId, Long categoryId);
    
    List<ExpenseRecord> findByPayerIdAndIsDeletedFalse(Long payerId);
    
    List<ExpenseRecord> findByCreatedByAndIsDeletedFalse(Long createdBy);
    
    List<ExpenseRecord> findByLedgerIdAndExpenseDateBetweenAndIsDeletedFalse(Long ledgerId, LocalDate startDate, LocalDate endDate);
    
    boolean existsByIdAndIsDeletedFalse(Long id);
    
    long countByLedgerIdAndIsDeletedFalse(Long ledgerId);
    
    long countByPayerIdAndIsDeletedFalse(Long payerId);
}
