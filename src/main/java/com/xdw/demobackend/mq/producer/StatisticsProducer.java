package com.xdw.demobackend.mq.producer;

import com.xdw.demobackend.dto.message.StatisticsMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.xdw.demobackend.config.RabbitMQConfig.STATISTICS_EXCHANGE;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatisticsProducer {
    private final RabbitTemplate rabbitTemplate;

    public void triggerStatisticsUpdate(StatisticsMessage message) {
        String routingKey = "statistics." + message.getEventType();
        rabbitTemplate.convertAndSend(STATISTICS_EXCHANGE, routingKey, message);
        log.info("Sent statistics trigger: ledgerId={}, eventType={}", 
                message.getLedgerId(), message.getEventType());
    }
}
