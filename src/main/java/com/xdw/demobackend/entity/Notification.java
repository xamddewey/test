package com.xdw.demobackend.entity;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public interface Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();

    @ManyToOne
    User user();

    @IdView("user")
    long userId();

    NotificationType type();

    String title();

    @Nullable
    String content();

    @Nullable
    Boolean isRead();

    @Nullable
    LocalDateTime readAt();

    @Nullable
    LocalDateTime createdAt();

    @Nullable
    LocalDateTime updatedAt();

    @Nullable
    Boolean isDeleted();

    @Nullable
    LocalDateTime deletedAt();

    @Nullable
    String userNickname();

    enum NotificationType {
        INVITATION,
        SYSTEM,
        SETTLEMENT
    }
}
