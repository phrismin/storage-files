package com.rudoy.service;

import com.rudoy.entity.AppDocument;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileService {

    AppDocument processDoc(Message externalMessage);
}
