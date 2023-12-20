package com.rudoy.service.impl;

import com.rudoy.dao.AppDocumentDAO;
import com.rudoy.dao.AppPhotoDAO;
import com.rudoy.entity.AppDocument;
import com.rudoy.entity.AppPhoto;
import com.rudoy.service.FileService;
import com.rudoy.utils.CryptoTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Log4j
@Service
public class FileServiceImpl implements FileService {
    private final AppDocumentDAO appDocumentDAO;
    private final AppPhotoDAO appPhotoDAO;
    private final CryptoTool cryptoTool;

    @Override
    public AppDocument getDocument(String hashId) {
        Long id = cryptoTool.idOf(hashId);
        if (id == null) {
            return null;
        }
        return appDocumentDAO.findById(id).orElse(null);
    }

    @Override
    public AppPhoto getPhoto(String hashId) {
        Long id = cryptoTool.idOf(hashId);
        if (id == null) {
            return null;
        }
        return appPhotoDAO.findById(id).orElse(null);
    }
}

