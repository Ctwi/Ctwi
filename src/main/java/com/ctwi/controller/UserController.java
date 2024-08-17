package com.ctwi.controller;

import com.ctwi.auth.repository.AuthRepository;
import com.ctwi.controller.model.*;
import com.ctwi.service.Auth;
import com.ctwi.service.SessionManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.sql.SQLException;
import java.util.Base64;


@RestController
@RequestMapping("/api/users")

public class UserController {
    private final SessionManager service;
    @Autowired
    public UserController(SessionManager service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest input) {
        if(input.username == null || input.username.length() <= 3) {
            return new ResponseEntity<>(new RegisterResponse().withError("BAD_USERNAME"), HttpStatus.BAD_REQUEST);
        }
        if(input.password == null || input.username.length() <= 5) {
            return new ResponseEntity<>(new RegisterResponse().withError("BAD_PASSWORD"), HttpStatus.BAD_REQUEST);
        }

        //salt生成
        byte[] salt = Auth.generateSalt(16);

        //passwordをhash化
        String hashedPassword = Auth.encodePassword(input.password, salt, 10000, 32);
        String saltBase64 = Base64.getEncoder().encodeToString(salt);

        //db接続
        try {
            AuthRepository.insertUser(input.username,input.email,hashedPassword,saltBase64);
        } catch (SQLException e) {
            return new ResponseEntity<>(new RegisterResponse().withError("SERVER_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(RegisterResponse.createSuccess(), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest input, HttpServletResponse response) {
        if(input.email == null || input.email.length() <= 10) {
            return new ResponseEntity<>(new LoginResponse().withError("BAD_EMAIL"), HttpStatus.BAD_REQUEST);
        }
        if(input.password == null || input.password.length() <= 5) {
            return new ResponseEntity<>(new LoginResponse().withError("BAD_PASSWORD"), HttpStatus.BAD_REQUEST);
        }

        //db接続
        try {
            //dbからuserを取得
            AuthRepository.User user = AuthRepository.fetchUserByEmail(input.email);
            if (user == null) {
                return new ResponseEntity<>(new LoginResponse().withError("USER_NOT_FOUND"), HttpStatus.UNAUTHORIZED);
            }

            //ユーザー情報が存在する場合、password認証
            boolean isAuthenticated = AuthRepository.authenticateUser(input.email, input.password);
            if (!isAuthenticated) {
                return new ResponseEntity<>(new LoginResponse().withError("INVALID_PASSWORD"), HttpStatus.UNAUTHORIZED);
            }

            //認証成功、セッションIdを生成して、Cookieに設定
            String sessionId = this.service.createSession(input.email);
            Cookie cookie = new Cookie("SESSIONID", sessionId);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);

            return new ResponseEntity<>(LoginResponse.createSuccess(), HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(new LoginResponse().withError("SERVER_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/me")
    public ResponseEntity<String> validateSession(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("SESSIONID".equals(cookie.getName())) {
                    String sessionId = cookie.getValue();
                    // セッションIDの検証処理
                    if (this.service.isValidSession(sessionId)) {
                        return new ResponseEntity<>(sessionId, HttpStatus.OK);
                    } else {
                        return new ResponseEntity<>("Invalid session", HttpStatus.UNAUTHORIZED);
                    }
                }
            }
        }
        return new ResponseEntity<>("No session cookie found", HttpStatus.UNAUTHORIZED);
    }
}