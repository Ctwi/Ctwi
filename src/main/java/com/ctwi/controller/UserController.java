package com.ctwi.controller;
import com.ctwi.Message.Message;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin
@RequestMapping("/api/users")

public class UserController {
    @RequestMapping("/userAPI")
    public Message hello(@RequestParam(value = "name") String name) {
        String message = "こんにちは。" + name + "さん！";

        return new Message(message);
    }
}
