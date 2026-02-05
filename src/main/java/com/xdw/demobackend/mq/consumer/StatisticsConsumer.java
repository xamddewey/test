package com.xdw.demobackend.mq.consumer;

import com.xdw.demobackend.dto.message.StatisticsMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatisticsConsumer {

    @RabbitListener(queues = "statistics-queue")
    public void handleStatistics(StatisticsMessage message) {
        try {
            log.info("Received statistics trigger: ledgerId={}, eventType={}", 
                    message.getLedgerId(), message.getEventType());
            
            log.info("Statistics processing placeholder - actual logic will be implemented in Task 7");
        } catch (Exception e) {
            log.error("Failed to process statistics message: {}", message, e);
        }
    }
}
