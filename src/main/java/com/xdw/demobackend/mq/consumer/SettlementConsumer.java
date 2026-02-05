package com.xdw.demobackend.mq.consumer;

import com.xdw.demobackend.dto.message.SettlementMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementConsumer {

    @RabbitListener(queues = "settlement-queue")
    public void handleSettlement(SettlementMessage message) {
        try {
            log.info("Received settlement trigger: ledgerId={}, triggeredBy={}", 
                    message.getLedgerId(), message.getTriggeredBy());
            
            log.info("Settlement processing placeholder - actual logic will be implemented in Task 5");
        } catch (Exception e) {
            log.error("Failed to process settlement message: {}", message, e);
        }
    }
}
