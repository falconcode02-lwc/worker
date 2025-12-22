package io.falconFlow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class testcontroller {

    @GetMapping("/test")
    public String get(){
        return  "Hello World";
    }
}
