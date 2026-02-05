package com.xdw.demobackend.entity;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

/**
 * Invitation entity interface
 * Corresponds to the invitations table in the database
 * Represents ledger join invitations with status tracking
 */
@Entity
@Table(name = "invitations")
public interface Invitation {
    /**
     * Primary key ID - auto-generated
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();

    /**
     * Invited to ledger
     */
    @ManyToOne
    AccountLedger ledger();

    @IdView("ledger")
    long ledgerId();

    /**
     * Invitation sender
     */
    @ManyToOne
    User sender();

    @IdView("sender")
    long senderId();

    /**
     * Invitation recipient
     */
    @ManyToOne
    User recipient();

    @IdView("recipient")
    long recipientId();

    /**
     * Invitation status enum
     */
    InvitationStatus status();

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
     * Redundant sender nickname (avoids JOIN)
     */
    @Nullable
    String senderNickname();

    /**
     * Redundant recipient nickname (avoids JOIN)
     */
    @Nullable
    String recipientNickname();

    /**
     * Invitation status enum definition
     * PENDING = invitation sent, awaiting response
     * ACCEPTED = invitation accepted
     * REJECTED = invitation rejected
     */
    enum InvitationStatus {
        PENDING,
        ACCEPTED,
        REJECTED
    }
}
