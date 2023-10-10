package com.rudoy.service.impl;

import com.rudoy.dao.AppUserDAO;
import com.rudoy.entity.AppUser;
import com.rudoy.service.UserActivationService;
import com.rudoy.utils.CryptoTool;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserActivationServiceImpl implements UserActivationService {
    private final AppUserDAO appUserDAO;
    private final CryptoTool cryptoTool;

    public UserActivationServiceImpl(AppUserDAO appUserDAO, CryptoTool cryptoTool) {
        this.appUserDAO = appUserDAO;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public boolean activation(String cryptoId) {
        Long userId = cryptoTool.idOf(cryptoId);
        Optional<AppUser> optionalAppUser = appUserDAO.findById(userId);
        if (optionalAppUser.isPresent()) {
            AppUser appUser = optionalAppUser.get();
            appUser.setIsActive(true);
            appUserDAO.save(appUser);
            return true;
        }
        return false;
    }
}
