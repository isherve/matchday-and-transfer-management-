package com.ferwafa.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ferwafa.discipline")
public class DisciplineProperties {
    private int yellowCardThreshold = 2;
}
