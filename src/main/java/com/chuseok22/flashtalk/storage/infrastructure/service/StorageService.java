package com.chuseok22.flashtalk.storage.infrastructure.service;

import com.chuseok22.flashtalk.storage.infrastructure.properties.StorageProperties;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
@RequiredArgsConstructor
public class StorageService {

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

  private final StorageProperties properties;

  public Mono<String> save(byte[] bytes, String contentType) {
    if (!properties.allowedContentTypes().contains(contentType)) {
      return Mono.error(new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "PNG/JPEG 형식만 업로드 가능합니다."));
    }
    if (bytes.length > properties.maxSizeBytes()) {
      return Mono.error(new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "최대 10MB 업로드 가능합니다."));
    }

    String extension = resolveExtension(contentType);
    String datePath = LocalDate.now().format(DATE_TIME_FORMATTER);
    String fileName = UUID.randomUUID() + "." + extension;

    Path target = Paths.get(properties.basePath(), datePath, fileName);

    return Mono.fromCallable(() -> {
      Files.createDirectories(target.getParent());
      Files.write(target, bytes, StandardOpenOption.CREATE_NEW);
      return datePath + "/" + fileName;
    }).subscribeOn(Schedulers.boundedElastic());
  }

  private String resolveExtension(String contentType) {
    if (MediaType.IMAGE_PNG_VALUE.equals(contentType)) {
      return "png";
    }
    if (MediaType.IMAGE_JPEG_VALUE.equals(contentType)) {
      return "jpg";
    }
    throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "PNG/JPEG 만 업로그 가능합니다.");
  }

}
