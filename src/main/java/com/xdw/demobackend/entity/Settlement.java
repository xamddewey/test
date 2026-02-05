package com.xdw.demobackend.entity;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Settlement entity interface
 * Corresponds to the settlements table in the database
 * Represents payment settlements between users
 */
@Entity
@Table(name = "settlements")
public interface Settlement {
    /**
     * Primary key ID - auto-generated
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();

    /**
     * Associated ledger
     */
    @ManyToOne
    AccountLedger ledger();

    @IdView("ledger")
    long ledgerId();

    /**
     * Payer user
     */
    @ManyToOne
    User payer();

    @IdView("payer")
    long payerId();

    /**
     * Receiver user
     */
    @ManyToOne
    User receiver();

    @IdView("receiver")
    long receiverId();

    /**
     * Settlement amount
     */
    BigDecimal amount();

    /**
     * Settlement status enum
     */
    SettlementStatus status();

    /**
     * Creation timestamp
     */
    @Nullable
    LocalDateTime createdAt();

    /**
     * Last update timestamp
     */
    @Nullable
    LocalDateTime updatedAt();

    /**
     * Soft delete flag
     */
    @Nullable
    Boolean isDeleted();

    /**
     * Soft delete timestamp
     */
    @Nullable
    LocalDateTime deletedAt();

    /**
     * Redundant ledger name (avoids JOIN)
     */
    @Nullable
    String ledgerName();

    /**
     * Redundant payer nickname (avoids JOIN)
     */
    @Nullable
    String payerNickname();

    /**
     * Redundant receiver nickname (avoids JOIN)
     */
    @Nullable
    String receiverNickname();

    /**
     * Settlement description
     */
    @Nullable
    String description();

    /**
     * Settlement status enum definition
     * PENDING = settlement created, awaiting confirmation
     * COMPLETED = settlement completed
     */
    enum SettlementStatus {
        PENDING,
        COMPLETED
    }
}
