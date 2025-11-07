package com.chuseok22.flashtalk.common.infrastructure.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "springdoc")
public record SpringDocProperties(
    @Valid List<Server> servers
) {

  public record Server(
      @NotBlank String url,
      @NotBlank String description
  ) {

  }
}
