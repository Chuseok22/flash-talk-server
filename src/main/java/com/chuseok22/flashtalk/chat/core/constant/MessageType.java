package com.chuseok22.flashtalk.chat.core.constant;

public enum MessageType {
  TEXT,
  IMAGE;

  public static MessageType from(String value) {
    if (value == null) {
      throw new IllegalArgumentException("MessageType을 찾을 수 없습니다.");
    }
    try {
      return MessageType.valueOf(value);
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("MessageType을 찾을 수 없습니다.");
    }
  }
}
