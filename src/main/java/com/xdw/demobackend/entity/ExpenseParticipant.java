package com.xdw.demobackend.entity;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ExpenseParticipant entity interface
 * Corresponds to the expense_participants table in the database
 * Represents participants in each expense record with redundant fields
 */
@Entity
@Table(name = "expense_participants")
public interface ExpenseParticipant {
    /**
     * Primary key ID - auto-generated
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();

    /**
     * Associated expense record
     */
    @ManyToOne
    ExpenseRecord record();

    @IdView("record")
    long recordId();

    /**
     * Participant user
     */
    @ManyToOne
    User user();

    @IdView("user")
    long userId();

    /**
     * Shared amount for this participant
     */
    BigDecimal amount();

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
     * Redundant total expense amount (avoids JOIN)
     */
    @Nullable
    BigDecimal expenseAmount();

    /**
     * Redundant expense date (avoids JOIN)
     */
    @Nullable
    LocalDate expenseDate();

    /**
     * Redundant ledger ID (avoids JOIN)
     */
    @Nullable
    Long ledgerId();

    /**
     * Redundant category name (avoids JOIN)
     */
    @Nullable
    String categoryName();
}
