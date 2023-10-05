package com.rudoy.service;

import com.rudoy.entity.AppDocument;
import com.rudoy.entity.AppPhoto;
import com.rudoy.entity.BinaryContent;
import org.springframework.core.io.FileSystemResource;

public interface FileService {
    AppDocument getDocument(String id);
    AppPhoto getPhoto(String id);
    FileSystemResource getFileSystemResource(BinaryContent binaryContent);
}
