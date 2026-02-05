package com.xdw.demobackend.mq.producer;

import com.xdw.demobackend.dto.message.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.xdw.demobackend.config.RabbitMQConfig.NOTIFICATIONS_EXCHANGE;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendNotification(NotificationMessage message) {
        String routingKey = "notification." + message.getType();
        rabbitTemplate.convertAndSend(NOTIFICATIONS_EXCHANGE, routingKey, message);
        log.info("Sent notification message: type={}, userId={}", message.getType(), message.getUserId());
    }
}
