package com.example.modulith.module3;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Module3Controller {

    @GetMapping("/module3/hello")
    public String hello() {
        return "Hello from Module 3!";
    }
}
