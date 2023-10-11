package com.rudoy.service.impl;

import com.rudoy.dao.AppPhotoDAO;
import com.rudoy.dao.AppUserDAO;
import com.rudoy.dao.RawDataDAO;
import com.rudoy.entity.AppDocument;
import com.rudoy.entity.AppPhoto;
import com.rudoy.entity.AppUser;
import com.rudoy.entity.RawData;
import com.rudoy.entity.enums.UserState;
import com.rudoy.exeptions.UploadFileException;
import com.rudoy.service.AppUserService;
import com.rudoy.service.FileService;
import com.rudoy.service.MainService;
import com.rudoy.service.ProducerService;
import com.rudoy.service.enums.LinkType;
import com.rudoy.service.enums.ServiceCommands;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

@Service
@Log4j
public class MainServiceImpl implements MainService {
    private final ProducerService producerService;
    private final RawDataDAO rawDataDAO;
    private final AppUserDAO appUserDao;
    private final FileService fileService;
    private final AppPhotoDAO appPhotoDAO;
    private final AppUserService appUserService;

    public MainServiceImpl(ProducerService producerService,
                           RawDataDAO rawDataDAO,
                           AppUserDAO appUserDao,
                           FileService fileService,
                           AppPhotoDAO appPhotoDAO,
                           AppUserService appUserService) {
        this.producerService = producerService;
        this.rawDataDAO = rawDataDAO;
        this.appUserDao = appUserDao;
        this.fileService = fileService;
        this.appPhotoDAO = appPhotoDAO;
        this.appUserService = appUserService;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        String text = update.getMessage().getText();
        UserState userState = appUser.getUserState();

        String output = "";
        ServiceCommands serviceCommand = ServiceCommands.fromValue(text);
        if (ServiceCommands.CANCEL.equals(serviceCommand)) {
            output = cancelProcess(appUser);
        } else if (UserState.BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, text);
        } else if (UserState.WAIT_FOR_EMAIL_STATE.equals(userState)) {
            output = appUserService.setEmail(appUser, text);
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

        try {
            AppDocument appDocument = fileService.processDoc(update.getMessage());
            String link = fileService.generateLink(appDocument.getId(), LinkType.GET_DOC);
            var answer = "Document is successfully loaded! The link for load:" + link;
            sendAnswer(answer, chatId);
        } catch (UploadFileException e) {
            log.error(e);
            var error = "Unfortunately, file upload failed. Try again later.";
            sendAnswer(error, chatId);
        }
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();

        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        try {
            AppPhoto appPhoto = fileService.processPhoto(update.getMessage());
            String link = fileService.generateLink(appPhoto.getId(), LinkType.GET_PHOTO);
            var answer = "Photo is successfully loaded! The link for load: " + link;
            sendAnswer(answer, chatId);
        } catch (UploadFileException e) {
            log.error(e);
            var error = "Unfortunately, photo upload failed. Try again later.";
            sendAnswer(error, chatId);
        }
    }

    @Override
    public void processVoiceMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();

        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        // TODO добавить сохранение voice
        String answer = "Voice is successfully loaded! The link for load: http:test.com/getVoice/111";
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
        }
        return false;
    }

    private void sendAnswer(String output, Long chatId) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.producerAnswer(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, String cmd) {
        ServiceCommands serviceCommand = ServiceCommands.fromValue(cmd);
        if (ServiceCommands.REGISTRATION.equals(serviceCommand)) {
            return appUserService.registerUser(appUser);
        } else if (ServiceCommands.START.equals(serviceCommand)) {
            return "Hello! To see the list of available commands, press the /help.";
        } else if (ServiceCommands.HELP.equals(serviceCommand)) {
            return help();
        } else {
            return "Unknown command! To see the list of available commands, press the /help.";
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

        Optional<AppUser> optionalAppUser = appUserDao.findByTelegramUserId(telegramUser.getId());
        if (optionalAppUser.isEmpty()) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .userName(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .isActive(false)
                    .userState(UserState.BASIC_STATE)
                    .build();

            return appUserDao.save(transientAppUser);
        }

       return optionalAppUser.get();
    }

    private void saveRawData(Update update) {
        RawData rawData = new RawData();
        rawData.setEvent(update);
        rawDataDAO.save(rawData);
    }
}
