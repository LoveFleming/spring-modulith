package com.example.modulith.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulith;

@SpringBootApplication(scanBasePackages = "com.example.modulith")
@Modulith
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
