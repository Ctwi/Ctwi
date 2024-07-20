package com.ctwi.controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")

public class UserController {
    @RequestMapping("/text1")
    public String text1() {
        return "text content";
    }
}
