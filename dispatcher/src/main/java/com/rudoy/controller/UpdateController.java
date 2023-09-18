package com.rudoy.controller;

import com.rudoy.utils.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class UpdateController {
    private TelegramBot telegramBot;
    private MessageUtils messageUtils;

    public UpdateController(MessageUtils messageUtils) {
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
        }
    }

    private void distributeMessageByType(Update update) {
        Message message = update.getMessage();
        if (message.getText() != null) {
            processMessageText(update);
        } else if (message.getDocument() != null) {
            processMessageDocument(update);
        } else if (message.getPhoto() != null) {
            processMessagePhoto(update);
        } else if (message.getAudio() != null) {
            processMessagePhoto(update);
        } else {
            setUnsupportedMessageTypeView(update);
        }
    }

    private void processMessageText(Update update) {
        String text = update.getMessage().getText();
    }

    private void processMessageDocument(Update update) {

    }

    private void processMessagePhoto(Update update) {

    }

    private void setUnsupportedMessageTypeView(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessageWithText(update, "Unsupported message type");
        setView(sendMessage);
    }

    private void setView(SendMessage sendMessage) {
        telegramBot.sendMessage(sendMessage);
    }
}
