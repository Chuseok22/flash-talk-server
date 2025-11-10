package com.chuseok22.flashtalk.chat.application.service;

import com.chuseok22.flashtalk.chat.application.dto.IngestResult;
import com.chuseok22.flashtalk.storage.infrastructure.properties.StorageProperties;
import com.chuseok22.flashtalk.storage.infrastructure.service.StorageService;
import java.util.Base64;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageIngestService {

  private final StorageService storageService;
  private final StorageProperties properties;

  public Mono<IngestResult> ingest(String contentType, String base64OrDataUrl) {
    if (!properties.allowedContentTypes().contains(contentType)) {
      return Mono.error(new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "PNG/JPEG 만 업로드 가능합니다."));
    }

    String base64 = stripDataUrlPrefix(base64OrDataUrl, contentType);
    byte[] bytes;
    try {
      bytes = Base64.getDecoder().decode(base64);
    } catch (IllegalArgumentException e) {
      return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 Base64 요청입니다."));
    }

    if (bytes.length > properties.maxSizeBytes()) {
      return Mono.error(new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "이미지 최대 크기 초과"));
    }

    String mediaId = UUID.randomUUID().toString();
    String dataUrl = "data:" + contentType + ";base64," + base64;

    return storageService.save(bytes, contentType)
      .onErrorResume(ex -> Mono.empty())
      .thenReturn(new IngestResult(mediaId, contentType, dataUrl, bytes.length));
  }

  private String stripDataUrlPrefix(String base64OrDataUrl, String contentType) {
    String prefix = "data:" + contentType + ";base64,";
    if (base64OrDataUrl.startsWith(prefix)) {
      return base64OrDataUrl.substring(prefix.length());
    }
    return base64OrDataUrl;
  }
}
