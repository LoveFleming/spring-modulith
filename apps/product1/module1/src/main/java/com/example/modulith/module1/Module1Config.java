package com.example.modulith.module1;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
@ComponentScan(basePackages = "com.example.modulith.module1")
@ConditionalOnProperty(prefix = "module1", name = "enabled", havingValue = "true", matchIfMissing = false)
public class Module1Config {
}
