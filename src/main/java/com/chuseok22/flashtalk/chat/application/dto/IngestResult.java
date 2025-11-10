package com.chuseok22.flashtalk.chat.application.dto;

public record IngestResult(
  String mediaId,
  String contentType,
  String dataUrl,
  long sizeBytes
) {

}
