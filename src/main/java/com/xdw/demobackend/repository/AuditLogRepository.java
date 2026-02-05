package com.xdw.demobackend.repository;

import com.xdw.demobackend.entity.AuditLog;
import org.babyfish.jimmer.spring.repository.JRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AuditLog repository interface
 * 
 * NOTE: No soft-delete methods (AndIsDeletedFalse suffix) - audit logs are immutable.
 */
@Repository
public interface AuditLogRepository extends JRepository<AuditLog, Long> {

    /**
     * Find all audit logs for a specific entity
     */
    List<AuditLog> findByTypeAndEntityId(AuditLog.EntityType type, Long entityId);

    /**
     * Find all operations performed by a specific user
     */
    List<AuditLog> findByActorId(Long actorId);

    /**
     * Find all audit logs related to a specific ledger
     */
    List<AuditLog> findByLedgerId(Long ledgerId);

    /**
     * Find all audit logs of a specific entity type
     */
    List<AuditLog> findByType(AuditLog.EntityType type);

    /**
     * Find all audit logs of a specific action type
     */
    List<AuditLog> findByAction(AuditLog.ActionType action);

    /**
     * Find all audit logs for a specific ledger and entity type
     */
    List<AuditLog> findByLedgerIdAndType(Long ledgerId, AuditLog.EntityType type);

    /**
     * Find all operations by a specific user on a specific entity type
     */
    List<AuditLog> findByActorIdAndType(Long actorId, AuditLog.EntityType type);
}
