package com.xdw.demobackend.entity;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

/**
 * LedgerCategory entity interface
 * Corresponds to the ledger_categories table in the database
 * Represents category associations for each ledger
 */
@Entity
@Table(name = "ledger_categories")
public interface LedgerCategory {
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
     * Associated category
     */
    @ManyToOne
    ExpenseCategory category();

    @IdView("category")
    long categoryId();

    /**
     * Display order in this ledger
     */
    int displayOrder();

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
     * Redundant category name (avoids JOIN)
     */
    @Nullable
    String categoryName();

    /**
     * Usage count in this ledger
     */
    @Nullable
    Integer usageCount();
}
