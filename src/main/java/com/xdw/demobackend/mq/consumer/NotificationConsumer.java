package com.xdw.demobackend.mq.consumer;

import com.xdw.demobackend.dto.message.NotificationMessage;
import com.xdw.demobackend.entity.Notification;
import com.xdw.demobackend.entity.NotificationDraft;
import com.xdw.demobackend.entity.User;
import com.xdw.demobackend.entity.UserDraft;
import com.xdw.demobackend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {
    private final NotificationRepository notificationRepository;

    @RabbitListener(queues = "notification-queue")
    @Transactional
    public void handleNotification(NotificationMessage message) {
        try {
            log.info("Received notification message: type={}, userId={}", message.getType(), message.getUserId());

            Notification notification = NotificationDraft.$.produce(draft -> {
                draft.setUser(UserDraft.$.produce(u -> u.setId(message.getUserId())));
                draft.setType(Notification.NotificationType.valueOf(message.getType()));
                draft.setTitle(message.getTitle());
                draft.setContent(message.getContent());
                draft.setIsRead(false);
                draft.setCreatedAt(LocalDateTime.now());
                draft.setIsDeleted(false);
            });

            Notification saved = notificationRepository.insert(notification);
            log.info("Notification persisted: id={}", saved.id());
        } catch (Exception e) {
            log.error("Failed to process notification message: {}", message, e);
        }
    }
}
