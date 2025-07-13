package com.example.modulith.module2;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
@ComponentScan(basePackages = "com.example.modulith.module2")
@ConditionalOnProperty(prefix = "module2", name = "enabled", havingValue = "true", matchIfMissing = false)
public class Module2Config {
}
