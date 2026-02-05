package com.xdw.demobackend.mq.consumer;

import com.xdw.demobackend.dto.message.AuditMessage;
import com.xdw.demobackend.entity.*;
import com.xdw.demobackend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditConsumer {
    private final AuditLogRepository auditLogRepository;

    @RabbitListener(queues = "audit-queue")
    @Transactional
    public void handleAudit(AuditMessage message) {
        try {
            log.info("Received audit message: entityType={}, action={}, entityId={}", 
                    message.getEntityType(), message.getAction(), message.getEntityId());

            AuditLog auditLog = AuditLogDraft.$.produce(draft -> {
                draft.setType(AuditLog.EntityType.valueOf(message.getEntityType()));
                draft.setEntityId(message.getEntityId());
                draft.setAction(AuditLog.ActionType.valueOf(message.getAction()));
                draft.setActor(UserDraft.$.produce(u -> u.setId(message.getActorId())));
                draft.setChanges(message.getChanges());
                if (message.getLedgerId() != null) {
                    draft.setLedger(AccountLedgerDraft.$.produce(l -> l.setId(message.getLedgerId())));
                }
                draft.setCreatedAt(LocalDateTime.now());
            });

            AuditLog saved = auditLogRepository.insert(auditLog);
            log.info("Audit log persisted: id={}", saved.id());
        } catch (Exception e) {
            log.error("Failed to process audit message: {}", message, e);
        }
    }
}
