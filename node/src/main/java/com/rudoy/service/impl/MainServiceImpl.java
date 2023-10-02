package com.rudoy.service.impl;

import com.rudoy.dao.RawDataDAO;
import com.rudoy.entity.RawData;
import com.rudoy.service.MainService;
import com.rudoy.service.ProducerService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class MainServiceImpl implements MainService {
    private final ProducerService producerService;
    private final RawDataDAO rawDataDAO;

    public MainServiceImpl(ProducerService producerService, RawDataDAO rawDataDAO) {
        this.producerService = producerService;
        this.rawDataDAO = rawDataDAO;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);

        var message = update.getMessage();
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("Hello from NODE");
        producerService.producerAnswer(sendMessage);
    }

    private void saveRawData(Update update) {
        RawData rawData = new RawData();
        rawData.setEvent(update);

        rawDataDAO.save(rawData);
    }
}
