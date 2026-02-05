package com.xdw.demobackend.entity;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ExpenseRecord entity interface
 * Corresponds to the expense_records table in the database
 * Represents individual expense records with extensive redundant fields
 */
@Entity
@Table(name = "expense_records")
public interface ExpenseRecord {
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
     * Payer user
     */
    @ManyToOne
    User payer();

    @IdView("payer")
    long payerId();

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
     * Expense date (when it occurred)
     */
    LocalDate expenseDate();

    /**
     * Created by user
     */
    @ManyToOne
    User createdBy();

    @IdView("createdBy")
    long createdById();

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
     * Redundant category name (avoids JOIN)
     */
    @Nullable
    String categoryName();

    /**
     * Redundant payer nickname (avoids JOIN)
     */
    @Nullable
    String payerNickname();

    /**
     * Redundant creator nickname (avoids JOIN)
     */
    @Nullable
    String creatorNickname();

    /**
     * Participant count (redundant)
     */
    @Nullable
    Integer participantCount();

    /**
     * Average amount per participant (redundant)
     */
    @Nullable
    BigDecimal avgAmount();

    /**
     * Participants info in JSON format (redundant)
     */
    @Nullable
    String participantsInfo();

    /**
     * Has settlement flag (redundant status)
     */
    @Nullable
    Boolean hasSettlement();

    /**
     * Expense participants (inverse side)
     */
    @OneToMany(mappedBy = "record")
    List<ExpenseParticipant> participants();
}
