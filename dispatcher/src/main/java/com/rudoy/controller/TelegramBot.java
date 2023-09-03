package com.rudoy.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;

@Component
@Slf4j
@PropertySource("classpath:application-dev.properties")
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

    private UpdateController updateController;

    public TelegramBot(UpdateController updateController) {
        this.updateController = updateController;
    }

    @PostConstruct
    public void init() {
        updateController.registerTelegramBot(this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message requestMessage = update.getMessage();
        log.debug(requestMessage.getText());

        SendMessage responseMessage = new SendMessage();
        responseMessage.setChatId(requestMessage.getChatId().toString());
        responseMessage.setText("Hello from Local");
        sendMessage(responseMessage);
    }

        public void sendMessage(SendMessage message) {
            try {
                if (message != null) {
                    execute(message);
                }
            } catch (TelegramApiException tae) {
                log.error("Send message is null");
            }
        }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}