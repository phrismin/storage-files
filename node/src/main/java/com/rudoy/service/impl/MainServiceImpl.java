package com.rudoy.service.impl;

import com.rudoy.dao.AppUserDAO;
import com.rudoy.dao.RawDataDAO;
import com.rudoy.entity.AppUser;
import com.rudoy.entity.RawData;
import com.rudoy.entity.enums.UserState;
import com.rudoy.service.MainService;
import com.rudoy.service.ProducerService;
import com.rudoy.service.enums.ServiceCommands;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Service
@Log4j
public class MainServiceImpl implements MainService {
    private final ProducerService producerService;
    private final RawDataDAO rawDataDAO;
    private final AppUserDAO appUserDao;

    public MainServiceImpl(ProducerService producerService, RawDataDAO rawDataDAO, AppUserDAO appUserDao) {
        this.producerService = producerService;
        this.rawDataDAO = rawDataDAO;
        this.appUserDao = appUserDao;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        String text = update.getMessage().getText();
        UserState userState = appUser.getUserState();

        var output = "";
        if (ServiceCommands.CANCEL.equals(text)) {
            output = cancelProcess(appUser);
        } else if (UserState.BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, text);
        } else if (UserState.WAIT_FOR_EMAIL_STATE.equals(userState)) {
            // TODO добавить обработку мыла
        } else {
            log.error("Unknown user state: " + userState);
            output = "Unknown error! Please, input /cancel and retry again!";
        }

        Long chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);
    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();

        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        // TODO добавить сохранение документа
        String answer = "Document is successfully loaded! The link for load: http:test.com/getDoc/555";
        sendAnswer(answer, chatId);
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();

        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        // TODO добавить сохранение фото
        String answer = "Photo is successfully loaded! The link for load: http:test.com/getPhoto/555";
        sendAnswer(answer, chatId);
    }

    @Override
    public void processAudioMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();

        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        // TODO добавить сохранение audio
        String answer = "Audio is successfully loaded! The link for load: http:test.com/getAudio/111";
        sendAnswer(answer, chatId);
    }

    private boolean isNotAllowToSendContent(Long chatId, AppUser appUser) {
        UserState userState = appUser.getUserState();
        if (!appUser.getIsActive()) {
            String error = "Register or activate your account to download content.";
            sendAnswer(error, chatId);
            return true;
        } else if (!UserState.BASIC_STATE.equals(userState)) {
            String error = "Cancel the current command with /cancel to send files.";
            sendAnswer(error, chatId);
            return true;
        } else {
            return false;
        }
    }

    private void sendAnswer(String output, Long chatId) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.producerAnswer(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, String cmd) {
        if (ServiceCommands.REGISTRATION.equals(cmd)) {
            // TODO добавить регистрацию
            return "Temporally unavailable";
        } else if (ServiceCommands.START.equals(cmd)) {
            return "Hello! To see the list of available commands, press the /help";
        } else if (ServiceCommands.HELP.equals(cmd)) {
            return help();
        } else {
            return "Unknown command! To see the list of available commands, press the /help";
        }
    }

    private String help() {
        return "The list of available commands:\n" +
                " /cancel - cancel executing current command;\n" +
                " /registration - registration user;\n";
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setUserState(UserState.BASIC_STATE);
        appUserDao.save(appUser);
        return "Command cancelled!";
    }

    private AppUser findOrSaveAppUser(Update update) {
        Message textMessage = update.getMessage();
        User telegramUser = textMessage.getFrom();

        AppUser persistantAppUser = appUserDao.findAppUserByTelegramUserId(telegramUser.getId());
        if (persistantAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .userName(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    // TODO
                    .isActive(true)
                    .userState(UserState.BASIC_STATE)
                    .build();

            return appUserDao.save(transientAppUser);
        }

       return persistantAppUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = new RawData();
        rawData.setEvent(update);

        rawDataDAO.save(rawData);
    }
}
