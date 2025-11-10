package com.chuseok22.flashtalk.chat.core.constant;

import java.util.Optional;

public enum MessageType {
  TEXT,
  IMAGE;

  public static MessageType from(String value) {
    return Optional.ofNullable(value)
      .map(MessageType::valueOf)
      .orElseThrow(() -> new IllegalArgumentException("MessageType을 찾을 수 없습니다."));
  }
}
