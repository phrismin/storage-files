package com.rudoy.controller;

import com.rudoy.service.UpdateProducer;
import com.rudoy.utils.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.rudoy.RabbitQueue.*;

@Component
@Slf4j
public class UpdateController {
    private TelegramBot telegramBot;
    private MessageUtils messageUtils;
    private UpdateProducer updateProducer;

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
        } else {
            log.error("Unsupported message type is received: " + update);
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
            processMessageAudio(update);
        } else {
            setUnsupportedMessageTypeView(update);
        }
    }

    private void processMessageText(Update update) {
        updateProducer.produce(TEXT_MESSAGE_UPDATE, update);
        setFileIsReceivedView(update);
    }

    private void processMessageDocument(Update update) {
        updateProducer.produce(DOC_MESSAGE_UPDATE, update);
        setFileIsReceivedView(update);
    }

    private void processMessagePhoto(Update update) {
        updateProducer.produce(PHOTO_MESSAGE_UPDATE, update);
        setFileIsReceivedView(update);
    }

    private void processMessageAudio(Update update) {
        updateProducer.produce(AUDIO_MESSAGE_UPDATE, update);
        setFileIsReceivedView(update);
    }

    private void setFileIsReceivedView(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessageWithText(update, "File is received");
        setView(sendMessage);
    }

    private void setUnsupportedMessageTypeView(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessageWithText(update, "Unsupported message type");
        setView(sendMessage);
    }

    private void setView(SendMessage sendMessage) {
        telegramBot.sendMessage(sendMessage);
    }
}
