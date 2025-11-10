package com.chuseok22.flashtalk.storage.infrastructure.properties;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage.audit-image")
public record StorageProperties(
  String basePath,
  long maxSizeBytes,
  List<String> allowedContentTypes
) {

}
