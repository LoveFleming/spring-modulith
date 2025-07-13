package com.example.modulith.module2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Module2Controller {

    @GetMapping("/module2/hello")
    public String hello() {
        return "Hello from Module 2!";
    }
}
