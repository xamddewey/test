package com.xdw.demobackend.repository;

import com.xdw.demobackend.entity.Notification;
import org.babyfish.jimmer.spring.repository.JRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JRepository<Notification, Long> {
    
    Optional<Notification> findByIdAndIsDeletedFalse(Long id);
    
    List<Notification> findByUserIdAndIsDeletedFalse(Long userId);
    
    List<Notification> findByUserIdAndIsReadFalseAndIsDeletedFalse(Long userId);
    
    List<Notification> findByTypeAndIsDeletedFalse(Notification.NotificationType type);
    
    List<Notification> findByUserIdAndTypeAndIsDeletedFalse(Long userId, Notification.NotificationType type);
    
    long countByUserIdAndIsReadFalseAndIsDeletedFalse(Long userId);
    
    boolean existsByIdAndIsDeletedFalse(Long id);
}
