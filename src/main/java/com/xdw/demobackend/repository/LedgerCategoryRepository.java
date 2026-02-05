package com.xdw.demobackend.repository;

import com.xdw.demobackend.entity.LedgerCategory;
import org.babyfish.jimmer.spring.repository.JRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LedgerCategoryRepository extends JRepository<LedgerCategory, Long> {
    
    Optional<LedgerCategory> findByIdAndIsDeletedFalse(Long id);
    
    List<LedgerCategory> findByLedgerIdAndIsDeletedFalse(Long ledgerId);
    
    List<LedgerCategory> findByLedgerIdAndIsDeletedFalseOrderByDisplayOrderAsc(Long ledgerId);
    
    Optional<LedgerCategory> findByLedgerIdAndCategoryIdAndIsDeletedFalse(Long ledgerId, Long categoryId);
    
    boolean existsByLedgerIdAndCategoryIdAndIsDeletedFalse(Long ledgerId, Long categoryId);
    
    long countByLedgerIdAndIsDeletedFalse(Long ledgerId);
}
