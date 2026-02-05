package com.xdw.demobackend.entity;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * LedgerMember entity interface
 * Corresponds to the ledger_members table in the database
 * Represents user memberships in ledgers with balance tracking
 */
@Entity
@Table(name = "ledger_members")
public interface LedgerMember {
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
     * Associated user
     */
    @ManyToOne
    User user();

    @IdView("user")
    long userId();

    /**
     * Join status enum
     */
    JoinStatus joinStatus();

    /**
     * Invited timestamp
     */
    @Nullable
    LocalDateTime invitedAt();

    /**
     * Joined timestamp
     */
    @Nullable
    LocalDateTime joinedAt();

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
     * Redundant user nickname (avoids JOIN)
     */
    @Nullable
    String userNickname();

    /**
     * Redundant ledger name (avoids JOIN)
     */
    @Nullable
    String ledgerName();

    /**
     * Total paid by this member in this ledger
     */
    @Nullable
    BigDecimal totalPaid();

    /**
     * Total shared by this member in this ledger
     */
    @Nullable
    BigDecimal totalShared();

    /**
     * Balance (positive = others owe them, negative = they owe others)
     */
    @Nullable
    BigDecimal balance();

    /**
     * Record count (expenses participated)
     */
    @Nullable
    Integer recordCount();

    /**
     * Last activity timestamp in this ledger
     */
    @Nullable
    LocalDateTime lastActivityAt();

    /**
     * Join status enum definition
     * INVITED = invited but not joined yet
     * JOINED = joined the ledger
     */
    enum JoinStatus {
        INVITED,
        JOINED
    }
}
