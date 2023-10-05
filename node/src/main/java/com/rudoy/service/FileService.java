package com.rudoy.service;

import com.rudoy.entity.AppDocument;
import com.rudoy.entity.AppPhoto;
import com.rudoy.service.enums.LinkType;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileService {
    AppDocument processDoc(Message telegramMessage);
    AppPhoto processPhoto(Message telegramMessage);
    String generateLink(Long docId, LinkType linkType);
}
