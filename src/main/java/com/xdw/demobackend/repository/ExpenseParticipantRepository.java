package com.xdw.demobackend.repository;

import com.xdw.demobackend.entity.ExpenseParticipant;
import org.babyfish.jimmer.spring.repository.JRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseParticipantRepository extends JRepository<ExpenseParticipant, Long> {
    
    Optional<ExpenseParticipant> findByIdAndIsDeletedFalse(Long id);
    
    List<ExpenseParticipant> findByRecordIdAndIsDeletedFalse(Long recordId);
    
    List<ExpenseParticipant> findByUserIdAndIsDeletedFalse(Long userId);
    
    List<ExpenseParticipant> findByLedgerIdAndUserIdAndIsDeletedFalse(Long ledgerId, Long userId);
    
    Optional<ExpenseParticipant> findByRecordIdAndUserIdAndIsDeletedFalse(Long recordId, Long userId);
    
    boolean existsByRecordIdAndUserIdAndIsDeletedFalse(Long recordId, Long userId);
    
    long countByRecordIdAndIsDeletedFalse(Long recordId);
    
    long countByUserIdAndIsDeletedFalse(Long userId);
}
