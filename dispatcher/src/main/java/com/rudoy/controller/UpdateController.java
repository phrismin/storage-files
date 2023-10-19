package com.rudoy.controller;

import com.rudoy.service.UpdateProducer;
import com.rudoy.utils.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.rudoy.model.RabbitQueue.*;

@Component
@Slf4j
public class UpdateController {
    private TelegramBot telegramBot;
    private MessageUtils messageUtils;
    private UpdateProducer updateProducer;

    public UpdateController(MessageUtils messageUtils, UpdateProducer updateProducer) {
        this.updateProducer = updateProducer;
        this.messageUtils = messageUtils;
    }

    public void registerTelegramBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Received message is null");
            return;
        }

        if (update.getMessage() != null) {
            distributeMessageByType(update);
        } else {
            log.error("Unsupported message type is received: " + update);
        }
    }

    private void distributeMessageByType(Update update) {
        Message message = update.getMessage();
        if (message.hasText()) {
            processMessageText(update);
        } else if (message.hasDocument()) {
            processMessageDocument(update);
        } else if (message.hasPhoto()) {
            processMessagePhoto(update);
        } else {
            setUnsupportedMessageTypeView(update);
        }
    }

    private void processMessageText(Update update) {
        updateProducer.produce(TEXT_MESSAGE_UPDATE, update);
    }

    private void processMessageDocument(Update update) {
        updateProducer.produce(DOC_MESSAGE_UPDATE, update);
    }

    private void processMessagePhoto(Update update) {
        updateProducer.produce(PHOTO_MESSAGE_UPDATE, update);
    }

    private void setUnsupportedMessageTypeView(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessageWithText(update, "Unsupported message type.");
        setView(sendMessage);
    }

    public void setView(SendMessage sendMessage) {
        telegramBot.sendMessage(sendMessage);
    }
}
