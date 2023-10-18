package com.rudoy.service;

import com.rudoy.dto.MailParams;

public interface MailSenderService {
    void send(MailParams mailParams);
}
