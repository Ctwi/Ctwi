package com.ctwi.controller;
import com.ctwi.controller.model.ErrorResponse;
import com.ctwi.controller.model.RegisterRequest;
import com.ctwi.controller.model.RegisterResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/users")

public class UserController {
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest input) {
        if(input.username == null || input.username.length() <= 3){
            return new ResponseEntity<>(RegisterResponse.createError("BAD_USERNAME"),HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(RegisterResponse.createSuccess(),HttpStatus.OK);
    }
}
