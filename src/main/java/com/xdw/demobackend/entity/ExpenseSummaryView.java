package com.xdw.demobackend.entity;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ExpenseSummaryView entity interface
 * Corresponds to the expense_summary_view table in the database
 * Read-only summary view aggregating expense record data
 * Note: This is a regular entity, not a database view
 */
@Entity
@Table(name = "expense_summary_view")
public interface ExpenseSummaryView {
    /**
     * Primary key ID - auto-generated
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();

    /**
     * Associated expense record ID (unique)
     */
    long recordId();

    /**
     * Ledger ID
     */
    long ledgerId();

    /**
     * Ledger name
     */
    String ledgerName();

    /**
     * Category name
     */
    String categoryName();

    /**
     * Payer ID
     */
    long payerId();

    /**
     * Payer nickname
     */
    String payerNickname();

    /**
     * Creator nickname
     */
    String creatorNickname();

    /**
     * Expense amount
     */
    BigDecimal amount();

    /**
     * Expense description, optional
     */
    @Nullable
    String description();

    /**
     * Expense date
     */
    LocalDate expenseDate();

    /**
     * Participant count
     */
    int participantCount();

    /**
     * Average amount per participant
     */
    BigDecimal avgAmount();

    /**
     * Participants info (JSON format)
     */
    String participants();

    /**
     * Creation timestamp
     */
    LocalDateTime createdAt();

    /**
     * Last update timestamp
     */
    @Nullable
    LocalDateTime updatedAt();
}
