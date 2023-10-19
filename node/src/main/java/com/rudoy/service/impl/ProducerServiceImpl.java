package com.rudoy.service.impl;

import com.rudoy.config.RabbitConfiguration;
import com.rudoy.service.ProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@RequiredArgsConstructor
@Service
public class ProducerServiceImpl implements ProducerService {
    private final RabbitTemplate rabbitTemplate;
    private final RabbitConfiguration rabbitConfiguration;

    @Override
    public void producerAnswer(SendMessage sendMessage) {
        rabbitTemplate.convertAndSend(rabbitConfiguration.getAnswerMessageQueue(), sendMessage);
    }
}
