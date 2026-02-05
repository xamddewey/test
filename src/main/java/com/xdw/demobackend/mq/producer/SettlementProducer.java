package com.xdw.demobackend.mq.producer;

import com.xdw.demobackend.dto.message.SettlementMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.xdw.demobackend.config.RabbitMQConfig.SETTLEMENT_EXCHANGE;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementProducer {
    private final RabbitTemplate rabbitTemplate;

    public void triggerSettlement(SettlementMessage message) {
        String routingKey = "settlement.trigger";
        rabbitTemplate.convertAndSend(SETTLEMENT_EXCHANGE, routingKey, message);
        log.info("Sent settlement trigger: ledgerId={}, triggeredBy={}", 
                message.getLedgerId(), message.getTriggeredBy());
    }
}
