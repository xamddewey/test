package com.xdw.demobackend.entity;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ExpenseCategory entity interface
 * Corresponds to the expense_categories table in the database
 * Represents expense categories (system and user-defined)
 */
@Entity
@Table(name = "expense_categories")
public interface ExpenseCategory {
    /**
     * Primary key ID - auto-generated
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();

    /**
     * Category name, unique
     */
    String categoryName();

    /**
     * Category description, optional
     */
    @Nullable
    String description();

    /**
     * Is default category (added to new ledgers)
     */
    @Nullable
    Boolean isDefault();

    /**
     * Is system category (cannot be deleted)
     */
    @Nullable
    Boolean isSystem();

    /**
     * Creator user ID, nullable (NULL for system categories)
     */
    @Nullable
    @ManyToOne
    User creator();

    @Nullable
    @IdView("creator")
    Long creatorId();

    /**
     * Default display order
     */
    Integer displayOrder();

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
     * Usage count (redundant statistical field)
     */
    @Nullable
    Integer usageCount();

    /**
     * Ledger categories (inverse side)
     */
    @OneToMany(mappedBy = "category")
    List<LedgerCategory> ledgerCategories();
}
