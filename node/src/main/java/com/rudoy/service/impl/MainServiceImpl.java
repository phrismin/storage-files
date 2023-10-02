package com.rudoy.service.impl;

import com.rudoy.dao.AppUserDAO;
import com.rudoy.dao.RawDataDAO;
import com.rudoy.entity.AppUser;
import com.rudoy.entity.RawData;
import com.rudoy.entity.enums.UserState;
import com.rudoy.service.MainService;
import com.rudoy.service.ProducerService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static com.rudoy.entity.enums.UserState.BASIC_STATE;

@Service
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
        Message textMessage = update.getMessage();
        User telegramUser = textMessage.getFrom();
        var AppUser = findOrSaveAppUser(telegramUser);

        var message = update.getMessage();
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("Hello from NODE");
        producerService.producerAnswer(sendMessage);
    }

    private AppUser findOrSaveAppUser(User telegramUser) {
        AppUser persistantAppUser = appUserDao.findAppUserByTelegramUserId(telegramUser.getId());
        if (persistantAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .userName(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    // TODO
                    .isActive(true)
                    .userState(BASIC_STATE)
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
