package com.rudoy.service.impl;

import com.rudoy.dao.AppUserDAO;
import com.rudoy.entity.AppUser;
import com.rudoy.entity.enums.UserState;
import com.rudoy.service.AppUserService;
import com.rudoy.utils.CryptoTool;
import com.sun.net.httpserver.Headers;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Optional;

@Service
@Log4j
public class AppUserServiceImpl implements AppUserService {
    private final AppUserDAO appUserDAO;
    private final CryptoTool cryptoTool;
    @Value("${service.mail.uri}")
    private String mailServiceUri;

    public AppUserServiceImpl(AppUserDAO appUserDAO, CryptoTool cryptoTool) {
        this.appUserDAO = appUserDAO;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public String registerUser(AppUser appUser) {
        if (appUser.getIsActive()) {
            return "You are already registered!";
        } else if (appUser.getEmail() != null) {
            return "You've already been emailed. " +
                    "Follow the link in the email to confirm registration.";
        }
        appUser.setUserState(UserState.WAIT_FOR_EMAIL_STATE);
        appUserDAO.save(appUser);
        return "Please enter your email address:";
    }

    @Override
    public String setEmail(AppUser appUser, String email) {
        try {
            InternetAddress emailAddress = new InternetAddress(email);
        } catch (AddressException e) {
            return "Please enter valid email. To cancel, press the /cancel.";
        }
        Optional<AppUser> optionalUser = appUserDAO.findByEmail(email);
        if (optionalUser.isEmpty()) {
            appUser.setEmail(email);
            appUser.setUserState(UserState.BASIC_STATE);
            appUser = appUserDAO.save(appUser);

            String cryptoUserId = cryptoTool.hashOf(appUser.getId());
            ResponseEntity<?> response = sendRequestToMailService(cryptoUserId, email);
            if (response.getStatusCode() != HttpStatus.OK) {
                String message = String.format("Sending an email to the post %s failed.", email);
                log.error(message);
                appUser.setEmail(null);
                appUserDAO.save(appUser);
                return message;
            }
            return "An email has been sent to your email. " +
                    "Follow the link in the email to confirm registration.";
        } else {
            return "This email is already in use. Please enter valid email. " +
                    "To cancel, press the /cancel.";
        }
    }

    private ResponseEntity<?> sendRequestToMailService(String cryptoUserId, String email) {
        return null;
    }
}
