package com.rudoy.controller;

import com.rudoy.entity.AppDocument;
import com.rudoy.entity.AppPhoto;
import com.rudoy.entity.BinaryContent;
import com.rudoy.service.FileService;
import lombok.extern.log4j.Log4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/file")
@Log4j
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/getDoc")
    public ResponseEntity<?> getDoc(@RequestParam String id) {
        // TODO добавить ControllerAdvice
        AppDocument document = fileService.getDocument(id);

        if (document == null) {
            return ResponseEntity.badRequest().build();
        }

        BinaryContent binaryContent = document.getBinaryContent();
        FileSystemResource fileSystemResource = fileService.getFileSystemResource(binaryContent);
        if (fileSystemResource == null) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getMimeType()))
                .header("Content-disposition", "attachment; filename=" + document.getDocName())
                .body(fileSystemResource);
    }

    @GetMapping("/getPhoto")
    public ResponseEntity<?> getPhoto(@RequestParam String id) {
        // TODO добавить ControllerAdvice
        AppPhoto photo = fileService.getPhoto(id);

        if (photo == null) {
            return ResponseEntity.badRequest().build();
        }

        BinaryContent binaryContent = photo.getBinaryContent();
        FileSystemResource fileSystemResource = fileService.getFileSystemResource(binaryContent);
        if (fileSystemResource == null) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(MediaType.IMAGE_JPEG_VALUE))
                .header("Content-disposition", "attachment;")
                .body(fileSystemResource);
    }
}
