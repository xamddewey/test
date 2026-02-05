package com.xdw.demobackend.repository;

import com.xdw.demobackend.entity.AccountLedger;
import org.babyfish.jimmer.spring.repository.JRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountLedgerRepository extends JRepository<AccountLedger, Long> {
    
    Optional<AccountLedger> findByIdAndIsDeletedFalse(Long id);
    
    List<AccountLedger> findByCreatorIdAndIsDeletedFalse(Long creatorId);
    
    List<AccountLedger> findByIsDeletedFalse();
    
    boolean existsByIdAndIsDeletedFalse(Long id);
    
    long countByCreatorIdAndIsDeletedFalse(Long creatorId);
}
