package com.example.modulith.module1;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Module1Controller {

    @GetMapping("/module1/hello")
    public String hello() {
        return "Hello from Module 1!";
    }
}
