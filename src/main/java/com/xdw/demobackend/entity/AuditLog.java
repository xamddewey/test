package com.xdw.demobackend.entity;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

/**
 * AuditLog entity interface
 * Corresponds to the audit_logs table in the database
 * 
 * Immutable audit trail - no updates, no soft-deletes, only inserts.
 * Records all operations on ledgers, members, entries, settlements, and categories.
 */
@Entity
@Table(name = "audit_logs")
public interface AuditLog {
    /**
     * Primary key ID - auto-generated
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();

    /**
     * Entity type being audited
     * Maps to entity_type column (renamed to avoid Jimmer reserved keyword)
     */
    @Column(name = "entity_type")
    EntityType type();

    /**
     * ID of the entity being audited
     */
    long entityId();

    /**
     * Type of action performed
     */
    ActionType action();

    /**
     * User who performed the action
     */
    @ManyToOne
    User actor();

    /**
     * Actor user ID (denormalized for query performance)
     */
    @IdView("actor")
    long actorId();

    /**
     * Optional ledger context for the operation
     */
    @Nullable
    @ManyToOne
    AccountLedger ledger();

    /**
     * Optional ledger ID (denormalized)
     */
    @Nullable
    @IdView("ledger")
    Long ledgerId();

    /**
     * JSON or text description of the changes
     */
    @Nullable
    String changes();

    /**
     * Creation timestamp - immutable logs only have creation time
     */
    @Nullable
    LocalDateTime createdAt();

    /**
     * Entity types that can be audited
     */
    enum EntityType {
        LEDGER,
        MEMBER,
        ENTRY,
        SETTLEMENT,
        CATEGORY
    }

    /**
     * Types of actions that can be audited
     */
    enum ActionType {
        CREATE,
        UPDATE,
        DELETE
    }
}
