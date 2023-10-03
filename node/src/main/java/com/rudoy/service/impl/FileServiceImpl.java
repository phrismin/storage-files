package com.rudoy.service.impl;

import com.rudoy.dao.AppDocumentDAO;
import com.rudoy.dao.BinaryContentDAO;
import com.rudoy.entity.AppDocument;
import com.rudoy.service.FileService;
import lombok.extern.log4j.Log4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.net.MalformedURLException;
import java.net.URL;


@Service
@Log4j
@PropertySource("classpath:application-dev.properties")
public class FileServiceImpl implements FileService {
    @Value("${bot.token}")
    private String token;
    @Value("${service.fileInfo.uri}")
    private String fileInfoUri;
    @Value("${service.fileStorage.uri}")
    private String fileStorageUri;
    private final AppDocumentDAO appDocumentDAO;
    private final BinaryContentDAO binaryContentDAO;

    public FileServiceImpl(AppDocumentDAO appDocumentDAO, BinaryContentDAO binaryContentDAO) {
        this.appDocumentDAO = appDocumentDAO;
        this.binaryContentDAO = binaryContentDAO;
    }

    @Override
    public AppDocument processDoc(Message telegramMessage) {
        String fileId = telegramMessage.getDocument().getFileId();
        ResponseEntity<String> response = getFilePath(fileId);
        if (response.getStatusCode() == HttpStatus.OK) {
            JSONObject jsonObject = new JSONObject(response.getBody());
            String filePath = String.valueOf(jsonObject
                    .getJSONObject("result")
                    .getString("file_path")
            );
            byte[] fileInByte = downloadFile(filePath);
        }
        return null;
    }

    private byte[] downloadFile(String filePath) {
        String fullUri = fileStorageUri.replace("{token}", token).replace("{filePath}", filePath);
        URL urlObject = null;
        try {
            urlObject = new URL(fullUri);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        // TODO считать файл
        return new byte[0];
    }

    private ResponseEntity<String> getFilePath(String fileId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        return restTemplate.exchange(
                fileInfoUri,
                HttpMethod.GET,
                request,
                String.class,
                token,
                fileId
        );

    }
}
