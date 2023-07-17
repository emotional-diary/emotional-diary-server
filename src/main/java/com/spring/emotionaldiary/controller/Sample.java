package com.spring.emotionaldiary.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Sample {
    @RequestMapping("/sample")
    public String greeting(){
        return "sample!!";
    }
}
