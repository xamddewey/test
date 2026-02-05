package com.xdw.demobackend.mq.producer;

import com.xdw.demobackend.dto.message.AuditMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.xdw.demobackend.config.RabbitMQConfig.AUDIT_EXCHANGE;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendAuditEvent(AuditMessage message) {
        String routingKey = "audit." + message.getEntityType() + "." + message.getAction();
        rabbitTemplate.convertAndSend(AUDIT_EXCHANGE, routingKey, message);
        log.info("Sent audit message: entityType={}, action={}, entityId={}", 
                message.getEntityType(), message.getAction(), message.getEntityId());
    }
}
