package com.example.modulith.module3;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
@ComponentScan(basePackages = "com.example.modulith.module3")
@ConditionalOnProperty(prefix = "module3", name = "enabled", havingValue = "true", matchIfMissing = false)
public class Module3Config {
}
