package com.rudoy.controller;

import com.rudoy.entity.AppDocument;
import com.rudoy.entity.AppPhoto;
import com.rudoy.entity.BinaryContent;
import com.rudoy.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@RequiredArgsConstructor
@Log4j
@RequestMapping("/file")
@RestController
public class FileController {
    private final FileService fileService;

    @GetMapping("/getDoc")
    public void getDoc(@RequestParam String id, HttpServletResponse response) {
        // TODO добавить @ControllerAdvice
        AppDocument document = fileService.getDocument(id);

        if (document == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        response.setContentType(MediaType.parseMediaType(document.getMimeType()).toString());
        response.setHeader("Content-disposition", "attachment; filename=" + document.getDocName());
        response.setStatus(HttpServletResponse.SC_OK);

        BinaryContent binaryContent = document.getBinaryContent();
        writeFileToResponse(response, binaryContent);
    }

    @GetMapping("/getPhoto")
    public void getPhoto(@RequestParam String id, HttpServletResponse response) {
        // TODO добавить ControllerAdvice
        AppPhoto photo = fileService.getPhoto(id);

        if (photo == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        response.setHeader("Content-disposition", "attachment;");
        response.setStatus(HttpServletResponse.SC_OK);

        BinaryContent binaryContent = photo.getBinaryContent();
        writeFileToResponse(response, binaryContent);
    }

    private void writeFileToResponse(HttpServletResponse response, BinaryContent binaryContent) {
        try (ServletOutputStream out = response.getOutputStream();
             ByteArrayInputStream inputStream = new ByteArrayInputStream(binaryContent.getFileAsArrayOfBytes())) {
            byte[] buffer = new byte[8192];
            int readBytes = 0;
            while (readBytes != -1) {
                readBytes = inputStream.read(buffer);
                out.write(buffer, 0, readBytes);
            }
        } catch (IOException e) {
            log.error(e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
