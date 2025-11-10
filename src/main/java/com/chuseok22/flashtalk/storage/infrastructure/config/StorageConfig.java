package com.chuseok22.flashtalk.storage.infrastructure.config;

import com.chuseok22.flashtalk.storage.infrastructure.properties.StorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

}
