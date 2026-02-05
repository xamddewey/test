package com.xdw.demobackend.entity;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AccountLedger entity interface
 * Corresponds to the account_ledgers table in the database
 * Represents shared expense ledgers with statistical redundant fields
 */
@Entity
@Table(name = "account_ledgers")
public interface AccountLedger {
    /**
     * Primary key ID - auto-generated
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();

    /**
     * Ledger name
     */
    String ledgerName();

    /**
     * Ledger description, optional
     */
    @Nullable
    String description();

    /**
     * Creator user ID
     */
    @ManyToOne
    User creator();

    @IdView("creator")
    long creatorId();

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
     * Redundant creator nickname (avoids JOIN)
     */
    @Nullable
    String creatorNickname();

    /**
     * Member count (JOINED status)
     */
    @Nullable
    Integer memberCount();

    /**
     * Invited member count (INVITED status)
     */
    @Nullable
    Integer invitedCount();

    /**
     * Total expenses amount
     */
    @Nullable
    BigDecimal totalExpenses();

    /**
     * Expense record count
     */
    @Nullable
    Integer recordCount();

    /**
     * Last expense date
     */
    @Nullable
    LocalDate lastExpenseDate();

    /**
     * Last activity timestamp
     */
    @Nullable
    LocalDateTime lastActivityAt();

    /**
     * Ledger members (inverse side)
     */
    @OneToMany(mappedBy = "ledger")
    List<LedgerMember> members();

    /**
     * Expense records (inverse side)
     */
    @OneToMany(mappedBy = "ledger")
    List<ExpenseRecord> expenseRecords();
}
