package com.amp.fintech.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class hello {
    
    @GetMapping("/hello")
    public String helloWold() {
        return "Hello World!";
    }
}
