package com.chuseok22.flashtalk.chat.core;

import com.chuseok22.flashtalk.chat.core.constant.MessageType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ChatMessage {
  private String id;
  private MessageType messageType;
  private String text;
  private String dataUrl;
  private String mediaId;
  private String nickname;
  private long createdAt;
  private long expiresAt;
}
