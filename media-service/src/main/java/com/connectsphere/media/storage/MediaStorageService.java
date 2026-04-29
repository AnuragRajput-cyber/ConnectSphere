package com.connectsphere.media.storage;

import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface MediaStorageService {

    StoredMediaAsset store(MultipartFile file) throws IOException;

    boolean supportsLocalReads();

    Resource loadAsResource(String storageKey) throws IOException;
}
