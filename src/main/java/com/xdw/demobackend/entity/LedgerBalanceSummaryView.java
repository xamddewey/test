package com.xdw.demobackend.entity;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * LedgerBalanceSummaryView entity interface
 * Corresponds to the ledger_balance_summary_view table in the database
 * Read-only summary view aggregating member balance data
 * Note: This is a regular entity, not a database view
 */
@Entity
@Table(name = "ledger_balance_summary_view")
public interface LedgerBalanceSummaryView {
    /**
     * Primary key ID - auto-generated
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();

    /**
     * Ledger ID
     */
    long ledgerId();

    /**
     * User ID
     */
    long userId();

    /**
     * Ledger name
     */
    String ledgerName();

    /**
     * User nickname
     */
    String userNickname();

    /**
     * Total paid amount
     */
    @Nullable
    BigDecimal totalPaid();

    /**
     * Total shared amount
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
     * Last activity timestamp
     */
    @Nullable
    LocalDateTime lastActivity();

    /**
     * Last update timestamp
     */
    @Nullable
    LocalDateTime updatedAt();
}
