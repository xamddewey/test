package com.xdw.demobackend.repository;

import com.xdw.demobackend.entity.LedgerMember;
import org.babyfish.jimmer.spring.repository.JRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LedgerMemberRepository extends JRepository<LedgerMember, Long> {
    
    Optional<LedgerMember> findByIdAndIsDeletedFalse(Long id);
    
    List<LedgerMember> findByLedgerIdAndIsDeletedFalse(Long ledgerId);
    
    List<LedgerMember> findByUserIdAndIsDeletedFalse(Long userId);
    
    List<LedgerMember> findByLedgerIdAndJoinStatusAndIsDeletedFalse(Long ledgerId, LedgerMember.JoinStatus joinStatus);
    
    List<LedgerMember> findByUserIdAndJoinStatusAndIsDeletedFalse(Long userId, LedgerMember.JoinStatus joinStatus);
    
    Optional<LedgerMember> findByLedgerIdAndUserIdAndIsDeletedFalse(Long ledgerId, Long userId);
    
    boolean existsByLedgerIdAndUserIdAndIsDeletedFalse(Long ledgerId, Long userId);
    
    long countByLedgerIdAndJoinStatusAndIsDeletedFalse(Long ledgerId, LedgerMember.JoinStatus joinStatus);
    
    long countByUserIdAndJoinStatusAndIsDeletedFalse(Long userId, LedgerMember.JoinStatus joinStatus);
}
